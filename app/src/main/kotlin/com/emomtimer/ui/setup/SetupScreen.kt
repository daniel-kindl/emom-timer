package com.emomtimer.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emomtimer.domain.model.Preset
import com.emomtimer.ui.components.DurationPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onStartSession: (totalDurationMillis: Long, intervalMillis: Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var dialogPresetName by rememberSaveable { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Preset") },
            text = {
                OutlinedTextField(
                    value = dialogPresetName,
                    onValueChange = { dialogPresetName = it },
                    label = { Text("Preset name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.savePreset(dialogPresetName)
                        showSaveDialog = false
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            },
        )
    }

    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Delete Preset") },
            text = { Text("Delete \"${preset.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePreset(preset.id)
                        presetToDelete = null
                    },
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMOM Timer") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            DurationPicker(
                label = "Total Duration",
                minutes = state.totalMinutes,
                seconds = state.totalSeconds,
                onMinutesChange = viewModel::setTotalMinutes,
                onSecondsChange = viewModel::setTotalSeconds,
            )

            HorizontalDivider()

            DurationPicker(
                label = "Interval",
                minutes = state.intervalMinutes,
                seconds = state.intervalSeconds,
                onMinutesChange = viewModel::setIntervalMinutes,
                onSecondsChange = viewModel::setIntervalSeconds,
            )

            if (state.intervalExceedsTotal) {
                Text(
                    text = "⚠ Interval exceeds total duration — no beeps will fire.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            PresetsSection(
                presets = presets,
                onPresetClick = viewModel::loadPreset,
                onDeleteClick = { presetToDelete = it },
                onSavePreset = {
                    dialogPresetName = state.defaultPresetName()
                    showSaveDialog = true
                },
                saveEnabled = state.isValid,
            )

            Button(
                onClick = {
                    onStartSession(state.totalDurationMillis, state.intervalMillis)
                },
                enabled = state.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {
                Text("START", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetsSection(
    presets: List<Preset>,
    onPresetClick: (Preset) -> Unit,
    onDeleteClick: (Preset) -> Unit,
    onSavePreset: () -> Unit,
    saveEnabled: Boolean,
){
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Presets", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onSavePreset, enabled = saveEnabled) {
                Icon(
                    Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("Save")
            }
        }
        if (presets.isEmpty()) {
            Text(
                "No presets saved yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets, key = { it.id }) { preset ->
                    InputChip(
                        selected = false,
                        onClick = { onPresetClick(preset) },
                        label = { Text(preset.name) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onDeleteClick(preset) },
                                modifier = Modifier.size(20.dp),
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete ${preset.name}",
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
