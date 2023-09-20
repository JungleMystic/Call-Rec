package com.lrm.voicerec.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AudioFile::class], version = 1, exportSchema = false)
abstract class AudioRoomDatabase: RoomDatabase() {

    abstract fun audioFileDao(): AudioFileDao

    companion object {
        @Volatile
        private var INSTANCE: AudioRoomDatabase? = null

        fun getDatabase(context: Context): AudioRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioRoomDatabase::class.java,
                    "audio_files_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}