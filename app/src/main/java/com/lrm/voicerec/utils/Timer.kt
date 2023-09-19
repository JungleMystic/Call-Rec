package com.lrm.voicerec.utils

import android.os.Handler
import android.os.Looper

class Timer(listener: OnTimeTickListener) {

    interface OnTimeTickListener {
        fun onTimerTick(duration: String)
    }

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var duration = 0L
    private var delay = 100L

    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerTick(formattedString())
        }
    }

    fun startTimer() {
        handler.postDelayed(runnable, delay)

    }

    fun pauseTimer() {
        handler.removeCallbacks(runnable)
    }

    fun stopTimer() {
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    private fun formattedString(): String {
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60))

        return if (hours > 0)
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }
}