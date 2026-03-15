package com.example.myassistant.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myassistant.core.database.dao.AudioChunkDao
import com.example.myassistant.core.database.dao.MeetingDao
import com.example.myassistant.core.database.dao.SummaryDao
import com.example.myassistant.core.database.dao.TranscriptDao
import com.example.myassistant.core.database.entity.AudioChunkEntity
import com.example.myassistant.core.database.entity.MeetingEntity
import com.example.myassistant.core.database.entity.SummaryEntity
import com.example.myassistant.core.database.entity.TranscriptEntity

@Database(
    entities = [AudioChunkEntity::class, MeetingEntity::class, SummaryEntity::class, TranscriptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase(){
    abstract fun audioChunkDao(): AudioChunkDao
    abstract fun meetingDao(): MeetingDao
    abstract fun summaryDao(): SummaryDao
    abstract fun transcriptDao(): TranscriptDao
}