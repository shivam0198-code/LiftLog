package expo.modules.workoutworker.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.limajuice.liftlog.AppConfiguration
import com.limajuice.liftlog.Translations
import com.limajuice.liftlog.WorkoutMessage
import com.limajuice.liftlog.WorkoutMessagePayload
import expo.modules.workoutworker.WorkoutWorkerModule
import expo.modules.workoutworker.utils.Json

class PhoneWearableListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        Log.d(TAG, "Message received on path: $path")

        if (path.startsWith(PATH_COMMAND_PREFIX)) {
            try {
                val jsonString = String(messageEvent.data, Charsets.UTF_8)
                Log.d(TAG, "Watch command payload: $jsonString")

                val payload = Json.decodeFromString<WorkoutMessagePayload>(jsonString)
                val dummyTranslations = Translations(
                    workoutPersistentNotificationCurrentExerciseMessage = "",
                    workoutPersistentNotificationCardioTargetReachedMessage = "",
                    workoutPersistentNotificationFinishedMessage = "",
                    workoutPersistentNotificationFinishWorkoutAction = "",
                    workoutPersistentNotificationInProgressMessage = "",
                    workoutPersistentNotificationMaxRestOverMessage = "",
                    workoutPersistentNotificationMinRestOverMessage = "",
                    workoutPersistentNotificationRestBreakMessage = "",
                    workoutPersistentNotificationStartNowMessage = "",
                    workoutPersistentNotificationStartSoonMessage = ""
                )
                val workoutMessage = WorkoutMessage(
                    appConfiguration = AppConfiguration(notificationsEnabled = true),
                    payload = payload,
                    translations = dummyTranslations
                )
                WorkoutWorkerModule.broadcastMessage(workoutMessage)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling watch command from $path", e)
            }
        } else {
            super.onMessageReceived(messageEvent)
        }
    }

    companion object {
        private const val TAG = "PhoneWearableListener"
        const val PATH_COMMAND_PREFIX = "/liftlog/command"
    }
}
