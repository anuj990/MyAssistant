package com.example.myassistant.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audioChunk")
data class AudioChunkEntity(
    @PrimaryKey val id : String,
    val meetingId: String,
    val filePath : String,
    val chunkIndex : Int,
    val transcriptionStatus : String
)
