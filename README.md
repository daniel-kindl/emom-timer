# EMOM Timer

A minimal, production-quality Android workout interval timer — built for reliability during physical exercise.

[![Dev CI](https://github.com/daniel-kindl/emom-timer/actions/workflows/dev-ci.yml/badge.svg?branch=dev)](https://github.com/daniel-kindl/emom-timer/actions/workflows/dev-ci.yml)

---

## Features

| Feature | Detail |
|---------|--------|
| ⏱ Flexible timing | Set any total duration and interval length (mm:ss pickers) |
| 🔔 Sound feedback | Beep at every interval — uses alarm stream, ignores silent mode |
| 📳 Vibration feedback | Vibrates at every interval and on workout completion |
| ⏸ Pause & resume | Pause mid-session without losing progress or drifting |
| 💾 Presets | Save, name, and load your favourite interval configurations |
| 🖥 Workout-first UI | Large high-contrast display, screen stays on, one-hand friendly |
| 🔇 Toggleable feedback | Sound and vibration each independently on/off |

---

## Screenshots

> _Coming soon_

---

## Setup

### Requirements

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog 2023.1+ |
| JDK | 17 |
| Min Android SDK | 26 (Android 8.0) |
| Target SDK | 34 |

### Build

```bash
# Debug build
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Static analysis (detekt)
./gradlew detekt
```

### Release signing

Create `keystore.properties` in the project root (not committed):

```properties
storeFile=/path/to/keystore.jks
storePassword=yourStorePassword
keyAlias=yourKeyAlias
keyPassword=yourKeyPassword
```

Or set environment variables `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (used by CI/CD).

---

## Usage

### Running a workout

1. Launch the app
2. Set **Total Duration** (e.g. `20:00`)
3. Set **Interval** (e.g. `01:00`)
4. Tap **START**
5. The app beeps + vibrates at every interval boundary
6. Tap **PAUSE** to freeze the timer mid-session; tap **RESUME** to continue
7. Tap **STOP** at any time to end the session early

### Presets

- Tap **Save** in the Presets section on the setup screen to save the current configuration
- A name is auto-generated (e.g. `20min / 1min`) — edit it in the dialog if you prefer
- Tap any preset chip to instantly load those values into the pickers
- Tap ✕ on a chip and confirm to delete a preset

### Settings

Tap the gear icon (⚙) on the setup screen to toggle sound or vibration independently.

---

## Architecture

```
app/src/main/kotlin/com/emomtimer/
├── core/               Clock interface (injectable for deterministic tests)
├── domain/
│   ├── model/          TimerConfig, TimerEvent, UserSettings, Preset
│   ├── engine/         TimerEngine interface + drift-free impl (pause-safe)
│   └── repository/     SettingsRepository, PresetRepository interfaces
├── data/
│   ├── audio/          AudioPlayer (ToneGenerator / STREAM_ALARM)
│   ├── vibration/      VibrationManager
│   └── repository/     SettingsRepositoryImpl, PresetRepositoryImpl (DataStore)
├── di/                 Hilt AppModule
└── ui/
    ├── navigation/     AppNavigation (Compose Nav)
    ├── setup/          SetupScreen + ViewModel  (pickers, presets)
    ├── session/        ActiveSessionScreen + ViewModel  (timer display, pause/stop)
    ├── settings/       SettingsScreen + ViewModel
    ├── components/     DurationPicker
    └── theme/          Material 3 theme
```

### Timer engine

All interval boundaries are anchored to `startTime` (system clock) and computed as `startTime + N × intervalMillis`. Delays are recalculated on every iteration — this prevents drift and handles missed ticks safely.

**Pause/resume** works by accumulating total paused duration and subtracting it from elapsed time:

```
effectiveElapsed = now - startTime - totalPausedMs
```

This preserves drift-free accuracy across any number of pauses.

### State flow

```
SetupScreen ──(totalDurationMillis, intervalMillis)──► SessionViewModel
                                                              │
                                                       TimerEngineImpl
                                                              │
                                                       TimerEvent (Flow)
                                                              │
                                          ┌───────────────────┘
                                          ▼
                                   SessionUiState (StateFlow)
                                          │
                                   ActiveSessionScreen
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Concurrency | Kotlin Coroutines + Flow |
| Persistence | DataStore Preferences |
| Audio | ToneGenerator (STREAM_ALARM) |
| CI/CD | GitHub Actions |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch rules, commit conventions, and the release process.
