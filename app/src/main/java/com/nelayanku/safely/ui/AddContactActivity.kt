package com.nelayanku.safely.ui

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nelayanku.safely.MainActivity
import com.nelayanku.safely.R
import com.nelayanku.safely.model.Contact
import com.nelayanku.safely.readContactsFromFile
import com.nelayanku.safely.writeContactsToFile
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type
import java.util.UUID

class AddContactActivity : AppCompatActivity() {
    lateinit var etName : EditText
    lateinit var etPhone : EditText
    lateinit var btnSave : Button
    //string name dan phone
    lateinit var name : String
    lateinit var phone : String
    var utama : Boolean = false
    var edit : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        initElemen()
        btnClick()
    }

    fun initElemen(){
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)
        var intentx = intent
        name = intentx.getStringExtra("nama").toString()
        phone = intentx.getStringExtra("phone").toString()
        etName.setText(name)
        etPhone.setText(phone)
    }
    fun btnClick(){
        btnSave.setOnClickListener {
            name = etName.text.toString()
            phone = etPhone.text.toString()
            utama = false
            var intentx = intent
             edit = intentx.getBooleanExtra("edit",false)
            //uid
            //cek apakah ada yang kosong
            if (name.isEmpty() || phone.isEmpty()){
                //jika ada yang kosong
                //tampilkan pesan
                Snackbar.make(it, "Data tidak boleh kosong", Snackbar.LENGTH_LONG).show()
            }else{
                if(edit){
                    val uid = intentx.getStringExtra("uid")
                    // Jika dalam mode edit, maka lakukan perubahan data kontak berdasarkan UID

                    // Baca data kontak yang ada dari file JSON
                    val contacts = readContactsFromFile(this)

                    // Temukan kontak yang akan diubah berdasarkan UID
                    var contactToEdit = contacts.find { it.uid == uid }

                    if (contactToEdit != null) {
                        // Ubah data kontak yang ditemukan
                        contactToEdit.name = name
                        contactToEdit.phone = phone

                        // Simpan ulang data kontak ke file JSON
                        writeContactsToFile(this, contacts)
                        // Tampilkan Snackbar berhasil menyimpan perubahan
                        Snackbar.make(it, "Data berhasil diubah", Snackbar.LENGTH_SHORT).show()

                        // Dalam 2 detik intent ke fragment contact
                        intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("fragment", "contact")
                        Handler().postDelayed({
                            startActivity(intent)
                        }, 2000)
                    }
                }else{
                    // Buat objek Contact baru
                    val uid = UUID.randomUUID().toString()
                    val contact = Contact(name, phone, utama, uid)
                    // Baca data kontak yang ada dari file JSON (jika ada)
                    // Tampilkan dialog progress
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Menyimpan data...")
                    progressDialog.show()

                    // Baca data kontak yang ada dari file JSON (jika ada)
                    val contacts = readContactsFromFile(this)

                    // Tambahkan objek Contact baru ke daftar kontak
                    contacts.add(contact)

                    // Simpan ulang data kontak ke file JSON
                    writeContactsToFile(this,contacts)

                    // Sembunyikan dialog progress
                    progressDialog.dismiss()

                    // Tampilkan Snackbar berhasil menyimpan
                    Snackbar.make(it, "Data berhasil disimpan", Snackbar.LENGTH_SHORT).show()
                    //dalam 2 detik intent ke fragment contact
                    intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("fragment", "contact")
                    Handler().postDelayed({
                        startActivity(intent)
                    }, 1000)
                }

            }

        }
    }

}