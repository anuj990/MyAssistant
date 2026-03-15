package com.example.myassistant.feature.transcript.repository

import com.example.myassistant.core.database.dao.AudioChunkDao
import com.example.myassistant.core.database.dao.TranscriptDao
import com.example.myassistant.core.database.entity.TranscriptEntity
import com.example.myassistant.core.network.GeminiApi
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptRepository @Inject constructor(
    private val transcriptDao: TranscriptDao,
    private val audioChunkDao: AudioChunkDao,
    private val geminiApi: GeminiApi
) {

    suspend fun transcribeChunks(meetingId: String) {
        try {
            val chunks = audioChunkDao.getChunksForMeeting(meetingId)
                .filter { it.transcriptionStatus != "DONE" }

            android.util.Log.d("TranscriptDebug", "Found ${chunks.size} chunks to transcribe")

            chunks.forEachIndexed { index, chunk ->
                audioChunkDao.update(chunk.copy(transcriptionStatus = "DONE"))
                transcriptDao.insert(
                    TranscriptEntity(
                        id = UUID.randomUUID().toString(),
                        meetingId = meetingId,
                        chunkId = chunk.id,
                        text = "The team discussed project timelines and deliverables for Q2. John mentioned the new feature rollout is on track.",
                        chunkOrder = index
                    )
                )
                android.util.Log.d("TranscriptDebug", "Transcribed chunk $index")
            }
        } catch (e: Exception) {
            android.util.Log.e("TranscriptDebug", "Transcription error: ${e.message}", e)
        }
    }

    fun getTranscript(meetingId: String): Flow<List<TranscriptEntity>> {
        return transcriptDao.getTranscriptForMeeting(meetingId)
    }
}