package com.example.myassistant.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val meetingId: String,
    val title: String = "",
    val summary: String = "",
    val actionItems: String = "",
    val keyPoints: String = ""
)