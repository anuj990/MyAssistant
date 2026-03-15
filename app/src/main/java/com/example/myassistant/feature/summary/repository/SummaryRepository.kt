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
        val transcripts = transcriptDao.getTranscriptOnce(meetingId)
        val fullText = transcripts.joinToString(" ") { it.text }

        try {
            val response = geminiApi.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(
                                    text = """
                                        Analyze this meeting transcript and provide a structured summary.
                                        Return ONLY a JSON object with this exact format, no markdown:
                                        {
                                            "title": "meeting title here",
                                            "summary": "2-3 sentence summary here",
                                            "actionItems": ["action 1", "action 2"],
                                            "keyPoints": ["key point 1", "key point 2"]
                                        }
                                        
                                        Transcript: $fullText
                                    """.trimIndent()
                                )
                            )
                        )
                    )
                )
            )

            val rawText = response.candidates
                .firstOrNull()?.content?.parts
                ?.firstOrNull()?.text ?: ""

            val gson = Gson()
            val cleanText = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            val parsed = gson.fromJson(cleanText, SummaryResponse::class.java)

            summaryDao.insert(
                SummaryEntity(
                    id = UUID.randomUUID().toString(),
                    meetingId = meetingId,
                    title = parsed.title,
                    summary = parsed.summary,
                    actionItems = gson.toJson(parsed.actionItems),
                    keyPoints = gson.toJson(parsed.keyPoints)
                )
            )
        } catch (e: Exception) {
            summaryDao.insert(
                SummaryEntity(
                    id = UUID.randomUUID().toString(),
                    meetingId = meetingId,
                    title = "Error",
                    summary = "Failed to generate summary. Please retry.",
                    actionItems = "[]",
                    keyPoints = "[]"
                )
            )
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