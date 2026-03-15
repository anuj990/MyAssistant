package com.example.myassistant.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myassistant.core.database.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert (meeting : MeetingEntity)

    @Update
    suspend fun update(meeting: MeetingEntity)

    @Query("SELECT * FROM meetings ORDER BY startingTime DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE id = :id")
    fun getMeetingById(id : String) : MeetingEntity?
}