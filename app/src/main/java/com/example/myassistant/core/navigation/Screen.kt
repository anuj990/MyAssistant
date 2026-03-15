package com.example.myassistant.core.navigation


sealed class Screen(val route : String){
    object Dashboard : Screen("dashboard")
    object Recording : Screen("recording")
    object Transcript : Screen("transcript/{meetingId}"){
        fun createRoute(meetingId: String) = "transcript/$meetingId"
    }
    object Summary : Screen("summary/{meetingId}"){
        fun createRoute(meetingId: String) = "summary/$meetingId"
    }
}