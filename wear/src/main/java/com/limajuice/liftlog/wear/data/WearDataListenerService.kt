package com.limajuice.liftlog.wear.data

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.limajuice.liftlog.wear.MainActivity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant

class WearDataListenerService : WearableListenerService() {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path == PATH_WORKOUT_STATE) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val isActive = dataMapItem.dataMap.getBoolean(KEY_IS_ACTIVE, false)
                    val workoutJson = dataMapItem.dataMap.getString(KEY_WORKOUT_JSON)

                    Log.d(TAG, "Workout state changed: isActive=$isActive, hasJson=${workoutJson != null}")

                    if (!isActive || workoutJson.isNullOrBlank()) {
                        WearWorkoutRepository.setState(WearWorkoutState(isActive = false))
                    } else {
                        parseAndApplyState(workoutJson)
                    }
                }
            }
        }
    }

    private fun parseAndApplyState(json: String) {
        try {
            val adapter = moshi.adapter(Map::class.java)
            @Suppress("UNCHECKED_CAST")
            val root = adapter.fromJson(json) as? Map<String, Any> ?: return

            val currentExerciseDetails = root["currentExerciseDetails"] as? Map<String, Any>
            val exercise = currentExerciseDetails?.get("exercise") as? Map<String, Any>
            val blueprint = exercise?.get("blueprint") as? Map<String, Any>
            val exerciseName = (blueprint?.get("name") as? String) ?: "Workout"
            val setIndex = (currentExerciseDetails?.get("setIndex") as? Number)?.toInt() ?: 0

            val potentialSets = (exercise?.get("potentialSets") as? List<Map<String, Any>>) ?: emptyList()
            val totalSets = potentialSets.size.coerceAtLeast(1)
            val currentPotentialSet = potentialSets.getOrNull(setIndex)
            val target = currentPotentialSet?.get("target") as? Map<String, Any>
            val targetReps = (target?.get("reps") as? Number)?.toInt()
                ?: (target?.get("max") as? Number)?.toInt() ?: 8

            val weightMap = currentPotentialSet?.get("weight") as? Map<String, Any>
            val targetWeight = (weightMap?.get("value") as? Number)?.toDouble() ?: 0.0
            val weightUnitRaw = (weightMap?.get("unit") as? String) ?: "kilograms"
            val weightUnit = if (weightUnitRaw.contains("pound", ignoreCase = true)) "lb" else "kg"

            // Rest timer info
            val restTimerInfo = root["restTimerInfo"] as? Map<String, Any>
            val isRestTimerActive = restTimerInfo != null
            var restStartedMs = 0L
            var restEndMs = 0L

            if (restTimerInfo != null) {
                val startIso = restTimerInfo["startedAt"] as? String
                val endIso = restTimerInfo["endAt"] as? String
                if (startIso != null) {
                    restStartedMs = runCatching { Instant.parse(startIso).toEpochMilli() }.getOrDefault(0L)
                }
                if (endIso != null) {
                    restEndMs = runCatching { Instant.parse(endIso).toEpochMilli() }.getOrDefault(0L)
                }
            }

            // Totals
            val totalWeightMap = root["totalWeightLifted"] as? Map<String, Any>
            val totalWeightVal = (totalWeightMap?.get("value") as? Number)?.toDouble() ?: 0.0
            val totalWeightStr = "${totalWeightVal.toInt()} $weightUnit"

            val workoutDuration = (root["workoutDuration"] as? String) ?: "00:00"

            val newState = WearWorkoutState(
                isActive = true,
                exerciseName = exerciseName,
                exerciseIndex = 0,
                totalExercises = 1,
                setIndex = setIndex + 1,
                totalSets = totalSets,
                targetWeight = targetWeight,
                targetReps = targetReps,
                weightUnit = weightUnit,
                isRestTimerActive = isRestTimerActive,
                restTimerStartedMs = restStartedMs,
                restTimerEndMs = restEndMs,
                totalWeightLiftedFormatted = totalWeightStr,
                workoutDurationFormatted = workoutDuration
            )

            WearWorkoutRepository.setState(newState)

            // Auto-launch activity if a new workout is starting and screen is off/closed
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse workout state JSON", e)
        }
    }

    companion object {
        private const val TAG = "WearDataListener"
        const val PATH_WORKOUT_STATE = "/liftlog/workout_state"
        const val KEY_WORKOUT_JSON = "workoutJson"
        const val KEY_IS_ACTIVE = "isActive"
    }
}
