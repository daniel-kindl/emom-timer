package com.emomtimer.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emomtimer.data.audio.AudioPlayer
import com.emomtimer.data.vibration.VibrationManager
import com.emomtimer.domain.engine.TimerEngineFactory
import com.emomtimer.domain.model.SessionStatus
import com.emomtimer.domain.model.TimerConfig
import com.emomtimer.domain.model.TimerEvent
import com.emomtimer.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val status: SessionStatus = SessionStatus.Running,
    val currentRound: Int = 1,
    val totalRounds: Int = 0,
    val elapsedMillis: Long = 0L,
    val remainingInIntervalMillis: Long = 0L,
    val totalDurationMillis: Long = 0L,
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val timerEngineFactory: TimerEngineFactory,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: AudioPlayer,
    private val vibrationManager: VibrationManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val totalDurationMillis: Long =
        checkNotNull(savedStateHandle["totalDurationMillis"])
    private val intervalMillis: Long =
        checkNotNull(savedStateHandle["intervalMillis"])

    private val timerEngine = timerEngineFactory.create(viewModelScope)

    private val _uiState = MutableStateFlow(
        SessionUiState(totalDurationMillis = totalDurationMillis)
    )
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
        timerEngine.start(
            TimerConfig(
                intervalMillis = intervalMillis,
                totalDurationMillis = totalDurationMillis,
            )
        )
    }

    private fun observeEvents() {
        viewModelScope.launch {
            timerEngine.events.collect { event ->
                when (event) {
                    is TimerEvent.Tick -> _uiState.update {
                        it.copy(
                            status = SessionStatus.Running,
                            elapsedMillis = event.elapsedMillis,
                            remainingInIntervalMillis = event.remainingInInterval,
                            currentRound = event.currentInterval,
                            totalRounds = event.totalIntervals,
                        )
                    }

                    is TimerEvent.IntervalCompleted -> triggerFeedback(isCompletion = false)

                    is TimerEvent.WorkoutCompleted -> {
                        triggerFeedback(isCompletion = true)
                        _uiState.update { it.copy(status = SessionStatus.Completed) }
                    }
                }
            }
        }
    }

    private suspend fun triggerFeedback(isCompletion: Boolean) {
        val settings = settingsRepository.getSettings().first()
        if (settings.soundEnabled) {
            if (isCompletion) audioPlayer.playCompletionSound() else audioPlayer.playIntervalBeep()
        }
        if (settings.vibrationEnabled) {
            if (isCompletion) vibrationManager.vibrateCompletion() else vibrationManager.vibrateInterval()
        }
    }

    fun pauseSession() {
        timerEngine.pause()
        _uiState.update { it.copy(status = SessionStatus.Paused) }
    }

    fun resumeSession() {
        timerEngine.resume()
        _uiState.update { it.copy(status = SessionStatus.Running) }
    }

    fun stopSession() {
        timerEngine.stop()
        _uiState.update { it.copy(status = SessionStatus.Stopped) }
    }

    override fun onCleared() {
        super.onCleared()
        timerEngine.stop()
        audioPlayer.release()
    }
}
