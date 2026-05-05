# Changelog

All notable changes to EMOM Timer are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added
- Initial project setup with MVVM + Clean Architecture
- Drift-free timer engine based on system clock anchoring
- Setup screen with mm:ss duration pickers
- Active session screen with round counter and countdown
- Settings screen with sound and vibration toggles
- ToneGenerator audio using STREAM_ALARM (ignores silent mode)
- Vibration feedback on intervals and workout completion
- FLAG_KEEP_SCREEN_ON during sessions
- GitHub Actions CI/CD pipeline (dev build + release APK)
- Unit tests covering timer accuracy and edge cases

---

[Unreleased]: https://github.com/daniel-kindl/emom-timer/compare/HEAD...HEAD
