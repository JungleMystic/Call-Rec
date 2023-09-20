package com.lrm.voicerec.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_files_table")
data class AudioFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "file_name")
    val fileName: String = "",
    @ColumnInfo(name = "file_path")
    val filePath: String = "",
    @ColumnInfo(name = "duration")
    val duration: String = "",
)
