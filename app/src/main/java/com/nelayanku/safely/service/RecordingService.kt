package com.nelayanku.safely.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.nelayanku.myapplication.StringHelper
import com.nelayanku.safely.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecordingService : Service() {
    private var recorder: MediaRecorder? = null
    private var fileName: String? = null
    private var recordingDuration = 0
    override fun onCreate() {
        super.onCreate()
        recorder = MediaRecorder()
        recordingDuration = resources.getInteger(R.integer.N)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startRecording()

        // If we get killed, after returning from here, don't restart
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("LongLogTag")
    private fun startRecording() {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        val photoDirectory = File(getExternalFilesDir("sos-app"), "media")
        // Membuat direktori jika belum ada
        if (!photoDirectory.exists()) {
            photoDirectory.mkdirs()
        }
        fileName = photoDirectory.absolutePath + "/" + timeStamp + ".ogg"
        if (recorder == null) recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.OGG)
        recorder!!.setOutputFile(fileName)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            Log.e("RecordingService::startRecording()", "MediaRecorder::prepare() failed")
        }
        recorder!!.start()

        // Schedule the stopRecording() method to be called after the specified duration
        val handler = Handler()
        handler.postDelayed({
            resetRecording()
            startRecording()
        }, (recordingDuration * 1000 * 15 * 60).toLong()) // Convert duration to milliseconds
    }

    // Update the current number for the next recording
    private val nextRecordingNumber: String
        private get() {
            val preferences = getSharedPreferences("RecordingPrefs", MODE_PRIVATE)
            val currentNumber = preferences.getInt("currentNumber", 0)
            val nextNumber = currentNumber + 1
            val paddedNumber: String = StringHelper.padNumberWithZeros(nextNumber, 3)

            // Update the current number for the next recording
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                preferences.edit().putInt("currentNumber", nextNumber).apply()
            }
            return paddedNumber
        }

    private fun resetRecording() {
        if (recorder != null) {
            recorder!!.reset()
        }
    }

    private fun stopRecording() {
        if (recorder != null) {
            recorder!!.stop()
            recorder!!.release()
            recorder = null
        }
    }
}
