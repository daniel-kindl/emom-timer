package com.emomtimer.ui.tabata.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emomtimer.data.audio.AudioPlayer
import com.emomtimer.data.vibration.VibrationManager
import com.emomtimer.domain.engine.TabataEngineFactory
import com.emomtimer.domain.model.SessionStatus
import com.emomtimer.domain.model.TabataConfig
import com.emomtimer.domain.model.TabataEvent
import com.emomtimer.domain.model.TabataPhase
import com.emomtimer.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TabataSessionUiState(
    val status: SessionStatus = SessionStatus.Running,
    val phase: TabataPhase = TabataPhase.Work,
    val remainingInPhaseMillis: Long = 0L,
    val elapsedMillis: Long = 0L,
    val totalDurationMillis: Long = 0L,
)

@HiltViewModel
class TabataSessionViewModel @Inject constructor(
    private val engineFactory: TabataEngineFactory,
    private val settingsRepository: SettingsRepository,
    private val audioPlayer: AudioPlayer,
    private val vibrationManager: VibrationManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val totalDurationMillis: Long = checkNotNull(savedStateHandle["totalDurationMillis"])
    private val workMillis: Long = checkNotNull(savedStateHandle["workMillis"])
    private val restMillis: Long = checkNotNull(savedStateHandle["restMillis"])

    private val engine = engineFactory.create(viewModelScope)

    private val _uiState = MutableStateFlow(
        TabataSessionUiState(totalDurationMillis = totalDurationMillis)
    )
    val uiState: StateFlow<TabataSessionUiState> = _uiState.asStateFlow()

    init {
        observeEvents()
        engine.start(TabataConfig(workMillis, restMillis, totalDurationMillis))
    }

    private fun observeEvents() {
        viewModelScope.launch {
            engine.events.collect { event ->
                when (event) {
                    is TabataEvent.Tick -> _uiState.update {
                        it.copy(
                            status = SessionStatus.Running,
                            phase = event.phase,
                            remainingInPhaseMillis = event.remainingInPhaseMillis,
                            elapsedMillis = event.elapsedMillis,
                        )
                    }

                    is TabataEvent.WorkStarted -> triggerFeedback(
                        isCompletion = false,
                        playSound = { audioPlayer.playWorkStartBeep() },
                    )

                    is TabataEvent.RestStarted -> triggerFeedback(
                        isCompletion = false,
                        playSound = { audioPlayer.playRestStartBeep() },
                    )

                    is TabataEvent.WorkoutCompleted -> {
                        triggerFeedback(
                            isCompletion = true,
                            playSound = { audioPlayer.playCompletionSound() },
                        )
                        _uiState.update { it.copy(status = SessionStatus.Completed) }
                    }
                }
            }
        }
    }

    private suspend fun triggerFeedback(isCompletion: Boolean, playSound: () -> Unit) {
        val settings = settingsRepository.getSettings().first()
        if (settings.soundEnabled) playSound()
        if (settings.vibrationEnabled) {
            if (isCompletion) vibrationManager.vibrateCompletion() else vibrationManager.vibrateInterval()
        }
    }

    fun pauseSession() {
        engine.pause()
        _uiState.update { it.copy(status = SessionStatus.Paused) }
    }

    fun resumeSession() {
        engine.resume()
        _uiState.update { it.copy(status = SessionStatus.Running) }
    }

    fun stopSession() {
        engine.stop()
        _uiState.update { it.copy(status = SessionStatus.Stopped) }
    }

    override fun onCleared() {
        super.onCleared()
        engine.stop()
        audioPlayer.release()
    }
}
