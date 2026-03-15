package com.example.myassistant.feature.transcript.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myassistant.core.database.dao.TranscriptDao
import com.example.myassistant.core.database.entity.TranscriptEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranscriptViewModel @Inject constructor(
    private val transcriptDao: TranscriptDao
) : ViewModel() {

    private val _transcript = MutableStateFlow<List<TranscriptEntity>>(emptyList())
    val transcript: StateFlow<List<TranscriptEntity>> = _transcript.asStateFlow()

    fun loadTranscript(meetingId: String) {
        viewModelScope.launch {
            transcriptDao.getTranscriptForMeeting(meetingId)
                .collect { _transcript.value = it }
        }
    }
}