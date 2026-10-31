package com.limajuice.liftlog.wear.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WatchCommandSender(private val context: Context) {

    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    suspend fun completeSet(
        exerciseIndex: Int,
        setIndex: Int,
        weight: Double,
        reps: Int,
        unit: String
    ) = withContext(Dispatchers.IO) {
        val payload = mapOf(
            "type" to "CompleteSetCommand",
            "exerciseIndex" to exerciseIndex,
            "setIndex" to setIndex,
            "weight" to mapOf(
                "value" to weight.toString(),
                "unit" to if (unit.equals("lb", ignoreCase = true)) "pounds" else "kilograms"
            ),
            "reps" to reps
        )
        sendCommand("/liftlog/command/complete_set", payload)
    }

    suspend fun skipRestTimer() = withContext(Dispatchers.IO) {
        val payload = mapOf("type" to "SkipRestTimerCommand")
        sendCommand("/liftlog/command/skip_rest", payload)
    }

    suspend fun adjustRestTimer(deltaSeconds: Int) = withContext(Dispatchers.IO) {
        val payload = mapOf(
            "type" to "AdjustRestTimerCommand",
            "deltaSeconds" to deltaSeconds
        )
        sendCommand("/liftlog/command/adjust_rest", payload)
    }

    suspend fun finishWorkout() = withContext(Dispatchers.IO) {
        val payload = mapOf("type" to "FinishWorkoutCommand")
        sendCommand("/liftlog/command/finish_workout", payload)
    }

    suspend fun navigateSet(exerciseIndex: Int, setIndex: Int) = withContext(Dispatchers.IO) {
        val payload = mapOf(
            "type" to "NavigateSetCommand",
            "exerciseIndex" to exerciseIndex,
            "setIndex" to setIndex
        )
        sendCommand("/liftlog/command/navigate_set", payload)
    }

    private suspend fun sendCommand(path: String, payloadMap: Map<String, Any>) {
        try {
            val adapter = moshi.adapter(Map::class.java)
            val jsonString = adapter.toJson(payloadMap)
            val bytes = jsonString.toByteArray(Charsets.UTF_8)

            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected phone nodes found to send command $path")
                return
            }

            for (node in nodes) {
                messageClient.sendMessage(node.id, path, bytes).await()
                Log.d(TAG, "Sent command $path to phone ${node.displayName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send command $path to phone", e)
        }
    }

    companion object {
        private const val TAG = "WatchCommandSender"
    }
}
