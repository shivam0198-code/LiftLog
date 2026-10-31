# Wear OS Companion App

LiftLog features an optional, modular **Wear OS companion app** designed to provide glanceable workout tracking directly on your wrist without disrupting your training flow.

---

## Architectural Principles

The Wear OS companion adheres to LiftLog's core architecture defined in [`docs/WorkoutWorker.md`](./WorkoutWorker.md):

1. **Phone is the Single Source of Truth**:
   - The watch is a remote glanceable HUD and input controller.
   - All session state, progression rules, and persistent SQLite records remain exclusively on the phone.
   - The watch has no direct database access; if the phone is disconnected, the watch displays cached state until reconnected.
2. **Non-Invasive Integration**:
   - The watch app communicates with the mobile phone via Google Play Services **Wearable Data Layer API**.
   - The phone's background `WorkoutWorkerService` pushes updates via `DataClient` (`/liftlog/workout_state`).
   - The watch transmits user interactions (set completions, rest adjustments) back via `MessageClient` (`/liftlog/command/*`).
   - Incoming watch commands are dispatched directly into `WorkoutWorkerModule.broadcastMessage()` and processed by standard Redux actions.

---

## Features

- **Active Set Tracking**:
  - Displays current exercise name, set number (e.g., Set 2 of 4), and target reps/weight.
  - Previous performance guidance for quick reference.
  - Interactive `+` / `-` steppers for adjusting reps and weights before logging.
  - Rotating bezel / crown support via Horologist rotary handlers.
  - Prominent one-tap `✓ Complete Set` action.
- **Rest Timer with Haptics**:
  - Circular animated countdown progress ring.
  - Warning pulses at 3, 2, and 1 seconds remaining.
  - Double-pulse haptic vibration upon rest completion.
  - `+30s`, `-30s`, and `Skip Rest` controls.
  - Always-on display (Ambient mode) low-power updates.
- **Live Heart Rate Tracking**:
  - Uses the watch's optical PPG sensor via Wear Health Services to display live heart rate on screen during sets and rest intervals.
- **Workout Lifecycle & Summary**:
  - Standby idle screen when no workout is running.
  - End-of-workout volume summary and confirmation dialog.

---

## Module Structure

- **`wear/`**: Isolated native Wear OS application built with Kotlin and Jetpack Compose for Wear OS.
- **`app/modules/workout-worker/android/src/main/java/expo/modules/workoutworker/wear/`**:
  - `WearableSyncManager.kt`: Packages and dispatches active workout snapshots to Wear OS.
  - `PhoneWearableListenerService.kt`: Receives watch commands and emits them into the React Native runtime.
- **`app/plugins/with-wear-os.js`**: Expo Config Plugin that links `:wear` into the Gradle build without breaking continuous native generation (CNG).
