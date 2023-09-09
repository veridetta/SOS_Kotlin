package com.nelayanku.safely.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager

class WakelockService : Service() {

    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MyApp:KeepScreenOn"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_START_SERVICE" && !wakeLock.isHeld) {
            // Mengaktifkan wakelock ketika layanan dimulai
            wakeLock.acquire(30 * 60 * 1000L /*30 minutes*/)
        } else if (intent?.action == "ACTION_STOP_SERVICE" && wakeLock.isHeld) {
            // Menonaktifkan wakelock ketika layanan dihentikan
            wakeLock.release()
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
