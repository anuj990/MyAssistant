package com.example.myassistant.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myassistant.core.database.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcript: TranscriptEntity)

    @Query("SELECT * FROM transcripts WHERE meetingId = :meetingId ORDER BY chunkOrder ASC")
    fun getTranscriptForMeeting(meetingId: String): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcripts WHERE meetingId = :meetingId ORDER BY chunkOrder ASC")
    suspend fun getTranscriptOnce(meetingId: String): List<TranscriptEntity>
}