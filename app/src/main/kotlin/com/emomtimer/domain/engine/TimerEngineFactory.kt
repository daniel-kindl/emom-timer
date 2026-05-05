package com.emomtimer.domain.engine

import com.emomtimer.core.Clock
import kotlinx.coroutines.CoroutineScope

fun interface TimerEngineFactory {
    fun create(scope: CoroutineScope): TimerEngine
}

class DefaultTimerEngineFactory(private val clock: Clock) : TimerEngineFactory {
    override fun create(scope: CoroutineScope): TimerEngine = TimerEngineImpl(clock, scope)
}
