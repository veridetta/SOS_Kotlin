package com.nelayanku.safely.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.nelayanku.safely.MainActivity
import com.nelayanku.safely.R
import com.nelayanku.safely.model.Contact
import com.nelayanku.safely.model.Jadwal
import com.nelayanku.safely.readContactsFromFile
import com.nelayanku.safely.readJadwalFromFile
import com.nelayanku.safely.writeContactsToFile
import com.nelayanku.safely.writeJadwalToFile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddJadwalActivity : AppCompatActivity() {
    //etTanggal, etWaktu, etDurasi, spMode, btnSave
    lateinit var etTanggal: TextView
    lateinit var etWaktu: TextView
    lateinit var etDurasi: EditText
    lateinit var spMode: Spinner
    lateinit var btnSave: Button
    lateinit var sTanggal: String
    lateinit var sWaktu: String
    lateinit var sDurasi: String
    lateinit var sMode: String
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_jadwal)
        init()
        btnClick()
    }
    fun init(){
        etTanggal = findViewById(R.id.etTanggal)
        etWaktu = findViewById(R.id.etWaktu)
        etDurasi = findViewById(R.id.etDurasi)
        spMode = findViewById(R.id.spMode)
        btnSave = findViewById(R.id.btnSave)
        //set adapter spMode
        val adapter = ArrayAdapter.createFromResource(this, R.array.cam_mode, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMode.adapter = adapter

    }
    fun btnClick(){
        etTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        etWaktu.setOnClickListener {
            showTimePickerDialog()
        }
        btnSave.setOnClickListener {
            sTanggal = etTanggal.text.toString()
            sWaktu = etWaktu.text.toString()
            sDurasi = etDurasi.text.toString()
            sMode = spMode.selectedItem.toString()
            //cek apakah ada yang kosong
            if (sTanggal.isEmpty() || sWaktu.isEmpty() || sDurasi.isEmpty() || sMode.isEmpty()){
                //jika ada yang kosong
                //tampilkan pesan
                Snackbar.make(it, "Data tidak boleh kosong", Snackbar.LENGTH_LONG).show()
            }else{
                val uid = UUID.randomUUID().toString()
                val contact = Jadwal(sTanggal, sWaktu, sDurasi, sMode,uid)
                val contacts = readJadwalFromFile(this)
                // Tambahkan objek Contact baru ke daftar kontak
                contacts.add(contact)
                // Simpan ulang data kontak ke file JSON
                writeJadwalToFile(this,contacts)
                // Sembunyikan dialog progress
                // Tampilkan Snackbar berhasil menyimpan
                Snackbar.make(it, "Data berhasil disimpan", Snackbar.LENGTH_SHORT).show()
                //dalam 2 detik intent ke fragment contact
                intent = Intent(this, MainActivity::class.java)
                intent.putExtra("fragment", "setting")
                Handler().postDelayed({
                    startActivity(intent)
                }, 1000)
            }
        }
    }
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Proses hasil pilihan tanggal
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                etTanggal.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                // Proses hasil pilihan waktu
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                etWaktu.setText(timeFormat.format(selectedTime.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
}