package com.nelayanku.safely.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RecordingAlarmReceiver : BroadcastReceiver() {
    lateinit var tanggal: String
    lateinit var waktu: String
    lateinit var durasi: String
    lateinit var mode: String
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val action = intent.action
             tanggal = intent?.getStringExtra("tanggal").toString()
             waktu = intent?.getStringExtra("waktu").toString()
             durasi = intent?.getStringExtra("durasi").toString()
             mode = intent?.getStringExtra("mode").toString()
            if (action == "START_RECORDING_ACTION") {
                // Mulai perekaman video di sini
                startRecording(context)
            } else if (action == "STOP_RECORDING_ACTION") {
                // Berhenti perekaman video di sini
                stopRecording(context)
            }
        }
    }

    private fun startRecording(context: Context) {
        // Logika untuk memulai perekaman video, misalnya menggunakan layanan yang telah Anda buat
        val serviceIntent = Intent(context, ScheduledRecordingService::class.java)
        serviceIntent.putExtra("action", "start_recording")
        serviceIntent.putExtra("tanggal", tanggal)
        serviceIntent.putExtra("waktu", waktu)
        serviceIntent.putExtra("durasi", durasi)
        serviceIntent.putExtra("mode", mode)
        context.startService(serviceIntent)
    }

    private fun stopRecording(context: Context) {
        // Logika untuk menghentikan perekaman video, misalnya menggunakan layanan yang telah Anda buat
        val serviceIntent = Intent(context, ScheduledRecordingService::class.java)
        serviceIntent.putExtra("action", "stop_recording")
        context.startService(serviceIntent)
    }
}

