package com.emomtimer.core

fun interface Clock {
    fun currentTimeMillis(): Long
}

class SystemClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
