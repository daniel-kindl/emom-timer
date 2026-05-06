# DK Timer

A minimal, production-quality Android workout interval timer — built for reliability during physical exercise.

[![Dev CI](https://github.com/daniel-kindl/emom-timer/actions/workflows/dev-ci.yml/badge.svg?branch=dev)](https://github.com/daniel-kindl/emom-timer/actions/workflows/dev-ci.yml)

---

## Features

| Feature | Detail |
|---------|--------|
| ⏱ EMOM timer | Set total duration and interval length (mm:ss drum-roll pickers) |
| 🔁 Tabata timer | Set total, work, and rest durations; automatic phase alternation |
| 🔴🟢 Phase colours | Full-screen red (work) / green (rest) background in Tabata sessions |
| 🔔 Sound feedback | Beep at every interval — uses alarm stream, ignores silent mode |
| 📳 Vibration feedback | Vibrates at every interval and on workout completion |
| ⏸ Pause & resume | Pause mid-session without losing progress or drifting |
| 💾 Presets | Save, name, and load your favourite configurations for both timers |
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

### Starting a timer

1. Launch the app — you'll see the **DK Timer** home screen
2. Tap **EMOM** or **Tabata** to open that timer's setup screen

### EMOM

1. Set **Total Duration** and **Interval** using the drum-roll pickers
2. Tap **START** — the app beeps + vibrates at every interval boundary
3. Tap **PAUSE** to freeze mid-session; tap **RESUME** to continue
4. Tap **STOP** at any time to end early

### Tabata

1. Set **Total Duration**, **Work Time**, and **Rest Time**
2. Tap **START** — phases alternate automatically with distinct high/low beeps
3. Background turns red during work phases, green during rest
4. Tap **PAUSE** / **RESUME** to freeze and continue; tap **STOP** to end early

### Presets

- Tap **Save** in the Presets row on either setup screen to save the current config
- A name is auto-generated — edit it in the dialog if you prefer
- Tap any preset chip to instantly load those values
- Tap ✕ on a chip and confirm to delete a preset

### Settings

Tap the gear icon ⚙ on the **home screen** to toggle sound or vibration independently.

---

## Architecture

```
app/src/main/kotlin/com/emomtimer/
├── core/               Clock interface (injectable for deterministic tests)
├── domain/
│   ├── model/          TimerConfig, TimerEvent, SessionStatus, Preset
│   │                   TabataConfig, TabataEvent, TabataPreset
│   ├── engine/         AbstractPausableEngine (base), TimerEngine + impl,
│   │                   TabataEngine + impl + factory
│   └── repository/     SettingsRepository, PresetRepository,
│                       TabataPresetRepository interfaces
├── data/
│   ├── audio/          AudioPlayer (ToneGenerator / STREAM_ALARM)
│   ├── vibration/      VibrationManager
│   └── repository/     SettingsRepositoryImpl, PresetRepositoryImpl,
│                       TabataPresetRepositoryImpl (DataStore)
├── di/                 Hilt AppModule
└── ui/
    ├── navigation/     AppNavigation (Compose Nav)
    ├── home/           HomeScreen (timer type selection + settings entry)
    ├── setup/          SetupScreen + ViewModel  (EMOM pickers, presets)
    ├── session/        ActiveSessionScreen + ViewModel  (timer display, pause/stop)
    ├── tabata/
    │   ├── setup/      TabataSetupScreen + ViewModel
    │   └── session/    TabataSessionScreen + ViewModel
    ├── settings/       SettingsScreen + ViewModel
    ├── components/     DurationPicker, WheelPicker, PresetsSection
    └── theme/          Material 3 theme (complete 15-slot typography)
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
