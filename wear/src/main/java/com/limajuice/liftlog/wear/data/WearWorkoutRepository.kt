package com.limajuice.liftlog.wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object WearWorkoutRepository {

    private val _workoutState = MutableStateFlow(WearWorkoutState())
    val workoutState: StateFlow<WearWorkoutState> = _workoutState.asStateFlow()

    fun updateState(transform: (WearWorkoutState) -> WearWorkoutState) {
        _workoutState.update(transform)
    }

    fun setState(state: WearWorkoutState) {
        _workoutState.value = state
    }

    fun updateHeartRate(bpm: Int?) {
        _workoutState.update { it.copy(liveHeartRateBpm = bpm) }
    }
}
