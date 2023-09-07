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

        // Mengaktifkan wakelock ketika layanan dibuat
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Melepas wakelock ketika layanan dihancurkan
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
