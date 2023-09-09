package com.nelayanku.safely.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateFormat
import java.io.File
import java.util.Calendar
import java.util.Date

class ScheduledRecordingService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Logika penjadwalan perekaman video akan ditempatkan di sini
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, RecordingAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0)

        // Atur waktu penjadwalan (misalnya, setiap hari jam 15:00)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // Aktifkan alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY, // Interval harian
            pendingIntent
        )
        if (intent != null) {
            val action = intent.getStringExtra("action")

            // Mengecek apakah action adalah "start_recording"
            if (action == "start_recording") {
                val tanggal = intent.getStringExtra("tanggal").toString()
                val waktu = intent.getStringExtra("waktu").toString()
                val durasi = intent.getStringExtra("durasi").toString()
                val mode = intent.getStringExtra("mode").toString()

                // Lakukan tindakan yang sesuai dengan data yang diterima
                // Misalnya, Anda dapat memulai perekaman sesuai dengan tanggal, waktu, durasi, dan mode yang diterima.
                startRecording(tanggal, waktu, durasi, mode)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun startRecording(tanggal: String, waktu: String, durasi: String, mode: String) {
        // Inisialisasi MediaRecorder
        var modex = ""
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
            val recordingDuration = durasi.toInt() //dalam menit
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

        // ... (lanjutkan dengan kode lain yang diperlukan)
    }

}
