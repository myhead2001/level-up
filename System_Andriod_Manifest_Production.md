

# Production Specification: System Fit Android

## 1. Complete Dependency Tree & Build Configuration

To prevent class path conflicts, version mismatches, or compilation errors during the build phase, enforce this exact configuration for `build.gradle.kts` (Module: app).

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sololeveling.systemfit"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}

dependencies {
    // AndroidX & Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose (BOM)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Dependency Injection (Hilt)
    val hiltVersion = "2.50"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Firebase (BOM)
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Image/GIF Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
}

```

---

## 2. Low-Stamina & Hypertension Architecture Model

To ensure deterministic state changes and safety overrides, the presentation layer must follow a strict Unidirectional Data Flow (UDF) pattern via a sealed contract interface.

```kotlin
package com.sololeveling.systemfit.presentation.workout

import com.sololeveling.systemfit.domain.model.Exercise

interface WorkoutContract {
    sealed interface UiState {
        object Loading : UiState
        data class Setup(val totalDurationMinutes: Int, val rounds: Int) : UiState
        data class ActiveCombat(
            val currentRound: Int,
            val totalRounds: Int,
            val currentExercise: Exercise,
            val nextExerciseName: String?,
            val isRestPeriod: Boolean,
            val timeLeftSeconds: Int,
            val totalTimeLeftSeconds: Int
        ) : UiState
        data class PenaltyZone(val penaltyDurationMinutes: Int) : UiState
        data class Victory(val xpEarned: Int, val levelUp: Boolean) : UiState
    }

    sealed interface UiEvent {
        object StartQuest : UiEvent
        object SkipRest : UiEvent
        object TriggerPanicButton : UiEvent // Immediate Emergency Halt
        object ClaimRewards : UiEvent
    }

    sealed interface SideEffect {
        object PlaySystemChime : SideEffect
        object TriggerHapticAlert : SideEffect
        data class ShowToast(val message: String) : SideEffect
    }
}

```

---

## 3. Exhaustive Production Package & File Tree

The AI must build out the repository using this granular Clean Architecture structure. Every file name specified below must be explicitly written out without consolidation.

```text
com.sololeveling.systemfit
│
├── di
│   ├── DatabaseModule.kt          # Room DB, DAOs, and Shared Prefs injection
│   ├── FirebaseModule.kt          # Firebase Auth and Firestore injection
│   └── RepositoryModule.kt        # Binds interfaces to implementations
│
├── data
│   ├── local
│   │   ├── SystemDatabase.kt      # Room Database definition with pre-population callback
│   │   ├── dao
│   │   │   ├── UserDao.kt         # User profile, statistics, and level CRUD operations
│   │   │   └── ExerciseDao.kt     # Exercise library reader and random selection queries
│   │   └── entity
│   │       ├── UserEntity.kt
│   │       └── ExerciseEntity.kt
│   │
│   ├── remote
│   │   ├── model
│   │   │   └── FirestoreUserDto.kt# Network data transfer object for cloud synchronization
│   │   └── DataSource
│   │       └── RemoteSyncSource.kt# Handles double-write syncing to Firestore
│   │
│   └── repository
│       ├── UserRepositoryImpl.kt
│       └── ExerciseRepositoryImpl.kt
│
├── domain
│   ├── model
│   │   ├── User.kt                # Clean domain representation of Player
│   │   ├── Exercise.kt            # Clean domain representation of Exercise
│   │   └── ExerciseType.kt        # Enum class
│   │
│   └── usecase
│       ├── GenerateDailyQuestUseCase.kt  # Applies the deterministic stat-scaling math
│       ├── ProcessWorkoutResultUseCase.kt# Calculates XP, evaluates levels, handles penalty tracking
│       └── EmergencyHaltUseCase.kt       # Controls panic logic and cooling routine
│
└── presentation
    ├── theme
    │   ├── Color.kt               # System black, neon blue glowing tokens, warning gold
    │   ├── Type.kt                # Custom high-tech typography configuration
    │   └── Theme.kt               # Custom Material3 configuration matching Solo Leveling aesthetic
    │
    ├── main
    │   ├── MainActivity.kt        # Root window, sets content, controls global state
    │   └── Navigation.kt          # Navigation graph routing Compose destinations
    │
    ├── dashboard
    │   ├── DashboardScreen.kt     # Profile display, XP gauge, stat point spending controls
    │   └── DashboardViewModel.kt
    │
    ├── workout
    │   ├── WorkoutScreen.kt       # Active exercise view with GIF, timer component, emergency halt
    │   └── WorkoutViewModel.kt    # Lifecycle-aware countdown handling with zero time-drift
    │
    └── components
        ├── NeonPanel.kt           # Custom UI border modifier with outer glow effect
        └── CountdownRing.kt       # Thread-safe SVG circular progress tracker

```

---

## 4. Hardware Safety & Robust Background Execution

To keep the application processing reliably when resource demands fluctuate, the workout flow must integrate native OS lifecycle configurations.

### Memory Leak Prevention & WakeLock Lifecycle

The active workout screen must explicitly manage the window state via Compose `DisposableEffect` hooks to preserve resources:

```kotlin
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onNavigateToRewards: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Imperative system window control to prevent display timeout mid-circuit
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    // UI rendering implementation...
}

```

### Clock Drift Correction Engine

To completely avoid the background thread execution pauses common to Kotlin's standard `delay()` loops, the execution engine inside `WorkoutViewModel` must process time durations using relative device-uptime measurements:

```kotlin
class WorkoutViewModel @Inject constructor(
    private val generateDailyQuestUseCase: GenerateDailyQuestUseCase
) : ViewModel() {

    private var expectedEndTimeMillis = 0L
    private var timerJob: Job? = null

    fun startCountdown(durationSeconds: Int) {
        timerJob?.cancel()
        // Establish an immutable target time independent of application execution pauses
        expectedEndTimeMillis = SystemClock.elapsedRealtime() + (durationSeconds * 1000)
        
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (SystemClock.elapsedRealtime() < expectedEndTimeMillis) {
                val remaining = ((expectedEndTimeMillis - SystemClock.elapsedRealtime()) / 1000).toInt()
                updateTimerState(maxOf(0, remaining))
                delay(200) // High frequency sampling keeps precision without CPU utilization spikes
            }
            onTimerComplete()
        }
    }
}

```

---

## 5. Algorithmic Scaling & Progression System Metrics

### Mathematical Determinism

The scaling architecture must execute these formulas precisely inside `GenerateDailyQuestUseCase.kt`:

* **Active Interval Duration:** 
$$T_{active} = \min(20 + (AGI \times 2), 60)$$


* **Rest Interval Duration:** 
$$T_{rest} = \max(90 - (VIT \times 3), 30)$$


* **Total Target Rounds:** 
$$Rounds = \min(2 + \lfloor Level / 3 \rfloor, 5)$$


* **Next Level XP Threshold:** 
$$XP_{req} = 100 \times Level^{1.5}$$



### Hypertension Emergency Protocol

If `TriggerPanicButton` is registered via the `UiEvent` loop:

1. The active timer job is explicitly destroyed via `timerJob?.cancel()`.
2. The user state transitions directly into an immutable 3-minute controlled nasal recovery step ($180\text{ seconds}$).
3. The backend triggers a standard system `ToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)` signature to confirm receipt of the emergency command.
4. No performance failure flag is stored in the local Room log table. The application records a `isPartialCompletion = true` marker, scales down the collected experience calculation by half, and commits changes immediately to prevent loss of compliance history.