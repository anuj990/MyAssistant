package com.example.myassistant.core.database

import android.content.Context
import androidx.room.Room
import com.example.myassistant.core.database.dao.AudioChunkDao
import com.example.myassistant.core.database.dao.MeetingDao
import com.example.myassistant.core.database.dao.SummaryDao
import com.example.myassistant.core.database.dao.TranscriptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context : Context): AppDatabase{
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_assistant_db"
        ).build()
    }
    @Provides
    fun provideMeetingDao(db: AppDatabase): MeetingDao = db.meetingDao()
    @Provides
    fun provideAudioChunkDao(db: AppDatabase): AudioChunkDao = db.audioChunkDao()
    @Provides
    fun provideSummaryDao(db: AppDatabase): SummaryDao = db.summaryDao()
    @Provides
    fun provideTranscriptDao(db: AppDatabase): TranscriptDao = db.transcriptDao()
}