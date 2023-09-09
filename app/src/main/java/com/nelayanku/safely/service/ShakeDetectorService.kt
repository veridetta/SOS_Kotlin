package com.nelayanku.safely.service
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateFormat
import java.io.File
import java.util.Date
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
        // Mengambil status checkbox dari SharedPreferences
        val isShakeDetectorEnabled = sharedPreferences.getBoolean("shake", false)
        if (isShakeDetectorEnabled) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // Mengambil sensitivitas dari SharedPreferences saat service dibuat
        sensitivityThreshold = sharedPreferences.getInt("shakeValue", 10)
        // Mengambil status checkbox dari SharedPreferences
        val isShakeDetectorEnabled = sharedPreferences.getBoolean("shake", false)
        if (isShakeDetectorEnabled) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
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
                    var modex = ""
                    var mode = "Belakang"
                    if(mode== "Depan"){
                        modex = "camera_front"
                    }else{
                        modex = "camera_back"
                    }
                    val mMediaRecorder = MediaRecorder()
                    // Setel kamera sesuai dengan mode yang diterima
                    val cameraId = when (modex) {
                        "camera_front" -> Camera.CameraInfo.CAMERA_FACING_FRONT
                        "camera_back" -> Camera.CameraInfo.CAMERA_FACING_BACK
                        else -> Camera.CameraInfo.CAMERA_FACING_FRONT // Atur default sesuai kebutuhan Anda
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                        mMediaRecorder.setCamera(Camera.open(cameraId))
                    }
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    val photoDirectory = File(getExternalFilesDir("sos-app"), "media")
                    // Membuat direktori jika belum ada
                    if (!photoDirectory.exists()) {
                        photoDirectory.mkdirs()
                    }
                    mMediaRecorder.setOutputFile(
                        photoDirectory.toString() + "/" +
                                DateFormat.format("yyyy-MM-dd_kk-mm-ss", Date().time) +
                                ".mp4"
                    )
                    try {
                        mMediaRecorder.prepare()
                        // Schedule the stopRecording() method to be called after the specified duration
                        //start
                        val recordingDuration = 15
                        // Mulai perekaman
                        mMediaRecorder.start()
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            vibrator.vibrate(200)
                        }
                        val handler = Handler()
                        handler.postDelayed({
                            mMediaRecorder.stop()
                            mMediaRecorder.reset()
                            mMediaRecorder.release()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(200)
                            }
                        },(recordingDuration * 1000 * 60).toLong()) // Convert duration to milliseconds
                    } catch (e: Exception) {
                        // Tangani jika terjadi kesalahan saat memulai perekaman
                        e.printStackTrace()
                        // Hentikan dan lepaskan MediaRecorder jika terjadi kesalahan
                        mMediaRecorder.reset()
                        mMediaRecorder.release()
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
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Sesuaikan dengan cara Anda mengganti sensitivitas, misalnya dari SharedPreferences
            val newSensitivity = sharedPreferences.getInt("shakeValue", 10)
            setSensitivity(newSensitivity)
        }
    }

    override fun onDestroy() {
       // super.onDestroy()
        //sensorManager.unregisterListener(this)
    }
}

