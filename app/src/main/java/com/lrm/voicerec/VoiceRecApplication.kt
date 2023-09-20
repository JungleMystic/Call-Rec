package com.lrm.voicerec

import android.app.Application
import com.lrm.voicerec.database.AudioRoomDatabase

class VoiceRecApplication: Application() {

    val database: AudioRoomDatabase by lazy {
        AudioRoomDatabase.getDatabase(this)
    }
}