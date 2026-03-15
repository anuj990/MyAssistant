package com.example.myassistant.core.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            Text("Dashboard")
        }

        composable(Screen.Recording.route) {
            Text("Recording")
        }

        composable(Screen.Transcript.route) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            Text("Transcript $meetingId")
        }

        composable(Screen.Summary.route) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            Text("Summary $meetingId")
        }
    }
}