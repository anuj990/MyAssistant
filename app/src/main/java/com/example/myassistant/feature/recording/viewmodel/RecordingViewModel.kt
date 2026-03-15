package com.example.myassistant.feature.recording.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myassistant.feature.recording.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val repository: RecordingRepository
) : ViewModel() {

    sealed class RecordingUiState {
        object Idle : RecordingUiState()
        object Recording : RecordingUiState()
        object Paused : RecordingUiState()
        object Stopped : RecordingUiState()
    }

    private val _uiState = MutableStateFlow<RecordingUiState>(RecordingUiState.Idle)
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    val meetings = repository.getAllMeetings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun startRecording(context: Context) {
        repository.startRecording(context)
        _uiState.value = RecordingUiState.Recording
        startTimer()
    }

    fun stopRecording(context: Context) {
        repository.stopRecording(context)
        _uiState.value = RecordingUiState.Stopped
        stopTimer()
    }

    private fun startTimer() {
        _elapsedSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _elapsedSeconds.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}