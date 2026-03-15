package com.example.myassistant.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myassistant.feature.dashboard.DashboardScreen
import com.example.myassistant.feature.recording.RecordingScreen
import com.example.myassistant.feature.summary.SummaryScreen
import com.example.myassistant.feature.transcript.TranscriptScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onMeetingClick = { meetingId ->
                    navController.navigate(Screen.Summary.createRoute(meetingId))
                },
                onRecordClick = {
                    navController.navigate(Screen.Recording.route)
                }
            )
        }

        composable(Screen.Recording.route) {
            RecordingScreen(
                onStop = { navController.popBackStack() },
                viewModel = hiltViewModel(),
                formatTime = { seconds ->
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    "%02d:%02d".format(minutes, remainingSeconds)
                }
            )
        }

        composable(Screen.Transcript.route) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            TranscriptScreen(meetingId = meetingId)
        }

        composable(Screen.Summary.route) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            SummaryScreen(meetingId = meetingId)
        }
    }
}