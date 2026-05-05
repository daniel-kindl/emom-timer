package com.emomtimer.domain.engine

import com.emomtimer.core.Clock
import com.emomtimer.domain.model.TimerConfig
import com.emomtimer.domain.model.TimerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil

/**
 * Drift-free timer engine based on system clock anchoring.
 *
 * All interval boundaries are calculated as absolute timestamps from [startTime],
 * never by accumulating delays.  If the system is lagged, missed interval events
 * are emitted in one burst before the next delay is calculated.
 */
class TimerEngineImpl(
    private val clock: Clock,
    private val scope: CoroutineScope,
) : TimerEngine {

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<TimerEvent> = _events

    private var job: Job? = null

    override fun start(config: TimerConfig) {
        job?.cancel()
        job = scope.launch {
            val startTime = clock.currentTimeMillis()
            val totalIntervals = ceil(
                config.totalDurationMillis.toDouble() / config.intervalMillis
            ).toInt()
            var lastCompletedInterval = 0

            while (isActive) {
                val now = clock.currentTimeMillis()
                val elapsed = now - startTime

                val completedIntervals = (elapsed / config.intervalMillis).toInt()

                // Emit interval events before the completion check so that the final
                // interval always fires even when it coincides with totalDurationMillis.
                for (i in (lastCompletedInterval + 1)..completedIntervals) {
                    _events.emit(TimerEvent.IntervalCompleted(i))
                }
                lastCompletedInterval = completedIntervals

                if (elapsed >= config.totalDurationMillis) {
                    _events.emit(TimerEvent.WorkoutCompleted)
                    break
                }

                // Countdown to the next interval boundary
                val nextIntervalAt = startTime + (completedIntervals + 1) * config.intervalMillis
                val remainingInInterval = (nextIntervalAt - now).coerceAtLeast(0L)

                _events.emit(
                    TimerEvent.Tick(
                        elapsedMillis = elapsed,
                        remainingInInterval = remainingInInterval,
                        currentInterval = completedIntervals + 1,
                        totalIntervals = totalIntervals,
                    )
                )

                // Sleep until the sooner of: next interval boundary or next UI tick
                val nextUiTick = now + TICK_MS
                val sleepUntil = minOf(nextIntervalAt, nextUiTick, startTime + config.totalDurationMillis)
                val sleepMs = (sleepUntil - clock.currentTimeMillis()).coerceAtLeast(0L)
                if (sleepMs > 0) delay(sleepMs)
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    private companion object {
        const val TICK_MS = 100L
    }
}
