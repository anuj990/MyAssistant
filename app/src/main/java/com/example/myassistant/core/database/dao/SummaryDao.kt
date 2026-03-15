package com.example.myassistant.core.database.dao

import androidx.compose.runtime.Stable
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myassistant.core.database.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity)

    @Update
   suspend fun update(summary : SummaryEntity)

    @Query("SELECT * FROM summaries WHERE meetingId = :meetingId")
    fun getSummaryForMeeting(meetingId: String): Flow<SummaryEntity?>

}