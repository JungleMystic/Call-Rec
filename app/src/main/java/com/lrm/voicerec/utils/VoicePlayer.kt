package com.lrm.voicerec.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri

class VoicePlayer(private val context: Context) {

    private var player: MediaPlayer? = null

    fun startPlaying(filePath: String) {
        MediaPlayer.create(context, filePath.toUri()).apply {
            player = this
            start()
        }
    }

    fun stopPlaying() {
        player?.stop()
        player?.release()
        player = null
    }
}