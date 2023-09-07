package com.nelayanku.safely.service
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import kotlin.math.sqrt

class ShakeDetectorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var sensitivityThreshold: Int = 10 // Nilai sensitivitas default
    private var lastTimestamp: Long = 0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // Mengambil sensitivitas dari SharedPreferences saat service dibuat
        sensitivityThreshold = sharedPreferences.getInt("shakeValue", 10)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Fungsi untuk mengatur tingkat akurasi (sensitivitas) Shake Detector
    fun setSensitivity(sensitivity: Int) {
        sensitivityThreshold = sensitivity
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTimestamp > 2000) { // 100ms interval to avoid multiple detections
                lastTimestamp = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt((x * x + y * y + z * z).toDouble())

                if (acceleration > sensitivityThreshold) {
                    // Device is shaken with the desired sensitivity, trigger vibration
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        vibrator.vibrate(200)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Sesuaikan dengan cara Anda mengganti sensitivitas, misalnya dari SharedPreferences
            val newSensitivity = sharedPreferences.getInt("shakeValue", 10)
            setSensitivity(newSensitivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

