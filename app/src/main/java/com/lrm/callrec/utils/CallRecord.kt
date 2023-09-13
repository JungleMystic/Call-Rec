package com.lrm.callrec.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import kotlin.random.Random

class CallRecord(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private val mediaPlayer = MediaPlayer()

    private var outputPath = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .toString() + "/CallRec/myRec${getRandomInt()}.m4a"

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    fun startRecording() {
        outputPath = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + "/CallRec/myRec${getRandomInt()}.m4a"

        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputPath)
            prepare()
            start()
            recorder = this
        }
    }

    fun stopRecording() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }

    fun stopPlaying() {
        mediaPlayer.stop()
    }

    fun playRecording() {
        mediaPlayer.apply {
            setDataSource(outputPath)
            prepare()
            start()
        }
    }

    private fun getRandomInt(): Int = Random.nextInt(1000, 10000)
}