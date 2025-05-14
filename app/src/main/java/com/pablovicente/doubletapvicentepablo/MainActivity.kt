package com.pablovicente.doubletapvicentepablo

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Math.abs

class MainActivity : AppCompatActivity() {
    private val accelerometerUtils = Accelerometer()

    private lateinit var xAcc: TextView
    private lateinit var yAcc: TextView
    private lateinit var zAcc: TextView

    private lateinit var xTapCount: TextView
    private lateinit var yTapCount: TextView
    private lateinit var zTapCount: TextView

    // Contadores de los taps
    private var xTaps = 0
    private var yTaps = 0
    private var zTaps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        xAcc = findViewById(R.id.xAcc)
        yAcc = findViewById(R.id.yAcc)
        zAcc = findViewById(R.id.zAcc)

        xTapCount = findViewById(R.id.xTapCount)
        yTapCount = findViewById(R.id.yTapCount)
        zTapCount = findViewById(R.id.zTapCount)

        setupAccelerometer()
    }

    private fun setupAccelerometer() {
        try {
            accelerometerUtils.init(this)
            accelerometerUtils.addOnSensorChangedListener(::updateAccelerometerValues)
            accelerometerUtils.addOnDoubleTapListener(::onTapDetected)
        } catch (e: IllegalStateException) {
            Toast.makeText(this, "Sensor initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateAccelerometerValues(values: FloatArray) {
        val formattedValues = values.map { String.format("%.2f", it) }

        runOnUiThread {
            xAcc.text = formattedValues[0]
            yAcc.text = formattedValues[1]
            zAcc.text = formattedValues[2]
        }
    }

    private fun onTapDetected(values: FloatArray) {
        val (x, y, z) = values.map { abs(it) }
        Log.d("AccelerometerUtils", "onTapDetected: $x, $y, $z")

        runOnUiThread {
            when {
                x > y && x > z -> {
                    Log.d("AccelerometerUtils", "onTapDetected x")
                    xTaps++
                    xTapCount.text = "X Double Taps: $xTaps"
                }
                y > x && y > z -> {
                    Log.d("AccelerometerUtils", "onTapDetected y")
                    yTaps++
                    yTapCount.text = "Y Double Taps: $yTaps"
                }
                z > x && z > y -> {
                    Log.d("AccelerometerUtils", "onTapDetected z")
                    zTaps++
                    zTapCount.text = "Z Double Taps: $zTaps"
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (accelerometerUtils.isInitialized()) {
            accelerometerUtils.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        accelerometerUtils.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometerUtils.stopListening()
    }
}
