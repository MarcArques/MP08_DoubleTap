package com.pablovicente.doubletapvicentepablo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

class Accelerometer : SensorEventListener {
    companion object {
        private const val TAP_THRESHOLD = 4f
        private const val DOUBLE_TAP_WINDOW = 300L
    }

    private var sensorManager: SensorManager? = null
    private var linearAccelerationSensor: Sensor? = null
    private var isListening = false
    private val onDoubleTapListeners = mutableListOf<(FloatArray) -> Unit>()
    private val onSensorChangedListeners = mutableListOf<(FloatArray) -> Unit>()

    private var lastTapTimestamp: Long = 0
    private var tapCount = 0

    fun init(context: Context) {
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            linearAccelerationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

            if (sensorManager == null || linearAccelerationSensor == null) {
                throw IllegalStateException("Device doesn't support required sensors")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize accelerometer: ${e.message}")
        }
    }

    fun addOnDoubleTapListener(listener: (FloatArray) -> Unit) {
        onDoubleTapListeners.add(listener)
    }

    fun removeOnDoubleTapListener(listener: (FloatArray) -> Unit) {
        onDoubleTapListeners.remove(listener)
    }

    fun addOnSensorChangedListener(listener: (FloatArray) -> Unit) {
        onSensorChangedListeners.add(listener)
    }

    fun removeOnSensorChangedListener(listener: (FloatArray) -> Unit) {
        onSensorChangedListeners.remove(listener)
    }

    fun startListening() {
        if (isListening) return

        sensorManager?.registerListener(
            this,
            linearAccelerationSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )?.also { success ->
            if (success) {
                isListening = true
            } else {
                throw IllegalStateException("Failed to start sensor listening")
            }
        }
    }

    fun stopListening() {
        if (!isListening) return

        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                onSensorChangedListeners.forEach { it.invoke(sensorEvent.values) }
                processTapDetection(sensorEvent.values)
            }
        }
    }

    private fun processTapDetection(values: FloatArray) {
        val (x, y, z) = values.map { abs(it) }

        if (x > TAP_THRESHOLD || y > TAP_THRESHOLD || z > TAP_THRESHOLD) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastTapTimestamp > DOUBLE_TAP_WINDOW) {
                tapCount = 0
            }

            // Esto de aquí registra el toque
            tapCount++
            lastTapTimestamp = currentTime

            when (tapCount) {
                2 -> {
                    triggerDoubleTapListeners(values)
                }
                3 -> {
                    tapCount = 1
                }
            }
        }
    }


    private fun triggerDoubleTapListeners(values: FloatArray) {
        onDoubleTapListeners.forEach { it.invoke(values) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun isInitialized(): Boolean = sensorManager != null && linearAccelerationSensor != null

    fun isSensorAvailable(): Boolean = linearAccelerationSensor != null
}