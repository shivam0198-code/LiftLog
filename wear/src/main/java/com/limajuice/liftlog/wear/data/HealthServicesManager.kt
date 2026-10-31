package com.limajuice.liftlog.wear.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class HealthServicesManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val heartRateSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private var isTracking = false

    fun startTracking() {
        if (isTracking || heartRateSensor == null || sensorManager == null) return
        val success = sensorManager.registerListener(
            this,
            heartRateSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        isTracking = success
        Log.d(TAG, "Heart rate tracking started: $success")
    }

    fun stopTracking() {
        if (!isTracking || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isTracking = false
        WearWorkoutRepository.updateHeartRate(null)
        Log.d(TAG, "Heart rate tracking stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values.firstOrNull()?.toInt()
            if (bpm != null && bpm > 0) {
                WearWorkoutRepository.updateHeartRate(bpm)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    companion object {
        private const val TAG = "HealthServicesManager"
    }
}
