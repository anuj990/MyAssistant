package com.example.myassistant.feature.summary.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myassistant.core.database.entity.SummaryEntity
import com.example.myassistant.feature.summary.repository.SummaryRepository
import com.example.myassistant.feature.transcript.repository.TranscriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val transcriptRepository: TranscriptRepository
) : ViewModel() {

    sealed class SummaryUiState {
        object Idle : SummaryUiState()
        object Loading : SummaryUiState()
        data class Success(val summary: SummaryEntity) : SummaryUiState()
        data class Error(val message: String) : SummaryUiState()
    }

    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

//    fun loadSummary(meetingId: String) {
//        viewModelScope.launch {
//            _uiState.value = SummaryUiState.Loading
//            // first transcribe chunks
//            transcriptRepository.transcribeChunks(meetingId)
//            // then generate summary
//            summaryRepository.generateSummary(meetingId)
//            // then observe result
//            summaryRepository.getSummary(meetingId).collect { summary ->
//                if (summary != null) {
//                    _uiState.value = SummaryUiState.Success(summary)
//                } else {
//                    _uiState.value = SummaryUiState.Error("Failed to generate summary")
//                }
//            }
//        }
//    }
    fun loadSummary(meetingId: String) {
        viewModelScope.launch {
            _uiState.value = SummaryUiState.Loading

            // DEBUG - check chunks exist
            val chunks = transcriptRepository.transcribeChunks(meetingId)
            android.util.Log.d("SummaryDebug", "Starting transcription for $meetingId")

            transcriptRepository.transcribeChunks(meetingId)
            android.util.Log.d("SummaryDebug", "Transcription done, generating summary")

            summaryRepository.generateSummary(meetingId)
            android.util.Log.d("SummaryDebug", "Summary generation done")

            summaryRepository.getSummary(meetingId).collect { summary ->
                android.util.Log.d("SummaryDebug", "Summary received: $summary")
                if (summary != null) {
                    _uiState.value = SummaryUiState.Success(summary)
                } else {
                    _uiState.value = SummaryUiState.Error("Failed to generate summary")
                }
            }
        }
    }
}