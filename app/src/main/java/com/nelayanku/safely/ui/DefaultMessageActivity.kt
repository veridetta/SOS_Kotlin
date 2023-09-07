package com.nelayanku.safely.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.nelayanku.safely.R

class DefaultMessageActivity : AppCompatActivity() {
    //edittext etName
    private lateinit var etName: EditText
    //btnSimpan
    private lateinit var btnSimpan: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_message)
        init()
        getPesan()
    }

    fun init(){
        //inisialisasi etName
        etName = findViewById(R.id.etName)
        //inisialisasi btnSimpan
        btnSimpan = findViewById(R.id.btnSimpan)
        //setonclicklistener btnSimpan
        btnSimpan.setOnClickListener {
            //jika etName tidak kosong
            if (etName.text.toString() != ""){
                //simpan ke sharedpreferences
                //simpan ke sharedpreferences
                val editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit()
                editor.putString("pesan", etName.text.toString())
                editor.apply()
                //finish
                finish()
            }else{
                //jika kosong, tampilkan toast
                //jika kosong, tampilkan toast
                Toast.makeText(this, "Pesan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun getPesan(){
        //ambil dari sharedpreferences
        val defPesan = "Saya sedang dalam bahaya, lokasi saya saat ini di {{address}}, latitude {{lat}}," +
                " longitude {{long}}"
        val prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val pesan = prefs.getString("pesan", defPesan)
        //set ke etName
        etName.setText(pesan)

    }
}