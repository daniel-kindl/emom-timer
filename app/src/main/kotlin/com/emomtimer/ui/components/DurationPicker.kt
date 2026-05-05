package com.emomtimer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * MM:SS duration picker with large +/- touch targets.
 * Minutes: 0–99, Seconds: 0–59.
 */
@Composable
fun DurationPicker(
    label: String,
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeUnitStepper(
                value = minutes,
                label = "min",
                contentDescPrefix = "$label minutes",
                range = 0..99,
                onChange = onMinutesChange,
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TimeUnitStepper(
                value = seconds,
                label = "sec",
                contentDescPrefix = "$label seconds",
                range = 0..59,
                onChange = onSecondsChange,
            )
        }
    }
}

@Composable
private fun TimeUnitStepper(
    value: Int,
    label: String,
    contentDescPrefix: String,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = { if (value < range.last) onChange(value + 1) },
            modifier = Modifier
                .size(56.dp)
                .semantics { contentDescription = "Increase $contentDescPrefix" },
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
        Spacer(Modifier.height(4.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d".format(value),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        FilledIconButton(
            onClick = { if (value > range.first) onChange(value - 1) },
            modifier = Modifier
                .size(56.dp)
                .semantics { contentDescription = "Decrease $contentDescPrefix" },
        ) {
            Icon(Icons.Default.Remove, contentDescription = null)
        }
        Spacer(Modifier.width(8.dp))
    }
}
