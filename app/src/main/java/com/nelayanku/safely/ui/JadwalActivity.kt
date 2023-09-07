package com.nelayanku.safely.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nelayanku.safely.R
import com.nelayanku.safely.adapter.JadwalAdapter
import com.nelayanku.safely.model.Jadwal
import com.nelayanku.safely.readJadwalFromFile

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
            }
        }
    }
}