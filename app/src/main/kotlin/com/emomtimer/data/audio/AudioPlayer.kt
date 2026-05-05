package com.emomtimer.data.audio

interface AudioPlayer {
    fun playIntervalBeep()
    fun playCompletionSound()
    fun release()
}
