# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project

Kids' Puzzle is a single-module Android app (jigsaw puzzle assembly game for a `lv.marmog.androidpuzzlegame` package). Java, Gradle, AndroidX/Material. `minSdk 24`, `compileSdk`/`targetSdk 36`, Java 21 source/target.

## Critical environment note: JDK version

This machine only has **JDK 25** installed system-wide, but **Gradle 8.13 / AGP 8.12 cannot run on JDK 25** — any test/build task that instantiates `AndroidUnitTest` fails with `TypeNotPresentException: Type T not present` deep in Gradle's task reflection. This is an environment problem, not a code problem, and it affects every unit test, not just new ones.

Fix: point Gradle's daemon at a full JDK 17–21 via the **global** `~/.gradle/gradle.properties` (machine-local, not tracked in this repo — never add `org.gradle.java.home` to the project's own `gradle.properties`, since the path is machine-specific):

```properties
org.gradle.java.home=C:/Users/<user>/.jdks/ms-21.0.7
```

**It must be a real, complete JDK — not a JetBrains Runtime (JBR).** IntelliJ IDEA's own bundled runtime (`...\IntelliJ IDEA Ultimate <version>\jbr`) looks like a JDK 21 and satisfies the point above, but JetBrains strips `jlink`/`jmod` out of it. This project's `compileOptions` target Java 21 (source ≥ 9), which makes AGP build an "Android system modules" image via `jlink` (the `androidJdkImage`/`JdkImageTransform` step) — that step shells out to the `jlink` next to whichever `java` is running Gradle, so a JBR-based `org.gradle.java.home` fails with `jlink executable ... does not exist`. Use a full JDK distribution instead — e.g. one IntelliJ has already downloaded for its own SDK list under `~/.jdks/` (check with `ls ~/.jdks`), or a fresh install (Eclipse Temurin / Microsoft Build of OpenJDK, 17 or 21).

After changing `org.gradle.java.home`, run `./gradlew --stop` first so the old daemon isn't reused.

## Commands

All from the repo root, using `gradlew.bat` on Windows (`./gradlew` in Git Bash).

```bash
./gradlew.bat assembleDebug                       # build the debug APK
./gradlew.bat testDebugUnitTest                   # run all JVM unit tests (app/src/test)
./gradlew.bat testDebugUnitTest --tests "lv.marmog.androidpuzzlegame.ui.ComplexityActivityTest"                       # one class
./gradlew.bat testDebugUnitTest --tests "lv.marmog.androidpuzzlegame.ui.ComplexityActivityTest.goHome_startsStartActivityAndFinishes"  # one method
./gradlew.bat connectedDebugAndroidTest           # instrumented tests (app/src/androidTest) - needs an emulator/device
./gradlew.bat --stop                              # kill Gradle daemons (do this after changing org.gradle.java.home)
```

There is no lint/static-analysis or CI config in this repo currently.

## Unit testing without Robolectric

`app/build.gradle` sets `testOptions.unitTests.returnDefaultValues = true`, so calling an un-stubbed Android SDK method in a JVM unit test returns a default value (`null`/`0`/`false`) instead of throwing — this is what makes it possible to unit-test Android classes here without Robolectric or an emulator (there's no Robolectric dependency in this project). Mockito is on `mockito-core:5.23.0`, so its inline mock maker is the default, which is what makes the two tricks below possible.

- **DAO classes (`UserDAO`, `TimerDAO`) are directly testable**: each has a public `Context`-based constructor plus a **package-private constructor taking a `DatabaseProvider`** (see `database/DatabaseProvider.java`), explicitly labeled as a test-only seam. Tests inject a mocked `SQLiteDatabase` through this instead of touching a real `Context`. Follow this same pattern (`UserDAOTest.java`) for any new DAO.
- **`Activity` subclasses cannot be constructed with `new` in a test**: `AppCompatActivity`'s constructor chain (`ComponentActivity`) touches the real main `Looper`, which is `null` outside an actual Android runtime, and NPEs. Use `Mockito.mock(SomeActivity.class, Answers.CALLS_REAL_METHODS)` instead — it builds the instance via Objenesis (skipping the constructor) while still running real method bodies unless a method is explicitly stubbed. See `ComplexityActivityTest`/`CreateUsernameActivityTest`.
- **`onCreate()` itself still can't be called**, even on that mock: it calls `super.onCreate()`, which touches `ComponentActivity` internals (e.g. `SavedStateRegistryController`) that are only initialized by the real constructor. So don't try to drive an Activity through `onCreate()` in a JVM unit test. Instead:
  - Test public methods directly (e.g. `ComplexityActivity.selectPieces()`, `goHome()`).
  - For private logic normally reached through `onCreate()` (e.g. `CreateUsernameActivity.onSaveUsernameClicked()`, `populateUsernamesList()`), inject the private fields via reflection and invoke the private method via reflection, bypassing `onCreate()` entirely.
  - Click listeners that only exist as **inline lambdas inside `onCreate()`** (e.g. the delete-button and list-item-click handlers in `CreateUsernameActivity`) are not reachable this way — they compile to synthetic `lambda$onCreate$N` methods, and reflecting into those is brittle (the index silently shifts if a lambda is added/reordered). These are left untested unless Robolectric is added.
- **`mockConstruction(SomeClass.class)`** (Mockito's inline construction mocking) is used to intercept `new Intent(...)`, `new UserDAO(...)`, `new ArrayAdapter(...)` calls made *inside* the method under test, since those objects can't be constructor-injected.
- **`mockStatic(Toast.class)`** is used to verify/stub `Toast.makeText(...).show()` calls (routed through the `Toasts` helper).

## Architecture

### Package layout (`app/src/main/java/lv/marmog/androidpuzzlegame/`)
- `ui/` — one `AppCompatActivity` per screen, plus small shared helpers `Extras` (intent-extra key constants — always use these, never inline string keys) and `Toasts` (wraps `Toast.makeText(...).show()`).
- `database/` — SQLite persistence. `DatabaseHelper` (package-private schema/`SQLiteOpenHelper`) is the only class that knows the table/column names; `UserDAO` and `TimerDAO` are the only classes allowed to build SQL, each going through the `DatabaseProvider` interface (`getWritableDatabase()`) rather than a raw `SQLiteDatabase` directly, for testability.
- `model/` — `User`, a plain data holder.
- `puzzle/` — the jigsaw mechanics: `PuzzlePiece` (an `AppCompatImageView` subclass carrying its target position/size), `TouchListener` (drag/drop/snap-to-position logic, `OnPieceSnappedListener` callback), `SoundEffects` (one-shot `MediaPlayer` playback shared between drag sounds and the completion cheer).
- `adapter/` — `AssetImageLoader` (lists/decodes puzzle source images from `assets/img/`) and `ImageAdapter` (grid of image thumbnails).
- `exception/` — the app's own exception types (e.g. `PuzzleImagesUnavailableException`, thrown by `ImageAdapter` when `assets/img` can't be listed). Put every new custom exception here, not next to the class that throws it; keep them unchecked unless callers can genuinely recover, and always pass the original exception as `cause`.

### Screen flow
`StartActivity` (launcher) → `CreateUsernameActivity` (create/delete users) or, picking an existing user, → `ComplexityActivity` (choose 4/9/12 pieces) → `GridViewActivity` (choose source image, or camera/gallery) → `PuzzleActivity` (the actual jigsaw) → `ScoreActivity` (shows time + best time, then either "next game" back to `ComplexityActivity` or home). Every screen after `StartActivity` carries the current `userId`/`username` forward via `Intent` extras (keys in `Extras`) and has a "go home" `FloatingActionButton` that returns to `StartActivity`, ending the current session.

### Puzzle piece generation (`PuzzleActivity.splitImage()`)
This is the densest piece of logic in the app: it crops/scales the chosen bitmap, slices it into a `rows × cols` grid, and for each piece draws a jigsaw-shaped `Path` (straight edge on the puzzle's outer border, a bump/notch cubic-Bezier curve on internal edges) that's used both to mask the piece's bitmap and to stroke its border. `TouchListener` then handles drag/drop and uses `piece.xCoord`/`yCoord` (the piece's correct position, set during slicing) plus a distance tolerance to decide when a dropped piece "snaps" into place. `PuzzleActivity.isGameOver()` / `checkGameOver()` just check whether every `PuzzlePiece.canMove` is now `false`.

### Two DB tables, no migrations
`DatabaseHelper` defines `users` (id, username) and `timer` (best times per user, one column per complexity level: `timer_result_for_4/9/12`, FK to `users`). `onUpgrade()` just drops and recreates both tables — there is no real migration path, so a `DATABASE_VERSION` bump wipes all local data.
