package com.example.myassistant.feature.recording.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.StatFs
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.example.myassistant.core.database.dao.AudioChunkDao
import com.example.myassistant.core.database.dao.MeetingDao
import com.example.myassistant.core.database.entity.AudioChunkEntity
import com.example.myassistant.core.database.entity.MeetingEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import javax.inject.Inject
import kotlin.jvm.java

@AndroidEntryPoint
class RecordingService : Service(){
    @Inject lateinit var meetingDao: MeetingDao
    @Inject lateinit var audioChunkDao: AudioChunkDao
    private val scope  = CoroutineScope(Dispatchers.IO+ SupervisorJob())
    private var meetingId: String = ""
    private var chunkIndex: Int = 0
    private var mediaRecorder: MediaRecorder? = null
    private var isPausedByAudioFocus = false
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
    private var chunkTimer: Timer? = null
    private var isPausedByPhoneCall = false
    private var silenceCounter = 0
    private var silenceTimer: Timer? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupAudioFocus()
        registerReceiver(
            phoneCallReceiver,
            IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        )
        val headsetFilter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(headsetReceiver, headsetFilter)
    }
    private fun setupAudioFocus() {
        audioManager = getSystemService(AudioManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recording Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "recording_channel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }
    private fun startRecording(){
        meetingId = UUID.randomUUID().toString()
        chunkIndex = 0
        startForeground(NOTIFICATION_ID, buildNotification("Recording..."))
        if(!hasEnoughStorage()){
            updateNotification("Recording Stopped - Low Storage")
            stopSelf()
            return
        }
        scope.launch {
            meetingDao.insert(MeetingEntity(
                id = meetingId,
                title = "Meeting ${System.currentTimeMillis()}",
                startingTime = System.currentTimeMillis(),
                status = "RECORDING",
                endTime = null
            )
            )

        }
        startNewChunk()
        startChunktimer()
    }
    private fun updateNotification(status: String){
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID,buildNotification(status))
    }

    private fun startChunktimer(){
        chunkTimer = Timer()
        chunkTimer?.schedule(object : TimerTask(){
            override fun run() {
                rotatechunk()
            }
        },30000L,30000L)
    }
    private fun rotatechunk(){
        val savedFilePath = "${getExternalFilesDir(null)}/chunk_${meetingId}_${chunkIndex}.m4a"
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        scope.launch {
            audioChunkDao.insert(
                AudioChunkEntity(
                    id = UUID.randomUUID().toString(),
                    meetingId = meetingId,
                    filePath =  savedFilePath,
                    chunkIndex  = chunkIndex,
                    transcriptionStatus = "PENDING"
                )
            )
        }
        if (!hasEnoughStorage()) {
            updateNotification("Recording stopped - Low storage")
            stopRecording()
            return
        }
        chunkIndex++
        startNewChunk()

    }
    private fun startNewChunk() {
        val outputFile = "${getExternalFilesDir(null)}/chunk_${meetingId}_${chunkIndex}.m4a"

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile)
            prepare()
            start()
        }
        startSilenceDetection()
    }

    private fun stopRecording() {
        chunkTimer?.cancel()
        chunkTimer = null
        silenceTimer?.cancel()
        silenceTimer = null
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder  = null
        scope.launch {
            meetingDao.getMeetingById(meetingId)?.let {meeting->
                meetingDao.update(
                    meeting.copy(endTime = System.currentTimeMillis(), status = "STOPPED")
                )

            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    private fun buildNotification(status: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TwinMind")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
        scope.cancel()
        stopRecording()
        try { unregisterReceiver(phoneCallReceiver) } catch (e: Exception) { }
        try { unregisterReceiver(headsetReceiver) } catch (e: Exception) { }
    }
    private fun getAvailableStorageMB(): Long{
        val stats  = StatFs(getExternalFilesDir(null)?.path)
        val bytesAvailable = stats.blockSizeLong*stats.availableBlocksLong
        return bytesAvailable/(1024*1024)
    }
    private fun hasEnoughStorage(): Boolean{
        return getAvailableStorageMB() > 50
    }
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener{focusChange->
        when(focusChange){
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT->{
                if(!isPausedByAudioFocus){
                    isPausedByAudioFocus = true
                    pauseRecording()
                }
            }

            AudioManager.AUDIOFOCUS_GAIN->{
                if(isPausedByAudioFocus){
                    isPausedByAudioFocus = false
                    resumeRecording()
                }
            }
        }
    }
    private fun pauseRecording(){
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        chunkTimer?.cancel()
        chunkTimer = null
        silenceTimer?.cancel()
        silenceTimer = null
        updateNotification("Paused - Audio Focus Lost")

    }
    private fun resumeRecording(){
        if(!hasEnoughStorage()){
            updateNotification("Recording Stopped - Low Storage")
            stopRecording()
            return
        }
        chunkIndex++
        startNewChunk()
        startChunktimer()
        updateNotification("Recording...")
    }
    private val phoneCallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING,
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (!isPausedByPhoneCall && !isPausedByAudioFocus) {
                        isPausedByPhoneCall = true
                        pauseRecording()
                        updateNotification("Paused - Phone call")
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (isPausedByPhoneCall) {
                        isPausedByPhoneCall = false
                        resumeRecording()
                    }
                }
            }
        }
    }
    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 1) {
                        updateNotification("Microphone source changed - Wired headset")
                    } else if (state == 0) {
                        updateNotification("Microphone source changed - Headset removed")
                    }
                    rotatechunk()
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    updateNotification("Microphone source changed - Bluetooth connected")
                    rotatechunk()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    updateNotification("Microphone source changed - Bluetooth disconnected")
                    rotatechunk()
                }
            }
        }
    }
    private fun startSilenceDetection() {
        silenceCounter = 0
        silenceTimer?.cancel()
        silenceTimer = Timer()
        silenceTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                if (amplitude == 0) {
                    silenceCounter++
                    if (silenceCounter >= 10) {
                        updateNotification("No audio detected - Check microphone")
                    }
                } else {
                    silenceCounter = 0
                }
            }
        }, 1000L, 1000L)
    }
}