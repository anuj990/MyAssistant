package com.example.myassistant.feature.summary


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myassistant.feature.summary.viewmodel.SummaryViewModel
import com.example.myassistant.ui.theme.AccentBlue
import com.example.myassistant.ui.theme.BackgroundDark
import com.example.myassistant.ui.theme.SurfaceDark
import com.example.myassistant.ui.theme.TextPrimary
import com.example.myassistant.ui.theme.TextSecondary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun SummaryScreen(
    meetingId: String,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(meetingId) {
        viewModel.loadSummary(meetingId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Summary",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is SummaryViewModel.SummaryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Generating summary...", color = TextSecondary)
                    }
                }
            }

            is SummaryViewModel.SummaryUiState.Success -> {
                val summary = state.summary
                val gson = Gson()
                val actionItems: List<String> = gson.fromJson(
                    summary.actionItems,
                    object : TypeToken<List<String>>() {}.type
                )

                val keyPoints: List<String> = gson.fromJson(
                    summary.keyPoints,
                    object : TypeToken<List<String>>() {}.type
                )
                SummarySection(
                    title = "Title",
                    content = summary.title,
                    icon = Icons.Default.Title
                )
                SummarySection(
                    title = "Summary",
                    content = summary.summary,
                    icon = Icons.Default.Notes
                )
                SummaryListSection(
                    title = "Action Items",
                    items = actionItems,
                    icon = Icons.Default.CheckCircle
                )
                SummaryListSection(
                    title = "Key Points",
                    items = keyPoints,
                    icon = Icons.Default.Star
                )
            }

            is SummaryViewModel.SummaryUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadSummary(meetingId) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
fun SummarySection(title: String, content: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = AccentBlue, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SummaryListSection(title: String, items: List<String>, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = AccentBlue, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ", color = AccentBlue)
                    Text(item, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}