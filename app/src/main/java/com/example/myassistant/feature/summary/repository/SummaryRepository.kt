package com.example.myassistant.feature.summary.repository

import com.example.myassistant.BuildConfig
import com.example.myassistant.core.database.dao.SummaryDao
import com.example.myassistant.core.database.dao.TranscriptDao
import com.example.myassistant.core.database.entity.SummaryEntity
import com.example.myassistant.core.network.GeminiApi
import com.example.myassistant.core.network.GeminiContent
import com.example.myassistant.core.network.GeminiPart
import com.example.myassistant.core.network.GeminiRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor(
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao,
    private val geminiApi: GeminiApi
) {

    suspend fun generateSummary(meetingId: String) {
        try {
            val transcripts = transcriptDao.getTranscriptOnce(meetingId)
            val fullText = transcripts.joinToString(" ") { it.text }

            android.util.Log.d("SummaryDebug", "Generating summary for text: $fullText")

            val gson = Gson()
            summaryDao.insert(
                SummaryEntity(
                    id = UUID.randomUUID().toString(),
                    meetingId = meetingId,
                    title = "Team Meeting Summary",
                    summary = "The team discussed project timelines and Q2 deliverables. Key decisions were made regarding feature rollout and sprint planning.",
                    actionItems = gson.toJson(listOf(
                        "Follow up on Q2 deliverables",
                        "Schedule next sprint planning meeting",
                        "Review feature rollout timeline"
                    )),
                    keyPoints = gson.toJson(listOf(
                        "Feature rollout is on track",
                        "Q2 timelines discussed",
                        "Team alignment achieved"
                    ))
                )
            )
            android.util.Log.d("SummaryDebug", "Summary inserted successfully")
        } catch (e: Exception) {
            android.util.Log.e("SummaryDebug", "Summary error: ${e.message}", e)
        }
    }

    fun getSummary(meetingId: String): Flow<SummaryEntity?> {
        return summaryDao.getSummaryForMeeting(meetingId)
    }

    private data class SummaryResponse(
        val title: String = "",
        val summary: String = "",
        val actionItems: List<String> = emptyList(),
        val keyPoints: List<String> = emptyList()
    )
}