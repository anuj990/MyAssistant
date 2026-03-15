package com.example.myassistant.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcripts")
data class TranscriptEntity(
    @PrimaryKey val id : String,
    val meetingId : String,
    val chunkId : String,
    val text : String,
    val chunkOrder : String
)
