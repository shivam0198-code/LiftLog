package expo.modules.workoutworker.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.limajuice.liftlog.WorkoutEndedEvent
import com.limajuice.liftlog.WorkoutMessage
import com.limajuice.liftlog.WorkoutUpdatedEvent
import expo.modules.workoutworker.utils.Json
import kotlinx.coroutines.tasks.await

class WearableSyncManager(private val context: Context) {

    private val dataClient by lazy { Wearable.getDataClient(context) }

    suspend fun syncWorkoutState(message: WorkoutMessage) {
        try {
            val payload = message.payload
            when (payload) {
                is WorkoutUpdatedEvent -> {
                    val request = PutDataMapRequest.create(PATH_WORKOUT_STATE).apply {
                        dataMap.putString(KEY_WORKOUT_JSON, Json.encodeToString(payload))
                        dataMap.putBoolean(KEY_IS_ACTIVE, true)
                        dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    }.asPutDataRequest().setUrgent()

                    dataClient.putDataItem(request).await()
                    Log.d(TAG, "Synced active workout state to Wear OS")
                }
                is WorkoutEndedEvent -> {
                    val request = PutDataMapRequest.create(PATH_WORKOUT_STATE).apply {
                        dataMap.putBoolean(KEY_IS_ACTIVE, false)
                        dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    }.asPutDataRequest().setUrgent()

                    dataClient.putDataItem(request).await()
                    Log.d(TAG, "Synced workout ended state to Wear OS")
                }
                else -> {
                    // Other messages do not alter persistent watch state
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync workout state to Wear OS", e)
        }
    }

    companion object {
        private const val TAG = "WearableSyncManager"
        const val PATH_WORKOUT_STATE = "/liftlog/workout_state"
        const val KEY_WORKOUT_JSON = "workoutJson"
        const val KEY_IS_ACTIVE = "isActive"
        const val KEY_TIMESTAMP = "timestamp"
    }
}
