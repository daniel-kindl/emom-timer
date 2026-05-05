# EMOM Timer

A minimal, production-quality Android workout interval timer — built for reliability during physical exercise.

[![Dev CI](https://github.com/daniel-kindl/emom-timer/actions/workflows/dev-ci.yml/badge.svg?branch=dev)](https://github.com/daniel-kindl/emom-timer/actions/workflows/dev-ci.yml)

---

## Features

- Set any **total workout duration** and **interval length** (mm:ss pickers)
- **Beep** at every interval boundary (uses alarm audio stream — ignores silent mode)
- **Vibration** at every interval and on completion
- Large, high-contrast UI designed for hands-free use mid-exercise
- Keeps screen on during sessions
- Sound and vibration individually toggleable

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

# Lint (detekt)
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

## Architecture

```
app/src/main/kotlin/com/emomtimer/
├── core/               Clock interface (injectable for tests)
├── domain/
│   ├── model/          TimerConfig, TimerEvent, UserSettings
│   ├── engine/         TimerEngine interface + drift-free impl
│   └── repository/     SettingsRepository interface
├── data/
│   ├── audio/          AudioPlayer (ToneGenerator/STREAM_ALARM)
│   ├── vibration/      VibrationManager
│   └── repository/     SettingsRepositoryImpl (DataStore)
├── di/                 Hilt AppModule
└── ui/
    ├── navigation/     AppNavigation (Compose Nav)
    ├── setup/          SetupScreen + ViewModel
    ├── session/        ActiveSessionScreen + ViewModel
    ├── settings/       SettingsScreen + ViewModel
    ├── components/     DurationPicker
    └── theme/          Material 3 theme
```

### Timer Engine

The engine anchors all interval times to the original `startTime` (system clock), computing boundaries as `startTime + N × intervalMillis`. Delays are recalculated on every iteration — this prevents drift and handles missed ticks safely.

### State flow

```
SetupScreen → (totalDurationMillis, intervalMillis) → SessionViewModel
                                                            │
                                                     TimerEngineImpl
                                                            │
                                                     TimerEvent (Flow)
                                                            │
                                          ┌─────────────────┘
                                          ▼
                                   SessionUiState (StateFlow)
                                          │
                                   ActiveSessionScreen
```

---

## Usage

1. Launch the app
2. Set **Total Duration** (e.g. 20:00)
3. Set **Interval** (e.g. 01:00)
4. Tap **START**
5. The app beeps + vibrates at every interval
6. Tap **STOP** at any time to end the session

Go to **Settings** (gear icon) to toggle sound or vibration.

---

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Concurrency**: Kotlin Coroutines + Flow
- **Persistence**: DataStore Preferences
- **Audio**: ToneGenerator (STREAM_ALARM)
- **CI/CD**: GitHub Actions
