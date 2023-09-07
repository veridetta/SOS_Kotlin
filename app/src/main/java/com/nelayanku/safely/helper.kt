package com.nelayanku.safely

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nelayanku.safely.model.Contact
import com.nelayanku.safely.model.Jadwal
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.util.UUID

// Fungsi untuk menyimpan data kontak ke file JSON
public fun writeContactsToFile(context: Context, contacts: ArrayList<Contact>) {
    val file = File(context.filesDir, "contacts.json")
    val gson = Gson()
    val writer = FileWriter(file)
    gson.toJson(contacts, writer)
    writer.close()
}
// Fungsi untuk membaca data kontak dari file JSON
public fun readContactsFromFile(context: Context): ArrayList<Contact> {
    val file = File(context.filesDir, "contacts.json")
    val gson = Gson()
    val type: Type = object : TypeToken<ArrayList<Contact>>() {}.type

    return if (file.exists()) {
        val reader = FileReader(file)
        val contacts = gson.fromJson<ArrayList<Contact>>(reader, type)
        reader.close()
        contacts
    } else {
        ArrayList()
    }
}
public fun readJadwalFromFile(context: Context): ArrayList<Jadwal> {
    val file = File(context.filesDir, "jadwal.json")
    val gson = Gson()
    val type: Type = object : TypeToken<ArrayList<Jadwal>>() {}.type

    return if (file.exists()) {
        val reader = FileReader(file)
        val contacts = gson.fromJson<ArrayList<Jadwal>>(reader, type)
        reader.close()
        contacts
    } else {
        ArrayList()
    }
}
 fun writeJadwalToFile(context: Context, contacts: ArrayList<Jadwal>) {
    val file = File(context.filesDir, "jadwal.json")
    val gson = Gson()
    val writer = FileWriter(file)
    gson.toJson(contacts, writer)
    writer.close()
}
var isNotif = false
 fun showNotification(title:String, message:String, context: Context) {
    val notificationId = UUID.randomUUID().hashCode()
    val notificationBuilder = NotificationCompat.Builder(context, "channel_id")
        .setSmallIcon(R.drawable.ic_sos) // Ganti dengan ikon notifikasi Anda
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    val notificationManager = NotificationManagerCompat.from(context)
    // Menampilkan notifikasi
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
        ) {
            when (requestCode) {
                1 -> {
                    // If request is cancelled, the result arrays are empty.
                    isNotif = (grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    return
                }
            }
        }
        return
    }

        notificationManager.notify(notificationId, notificationBuilder.build())
}
