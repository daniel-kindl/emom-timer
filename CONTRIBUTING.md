# Contributing to EMOM Timer

Thank you for helping improve EMOM Timer!

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable production code. **Protected — no direct pushes.** |
| `dev`  | Active development. All work goes here. |

**All commits go to `dev`.**
`main` is only updated via tagged releases.

---

## Commit Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]
```

Types: `feat`, `fix`, `refactor`, `test`, `ci`, `docs`, `chore`

Examples:
```
feat(engine): add drift-free interval scheduling
fix(audio): prevent overlapping tones on rapid intervals
test(engine): cover non-divisible duration edge case
```

---

## Development Workflow

1. Branch off `dev`:
   ```bash
   git checkout dev
   git pull origin dev
   git checkout -b feat/my-feature
   ```

2. Implement your change.

3. Run checks locally:
   ```bash
   ./gradlew testDebugUnitTest detekt
   ```

4. Open a PR targeting `dev`.

---

## Release Process

Releases follow **Semantic Versioning** (`MAJOR.MINOR.PATCH`):

- `MAJOR` — breaking changes
- `MINOR` — new features, backwards compatible
- `PATCH` — bug fixes

**Steps to release:**

1. Ensure `dev` CI is green.
2. Update `CHANGELOG.md` with the new version section.
3. Bump `versionName` / `versionCode` in `app/build.gradle.kts`.
4. Merge `dev` → `main` via PR.
5. Tag the merge commit:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
6. The `release` workflow builds the signed APK and creates a GitHub Release automatically.

---

## Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- `detekt` is enforced in CI — run `./gradlew detekt` before pushing
- No business logic in UI layer
- Domain layer must remain Android-free (pure Kotlin)
