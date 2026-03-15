package com.example.myassistant.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myassistant.core.database.entity.AudioChunkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioChunkDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioChunk: AudioChunkEntity)

    @Update
    suspend fun update(audioChunk: AudioChunkEntity)

    @Query("SELECT * FROM audioChunk ORDER BY chunkIndex")
    fun getChunksForMeeting(meetingId: String): List<AudioChunkEntity>

    @Query("SELECT * FROM audioChunk WHERE transcriptionStatus  = 'PENDING'")
    suspend fun getPendingChunks(): List<AudioChunkEntity>
}