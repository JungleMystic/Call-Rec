package com.lrm.voicerec.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioFileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(audioFile: AudioFile)

    @Query("SELECT * FROM audio_files_table ORDER BY id DESC")
    fun getAll(): Flow<List<AudioFile>>

    @Update
    suspend fun update(audioFile: AudioFile)

    @Delete
    suspend fun delete(audioFile: AudioFile)

    @Query("SELECT * FROM audio_files_table WHERE id = :id")
    fun getAudioFile(id: Int): Flow<AudioFile>

}