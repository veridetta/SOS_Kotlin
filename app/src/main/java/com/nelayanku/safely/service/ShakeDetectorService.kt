package com.nelayanku.safely.service
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateFormat
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.nelayanku.safely.R
import com.nelayanku.safely.ui.HomeFragment.Companion.mSurfaceHolder
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ShakeDetectorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var sensitivityThreshold: Int = 10 // Nilai sensitivitas default
    private var lastTimestamp: Long = 0
    private lateinit var sharedPreferences: SharedPreferences

    private val TAG = "VideoRecordingService"
    private val NOTIFICATION_ID = 1

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        }
        // Mengambil sensitivitas dari SharedPreferences saat service dibuat
        sensitivityThreshold = sharedPreferences.getInt("shakeValue", 10)
        // Mengambil status checkbox dari SharedPreferences
        val isShakeDetectorEnabled = sharedPreferences.getBoolean("shake", false)
        //check video, audio, photo


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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("LongLogTag")
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
                    sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val video = sharedPreferences.getBoolean("video", false)
                    val audio = sharedPreferences.getBoolean("audio", false)
                    val photo = sharedPreferences.getBoolean("photo", false)
                    //if video
                    if (sharedPreferences.getBoolean("video", false)) {
                        //check jika isrecording true
                        val isRecording = sharedPreferences.getBoolean("isRecording", false)
                        if (isRecording) {
                            //
                            val intent = Intent(this, RecorderService::class.java)
                            intent.putExtra("preview", false)
                            intent.putExtra("picture", false)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            stopService(intent)
                            //set isRecording false
                            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isRecording", false)
                            editor.apply()
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(200)
                            }
                            //toast
                            Toast.makeText(
                                baseContext,
                                "Recording stop",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            //start service dari recorderService
                            val intent = Intent(this, RecorderService::class.java)
                            intent.putExtra("preview", false)
                            intent.putExtra("picture", false)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startService(intent)
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(200)
                            }
                            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isRecording", true)
                            editor.apply()

                            //toast
                            Toast.makeText(
                                baseContext,
                                "Recording start",
                                Toast.LENGTH_SHORT
                            ).show()
                            //handler
                            val handler = Handler()
                            handler.postDelayed({
                                //stop service dari recorderService
                                val intent = Intent(this, RecorderService::class.java)
                                intent.putExtra("preview", false)
                                intent.putExtra("picture", false)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                stopService(intent)
                                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    vibrator.vibrate(200)
                                }
                                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isRecording", false)
                                editor.apply()
                                //toast
                                Toast.makeText(
                                    baseContext,
                                    "Recording stop",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }, (1 * 1000 * 15 * 60).toLong()) // Convert duration to milliseconds
                        }
                    }
                    //if audio
                    if (sharedPreferences.getBoolean("audio", false)) {
                        val isRecording = sharedPreferences.getBoolean("isRecording", false)
                        if (isRecording) {
                            //
                            val intent = Intent(this, RecordingService::class.java)
                            Toast.makeText(this, "Recording stop", Toast.LENGTH_SHORT).show()
                            stopService(intent)
                            //set isRecording false
                            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isRecording", false)
                            editor.apply()
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(200)
                            }
                            //toast
                            Toast.makeText(
                                baseContext,
                                "Recording stop",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            //start service dari recorderService
                            val intent = Intent(this, RecordingService::class.java)
                            startService(intent)
                            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(200)
                            }
                            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isRecording", true)
                            editor.apply()

                            //toast
                            Toast.makeText(
                                baseContext,
                                "Recording start",
                                Toast.LENGTH_SHORT
                            ).show()
                            //handler
                            val handler = Handler()
                            handler.postDelayed({
                                //stop service dari recorderService
                                val intent = Intent(this, RecordingService::class.java)
                                stopService(intent)
                                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    vibrator.vibrate(200)
                                }
                                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isRecording", false)
                                editor.apply()
                                //toast
                                Toast.makeText(
                                    baseContext,
                                    "Recording stop",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }, (1 * 1000 * 15 * 60).toLong()) // Convert duration to milliseconds
                        }
                    }
                    //if photo
                    if (sharedPreferences.getBoolean("photo", false)) {
                        val intentx = Intent(this, RecorderService::class.java)
                        intentx.putExtra("preview", false)
                        intentx.putExtra("picture", false)
                        intentx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        stopService(intentx)

                        val intent = Intent(this, RecorderService::class.java)
                        intent.putExtra("picture", true)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startService(intent)
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

