# Changelog

All notable changes to DK Timer are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

---

## [2.0.0] - 2026-05-06

### Added
- **Tabata timer**: configure total duration, work interval, and rest interval; automatic
  work/rest phase alternation with distinct high/low audio beeps per phase
- **Full-screen phase backgrounds**: animated red (work) / green (rest) background in
  Tabata session; colours dim when paused
- **Tabata presets**: save, name, load, and delete Tabata configurations, mirroring
  the EMOM preset system
- **HomeScreen**: new app entry point with EMOM and Tabata timer cards; settings ⚙
  icon moved here from the setup screen
- **Drum-roll wheel pickers**: replaced +/− steppers with infinite-scroll snap pickers
  for all mm:ss duration fields
- **Shared `PresetsSection` component**: generic chip row used by both EMOM and Tabata
  setup screens, eliminating duplicate code
- **11 unit tests** covering Tabata engine accuracy, phase transitions, pause/resume,
  and edge cases

### Changed
- App renamed from **EMOM Timer** to **DK Timer**
- EMOM setup screen: Settings icon replaced with a back-arrow; title shortened to "EMOM"
- Both setup screens now fit within the visible viewport (no scrolling required)
- Settings descriptions updated to say "each timer event" (applies to both timers)
- Complete Material 3 typography scale defined (all 15 slots); explicit weights throughout

### Refactored
- Extracted `AbstractPausableEngine` base class — shared by `TimerEngineImpl` and
  `TabataEngineImpl`, eliminating duplicated pause/resume logic
- `SessionStatus` moved from `SessionViewModel` to `domain/model/` so both timer
  view models can reference it without UI coupling

---

## [1.0.0] - 2026-05-05

### Added
- Initial project setup with MVVM + Clean Architecture
- Drift-free timer engine based on system clock anchoring
- Setup screen with mm:ss duration pickers
- Active session screen with round counter and countdown
- Settings screen with sound and vibration toggles
- ToneGenerator audio using STREAM_ALARM (ignores silent mode)
- Vibration feedback on intervals and workout completion
- FLAG_KEEP_SCREEN_ON during sessions
- GitHub Actions CI/CD pipeline (dev CI + tagged release APK)
- Unit tests covering timer accuracy and edge cases
- Preset system: save, name, load, and delete workout configurations
- Pause/resume support with drift-free accuracy preserved across pauses
- App info section in Settings (version, author with website link)

---

[Unreleased]: https://github.com/daniel-kindl/emom-timer/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/daniel-kindl/emom-timer/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/daniel-kindl/emom-timer/releases/tag/v1.0.0
