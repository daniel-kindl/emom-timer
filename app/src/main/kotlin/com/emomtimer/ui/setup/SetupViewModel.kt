package com.emomtimer.ui.setup

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SetupUiState(
    val totalMinutes: Int = 20,
    val totalSeconds: Int = 0,
    val intervalMinutes: Int = 1,
    val intervalSeconds: Int = 0,
) {
    val totalDurationMillis: Long
        get() = (totalMinutes * 60L + totalSeconds) * 1_000L

    val intervalMillis: Long
        get() = (intervalMinutes * 60L + intervalSeconds) * 1_000L

    val isValid: Boolean
        get() = totalDurationMillis > 0 && intervalMillis > 0

    /** True when interval exceeds total — no beeps will fire. */
    val intervalExceedsTotal: Boolean
        get() = isValid && intervalMillis > totalDurationMillis
}

@HiltViewModel
class SetupViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun setTotalMinutes(value: Int) = _uiState.update { it.copy(totalMinutes = value.coerceIn(0, 99)) }
    fun setTotalSeconds(value: Int) = _uiState.update { it.copy(totalSeconds = value.coerceIn(0, 59)) }
    fun setIntervalMinutes(value: Int) = _uiState.update { it.copy(intervalMinutes = value.coerceIn(0, 99)) }
    fun setIntervalSeconds(value: Int) = _uiState.update { it.copy(intervalSeconds = value.coerceIn(0, 59)) }
}
