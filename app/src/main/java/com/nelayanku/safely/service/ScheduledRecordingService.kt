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
import androidx.annotation.RequiresApi
import java.io.File
import java.util.Calendar
import java.util.Date

class ScheduledRecordingService : Service() {
    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
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
    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    private fun startRecording(tanggal: String, waktu: String, durasi: String, mode: String) {
        //simpan mode ke sharedpreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("mode", mode)
        editor.apply()
        //startservice recorder
        val intent = Intent(this, RecorderService::class.java)
        intent.putExtra("tanggal", tanggal)
        intent.putExtra("waktu", waktu)
        intent.putExtra("durasi", durasi)
        intent.putExtra("mode", mode)
        startService(intent)
        //handler sesuai durasi
        val handler = Handler()
        handler.postDelayed({
            //intent stop service
            val intentx = Intent(this, RecorderService::class.java)
            intentx.action = "ACTION_STOP_SERVICE"
            stopService(intent)
        }, durasi.toLong() * 1000)
    }

}
