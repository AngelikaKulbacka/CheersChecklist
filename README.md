# Cheers Checklist

A personal tasting log for alcoholic drinks, built as a Kotlin Multiplatform (KMP) learning project. It tracks what you've tried, when, and how it tasted — no backend, no account, everything stored locally on-device with Room.

| List screen | Edit/Add screen |
|---|---|
| ![List screen](screenshots/list.png) | ![Edit/Add screen](screenshots/edit.png) |

## Features

- Add, edit, and delete tasting entries
- Fields: name, brand, type (category), color, oiliness, scent, flavor, rating (1–5), date tasted, notes
- **User-defined types**: the built-in categories (Whisky, Wine, Beer, Cocktail, Other) are just defaults — you can add your own from the edit screen, and it's remembered from then on
- Search by name or type
- Filter by category
- Sort by newest, name, or rating
- All data persists locally via Room (SQLite) — nothing leaves the device

## Platforms

| Platform | Status |
|---|---|
| Android | Fully built and tested |
| Desktop (JVM) | Fully built and tested |
| iOS | Compiles (`:shared:compileKotlinIosArm64`), not run — no Mac available for this project |

## Tech stack

- **Kotlin Multiplatform** + **Compose Multiplatform** — shared UI across Android/Desktop/iOS
- **Room** (with the `androidx.sqlite` bundled SQLite driver) — local persistence, with tested schema migrations
- **Koin** — dependency injection
- **Navigation Compose** (JetBrains multiplatform port) — list ↔ edit screen navigation
- **Koin + kotlinx-coroutines + kotlinx-datetime** — the rest of the shared logic layer

## Project structure

- [`/shared`](./shared/src) — all shared code (KMP)
  - [`commonMain`](./shared/src/commonMain/kotlin) — UI (Compose), ViewModel, repositories, Room entities/DAOs/migrations, DI module — the vast majority of the app
  - [`androidMain`](./shared/src/androidMain/kotlin) / [`iosMain`](./shared/src/iosMain/kotlin) / [`jvmMain`](./shared/src/jvmMain/kotlin) — platform-specific pieces only (the Room database driver setup per platform)
  - `commonTest` / `jvmTest` — unit tests, DAO tests, and migration tests (37 tests total)
- [`/androidApp`](./androidApp/src/main) — Android application shell (`MainActivity`, manifest, launcher icon)
- [`/desktopApp`](./desktopApp/src/main) — Desktop application shell (`main()`)
- [`/iosApp`](./iosApp/iosApp) — iOS application shell (SwiftUI entry point)

## Architecture

A pragmatic layered approach, not a full Clean Architecture setup:

- **UI** (`ListScreen.kt`, `EditScreen.kt`) → **`TastingViewModel`** → **Repositories** (`TastedDrinkRepository`, `CategoryRepository`) → **Room DAOs**
- Repositories are interfaces with a Room-backed implementation, injected via Koin — swappable for tests (see `FakeTastedDrinkRepository` / `FakeCategoryRepository` in the test suite)
- Filtering and sorting happen in-memory in the ViewModel as small, independently unit-tested pure functions (`EntryFilter.kt`, `EntrySort.kt`), rather than as Room queries — the dataset is small, and this keeps that logic trivially testable
- Schema changes go through real Room `Migration`s, each verified with `androidx.room.testing.MigrationTestHelper` against a real (in-memory) database

## Getting started

### Running the apps

- Android: `./gradlew :androidApp:assembleDebug` (or install to a connected device/emulator with `:androidApp:installDebug`)
- Desktop: `./gradlew :desktopApp:run`
- iOS: open [`/iosApp`](./iosApp) in Xcode — requires macOS

### Running tests

- All shared-module tests (unit, DAO, migrations): `./gradlew :shared:jvmTest`

### CI

`.github/workflows/ci.yml` runs the test suite plus Android and Desktop builds on every push/PR to `main`.

## Known limitations

- iOS is compile-checked only, never run — no Mac available
- No cloud sync / backup — data lives only on the device
- No dark theme yet
