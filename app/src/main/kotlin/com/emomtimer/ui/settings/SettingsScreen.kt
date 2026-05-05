package com.emomtimer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ListItem(
                headlineContent = { Text("Sound") },
                supportingContent = {
                    Text(
                        "Play beep at each interval (uses alarm audio stream, ignores silent mode)",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = viewModel::setSoundEnabled,
                    )
                },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Vibration") },
                supportingContent = {
                    Text(
                        "Vibrate at each interval and on workout completion",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = settings.vibrationEnabled,
                        onCheckedChange = viewModel::setVibrationEnabled,
                    )
                },
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()

            Spacer(modifier = Modifier.weight(1f))
            val uriHandler = LocalUriHandler.current
            val authorLink = buildAnnotatedString {
                append("Made by ")
                pushStringAnnotation(tag = "URL", annotation = "https://daniel-kindl.github.io/")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                ) { append("Daniel Kindl") }
                pop()
            }
            ClickableText(
                text = authorLink,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp),
                onClick = { offset ->
                    authorLink.getStringAnnotations("URL", offset, offset)
                        .firstOrNull()
                        ?.let { uriHandler.openUri(it.item) }
                },
            )
            Text(
                text = "v$versionName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
