package com.example.myassistant.feature.recording.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.myassistant.core.database.dao.AudioChunkDao
import com.example.myassistant.core.database.dao.MeetingDao
import com.example.myassistant.core.database.entity.MeetingEntity
import com.example.myassistant.feature.recording.service.RecordingService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val meetingDao: MeetingDao,
    private val audioChunkDao: AudioChunkDao
) {

    fun startRecording(context: Context) {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopRecording(context: Context) {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

    }

    fun getAllMeetings(): Flow<List<MeetingEntity>> {
        return meetingDao.getAllMeetings()
    }

    suspend fun getMeetingById(id: String): MeetingEntity? {
        return meetingDao.getMeetingById(id)
    }
}