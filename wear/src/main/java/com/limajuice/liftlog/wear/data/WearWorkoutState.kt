package com.limajuice.liftlog.wear.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WearWorkoutState(
    val isActive: Boolean = false,
    val exerciseName: String = "",
    val exerciseIndex: Int = 0,
    val totalExercises: Int = 0,
    val setIndex: Int = 0,
    val totalSets: Int = 0,
    val targetWeight: Double = 0.0,
    val targetReps: Int = 0,
    val weightUnit: String = "kg",
    val weightIncrement: Double = 2.5,
    val previousPerformance: String? = null,
    val isRestTimerActive: Boolean = false,
    val restTimerStartedMs: Long = 0L,
    val restTimerEndMs: Long = 0L,
    val totalWeightLiftedFormatted: String = "0 kg",
    val workoutDurationFormatted: String = "00:00",
    val liveHeartRateBpm: Int? = null
)
