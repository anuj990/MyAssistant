package com.example.myassistant.feature.recording

import android.Manifest
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myassistant.feature.recording.viewmodel.RecordingViewModel
import com.example.myassistant.ui.theme.AccentBlue
import com.example.myassistant.ui.theme.AccentRed
import com.example.myassistant.ui.theme.BackgroundDark
import com.example.myassistant.ui.theme.TextPrimary
import com.example.myassistant.ui.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordingScreen(
    onStop: () -> Unit,
    viewModel: RecordingViewModel = hiltViewModel(),
    formatTime: (Long) -> String
){
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    LaunchedEffect(Unit) {
        if (micPermission.status.isGranted) {
            viewModel.startRecording(context)
        } else {
            micPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(micPermission.status.isGranted) {
        if (micPermission.status.isGranted) {
            viewModel.startRecording(context)
        }
    }

    val isRecording = uiState is RecordingViewModel.RecordingUiState.Recording

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (uiState) {
                    is RecordingViewModel.RecordingUiState.Recording -> "Recording..."
                    is RecordingViewModel.RecordingUiState.Paused -> "Paused"
                    is RecordingViewModel.RecordingUiState.Stopped -> "Stopped"
                    else -> "Ready"
                },
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = formatTime(elapsedSeconds.toLong()),
                color = TextPrimary,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isRecording) 1.1f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        if (isRecording) AccentRed.copy(alpha = 0.2f)
                        else AccentBlue.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                IconButton(
                    onClick = {
                        if (isRecording) {
                            viewModel.stopRecording(context)
                            onStop()
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isRecording) AccentRed else AccentBlue,
                            CircleShape
                        )
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isRecording) "Tap to stop recording" else "Starting...",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}