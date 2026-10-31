package com.limajuice.liftlog.wear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.limajuice.liftlog.wear.data.HealthServicesManager
import com.limajuice.liftlog.wear.presentation.active.ActiveWorkoutScreen
import com.limajuice.liftlog.wear.presentation.idle.IdleScreen
import com.limajuice.liftlog.wear.presentation.summary.WorkoutSummaryScreen
import com.limajuice.liftlog.wear.presentation.theme.LiftLogWearTheme
import com.limajuice.liftlog.wear.presentation.timer.RestTimerScreen
import com.limajuice.liftlog.wear.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WorkoutViewModel by viewModels()
    private val healthManager by lazy { HealthServicesManager(this) }

    private val sensorPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            healthManager.startTracking()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen awake while active in a set
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Check and request heart rate permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            healthManager.startTracking()
        } else {
            sensorPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }

        setContent {
            LiftLogWearTheme {
                val state by viewModel.state.collectAsState()
                val isSummaryOpen by viewModel.isSummaryOpen.collectAsState()

                when {
                    !state.isActive -> {
                        IdleScreen()
                    }
                    isSummaryOpen -> {
                        WorkoutSummaryScreen(
                            state = state,
                            onConfirmFinish = { viewModel.finishWorkout() },
                            onBackToWorkout = { viewModel.closeSummary() }
                        )
                    }
                    state.isRestTimerActive -> {
                        RestTimerScreen(
                            state = state,
                            onSkipRest = { viewModel.skipRestTimer() },
                            onAdjustRest = { delta -> viewModel.adjustRestTimer(delta) }
                        )
                    }
                    else -> {
                        ActiveWorkoutScreen(
                            state = state,
                            onCompleteSet = { weight, reps -> viewModel.completeSet(weight, reps) },
                            onNavigateSet = { delta -> viewModel.navigateSet(delta) },
                            onOpenSummary = { viewModel.openSummary() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        healthManager.stopTracking()
    }
}
