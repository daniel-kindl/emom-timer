package com.emomtimer.domain.engine

import com.emomtimer.domain.model.TimerConfig
import com.emomtimer.domain.model.TimerEvent
import kotlinx.coroutines.flow.SharedFlow

interface TimerEngine {
    val events: SharedFlow<TimerEvent>
    fun start(config: TimerConfig)
    fun pause()
    fun resume()
    fun stop()
}
