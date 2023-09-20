package com.lrm.voicerec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.lrm.voicerec.database.AudioFile
import com.lrm.voicerec.database.AudioFileDao
import kotlinx.coroutines.launch

class RecViewModel(
    private val audioFileDao: AudioFileDao
): ViewModel() {

    val getAll = audioFileDao.getAll().asLiveData()

    private fun insertFile(audioFile: AudioFile) {
        viewModelScope.launch {
            audioFileDao.insert(audioFile)
        }
    }

    fun addFile(fileName: String, filePath: String, duration: String) {
        val audioFile = AudioFile(fileName = fileName, filePath = filePath, duration = duration)
        insertFile(audioFile)
    }

    fun deleteFile(audioFile: AudioFile) {
        viewModelScope.launch {
            audioFileDao.delete(audioFile)
        }
    }

}

class RecViewModelFactory(private val audioFileDao: AudioFileDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecViewModel(audioFileDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}