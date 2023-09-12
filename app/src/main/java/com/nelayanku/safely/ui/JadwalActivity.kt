package com.nelayanku.safely.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.nelayanku.safely.MainActivity
import com.nelayanku.safely.R
import com.nelayanku.safely.adapter.JadwalAdapter
import com.nelayanku.safely.model.Jadwal
import com.nelayanku.safely.readJadwalFromFile
import com.nelayanku.safely.service.RecordingAlarmReceiver
import com.nelayanku.safely.writeJadwalToFile

class JadwalActivity : AppCompatActivity() {
    lateinit var lyKosong : LinearLayout
    //btnTambahJadwal, rvJadwal
    lateinit var btnTambahJadwal : Button
    lateinit var rvJadwal : RecyclerView
    //adapter
    lateinit var adapter : JadwalAdapter
    var listJadwal : ArrayList<Jadwal> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jadwal)
        init()
        btnClick()
        initRc()
    }
    fun init(){
        lyKosong = findViewById(R.id.lyKosong)
        btnTambahJadwal = findViewById(R.id.btnTambahJadwal)
        rvJadwal = findViewById(R.id.rvJadwal)
    }
    fun btnClick(){
        btnTambahJadwal.setOnClickListener {
            //intent ke AddJadwalActivity
            val intent = Intent(this, AddJadwalActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun initRc(){
        listJadwal.clear()
        listJadwal.addAll(readJadwalFromFile(this))
        val sortedJadwal = listJadwal.sortedBy { it.tanggal }
        //ifjadwal kosong
        if (sortedJadwal.isEmpty()){
            lyKosong.visibility = LinearLayout.VISIBLE
            rvJadwal.visibility = RecyclerView.GONE
        }else {
            rvJadwal.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(this@JadwalActivity, 1)
                // set the custom adapter to the RecyclerView
                adapter = JadwalAdapter(
                    sortedJadwal,
                    this@JadwalActivity
                )
                //tambah klik
                { jadwal -> deleteRecordingSchedule(jadwal) }
            }
        }
    }
    fun deleteRecordingSchedule(jadwal : Jadwal){
        //baca file dari json
        val jadwals = readJadwalFromFile(this)
        //temukan jadwal yang akan dihapus berdasarkan uid
        var jadwalToDelete = jadwals.find { it.uid == jadwal.uid }
        //hapus jadwal
        jadwals.remove(jadwalToDelete)
        //simpan ulang ke json
        writeJadwalToFile(this, jadwals)
        //batalkan alarm
        cancelRecordingSchedule(jadwal.tanggal, jadwal.waktu, jadwal.mode)
        //tampilkan snackbar
        // Tampilkan Snackbar berhasil menyimpan perubahan
        Snackbar.make(rvJadwal, "Data berhasil dihapus", Snackbar.LENGTH_SHORT).show()
        //refresh adapter
        initRc()

    }
    private fun cancelRecordingSchedule(tanggal: String, waktu: String, mode: String) {
        // Buat intent untuk memulai perekaman (sama seperti yang digunakan sebelumnya)
        val startIntent = Intent(this, RecordingAlarmReceiver::class.java)
        startIntent.action = "START_RECORDING_ACTION"
        startIntent.putExtra("tanggal", tanggal)
        startIntent.putExtra("waktu", waktu)
        startIntent.putExtra("mode", mode)

        // Buat PendingIntent untuk memulai perekaman
        val startPendingIntent = PendingIntent.getBroadcast(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Dapatkan AlarmManager
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Batalkan PendingIntent yang sesuai
        alarmManager.cancel(startPendingIntent)

        // Anda juga perlu membatalkan PendingIntent untuk menghentikan perekaman jika diperlukan
        // Buat intent untuk menghentikan perekaman (sama seperti yang digunakan sebelumnya)
        val stopIntent = Intent(this, RecordingAlarmReceiver::class.java)
        stopIntent.action = "STOP_RECORDING_ACTION"
        stopIntent.putExtra("tanggal", tanggal)
        stopIntent.putExtra("waktu", waktu)
        stopIntent.putExtra("mode", mode)

        // Buat PendingIntent untuk menghentikan perekaman
        val stopPendingIntent = PendingIntent.getBroadcast(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Batalkan PendingIntent yang sesuai
        alarmManager.cancel(stopPendingIntent)
    }
    override fun onResume() {
        super.onResume()
        initRc()
    }
}