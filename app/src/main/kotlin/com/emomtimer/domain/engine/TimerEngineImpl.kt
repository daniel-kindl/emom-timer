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
 * never by accumulating delays. If the system is lagged, missed interval events
 * are emitted in one burst before the next delay is calculated.
 *
 * Pause/resume works by tracking total accumulated pause duration and subtracting
 * it from the elapsed time, preserving drift-free behaviour across pauses.
 */
class TimerEngineImpl(
    private val clock: Clock,
    private val scope: CoroutineScope,
) : TimerEngine {

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<TimerEvent> = _events

    private var job: Job? = null

    // Volatile fields: written on the calling thread, read on the timer coroutine thread.
    // @Volatile guarantees visibility; write ordering in pause()/resume() ensures correctness.
    @Volatile private var isPaused = false
    @Volatile private var pauseStartTime = 0L
    @Volatile private var totalPausedMs = 0L

    override fun start(config: TimerConfig) {
        job?.cancel()
        isPaused = false
        pauseStartTime = 0L
        totalPausedMs = 0L
        job = scope.launch {
            val startTime = clock.currentTimeMillis()
            val totalIntervals = ceil(
                config.totalDurationMillis.toDouble() / config.intervalMillis
            ).toInt()
            var lastCompletedInterval = 0

            while (isActive) {
                // Suspend cheaply while paused; re-check on every tick.
                while (isPaused && isActive) {
                    delay(PAUSE_CHECK_MS)
                }
                if (!isActive) break

                val now = clock.currentTimeMillis()
                val elapsed = now - startTime - totalPausedMs

                val completedIntervals = (elapsed / config.intervalMillis).toInt()

                // Emit interval events before the completion check so that the final
                // interval always fires even when it coincides with totalDurationMillis.
                for (i in (lastCompletedInterval + 1)..completedIntervals) {
                    _events.emit(TimerEvent.IntervalCompleted(i))
                }
                lastCompletedInterval = completedIntervals

                if (elapsed >= config.totalDurationMillis) {
                    _events.emit(TimerEvent.WorkoutCompleted)
                    return@launch
                }

                val nextIntervalAt = startTime + totalPausedMs + (completedIntervals + 1) * config.intervalMillis
                val remainingInInterval = (nextIntervalAt - now).coerceAtLeast(0L)

                _events.emit(
                    TimerEvent.Tick(
                        elapsedMillis = elapsed,
                        remainingInInterval = remainingInInterval,
                        currentInterval = completedIntervals + 1,
                        totalIntervals = totalIntervals,
                    )
                )

                val workoutEnd = startTime + totalPausedMs + config.totalDurationMillis
                val nextUiTick = now + TICK_MS
                val sleepUntil = minOf(nextIntervalAt, nextUiTick, workoutEnd)
                val sleepMs = (sleepUntil - clock.currentTimeMillis()).coerceAtLeast(0L)
                if (sleepMs > 0) delay(sleepMs)
            }
        }
    }

    override fun pause() {
        if (!isPaused) {
            pauseStartTime = clock.currentTimeMillis()
            isPaused = true
        }
    }

    override fun resume() {
        if (isPaused) {
            // Update totalPausedMs BEFORE clearing isPaused so the timer loop
            // sees the correct offset as soon as it exits the pause-check loop.
            totalPausedMs += clock.currentTimeMillis() - pauseStartTime
            isPaused = false
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    private companion object {
        const val TICK_MS = 100L
        const val PAUSE_CHECK_MS = 50L
    }
}
