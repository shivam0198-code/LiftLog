package com.limajuice.liftlog.wear.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limajuice.liftlog.wear.data.WatchCommandSender
import com.limajuice.liftlog.wear.data.WearWorkoutRepository
import com.limajuice.liftlog.wear.data.WearWorkoutState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val commandSender = WatchCommandSender(application)
    val state: StateFlow<WearWorkoutState> = WearWorkoutRepository.workoutState

    private val _isSummaryOpen = MutableStateFlow(false)
    val isSummaryOpen: StateFlow<Boolean> = _isSummaryOpen.asStateFlow()

    fun completeSet(weight: Double, reps: Int) {
        val current = state.value
        viewModelScope.launch {
            commandSender.completeSet(
                exerciseIndex = current.exerciseIndex,
                setIndex = current.setIndex - 1,
                weight = weight,
                reps = reps,
                unit = current.weightUnit
            )
        }
    }

    fun skipRestTimer() {
        viewModelScope.launch {
            commandSender.skipRestTimer()
            WearWorkoutRepository.updateState { it.copy(isRestTimerActive = false) }
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        viewModelScope.launch {
            commandSender.adjustRestTimer(deltaSeconds)
            WearWorkoutRepository.updateState {
                val newEnd = it.restTimerEndMs + (deltaSeconds * 1000L)
                it.copy(restTimerEndMs = newEnd)
            }
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            commandSender.finishWorkout()
            _isSummaryOpen.value = false
            WearWorkoutRepository.setState(WearWorkoutState(isActive = false))
        }
    }

    fun navigateSet(delta: Int) {
        val current = state.value
        val newSet = (current.setIndex + delta).coerceIn(1, current.totalSets)
        if (newSet != current.setIndex) {
            viewModelScope.launch {
                commandSender.navigateSet(current.exerciseIndex, newSet - 1)
                WearWorkoutRepository.updateState { it.copy(setIndex = newSet) }
            }
        }
    }

    fun openSummary() {
        _isSummaryOpen.value = true
    }

    fun closeSummary() {
        _isSummaryOpen.value = false
    }
}
