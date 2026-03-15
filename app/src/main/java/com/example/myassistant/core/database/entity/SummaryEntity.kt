package com.example.myassistant.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summary")
data class SummaryEntity(
    @PrimaryKey val id : String,
    val meetingId : String,
    val title : String,
    val summary : String,
    val actionsItems : String,
    val keyPoints : String
)
