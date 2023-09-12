package com.nelayanku.safely.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nelayanku.safely.MainActivity
import com.nelayanku.safely.R
import com.nelayanku.safely.adapter.ContactAdapter
import com.nelayanku.safely.model.Contact
import com.nelayanku.safely.readContactsFromFile
import com.nelayanku.safely.service.RecorderService
import com.nelayanku.safely.writeContactsToFile
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.lang.reflect.Type
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var btnAdd : LinearLayout
    lateinit var btnAddFromContact : LinearLayout
    lateinit var btnDefaultMessage : LinearLayout
    lateinit var rvContact : RecyclerView
    private val contactList: ArrayList<Contact> = ArrayList() // Untuk menyimpan data kontak
    //adapter
    private lateinit var adapter: ContactAdapter
    var utamaTrueContacts: List<Contact> = ArrayList() // Untuk menyimpan data kontak dengan utama=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        btnClick()
    }
    fun init(){
        btnAdd = view?.findViewById(R.id.btnAdd)!!
        btnAddFromContact = view?.findViewById(R.id.btnAddFromContact)!!
        btnDefaultMessage = view?.findViewById(R.id.btnDefaultMessage)!!
        rvContact = view?.findViewById(R.id.rvContact)!!
        // Load data kontak dari file JSON
        contactList.clear()
        contactList.addAll(readContactsFromFile(requireContext()))
        // Pisahkan kontak menjadi dua daftar: satu untuk utama=true dan satu lagi untuk utama=false
        utamaTrueContacts = contactList.filter { it.utama }
        val utamaFalseContacts = contactList.filterNot { it.utama }

        // Urutkan kedua daftar tersebut secara terpisah
        val sortedUtamaTrueContacts = utamaTrueContacts.sortedBy { it.name }
        val sortedUtamaFalseContacts = utamaFalseContacts.sortedBy { it.name }

        // Gabungkan kedua daftar tersebut
        val sortedContacts = mutableListOf<Contact>()
        sortedContacts.addAll(sortedUtamaTrueContacts)
        sortedContacts.addAll(sortedUtamaFalseContacts)
        // Sekarang sortedContacts berisi semua kontak dengan utama=true di paling atas dan kemudian urutan abjad

        rvContact.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            adapter = ContactAdapter(
                sortedContacts,
                requireContext(),
                { product ->
                    if (utamaTrueContacts.isNotEmpty()){
                        val handler = Handler()
                        handler.postDelayed({
                            ubahContactUtama(product)
                        }, 1000)
                    }
                    ubahContact(product)
                },
                { product -> editContact(product) },
                { product -> deleteContact(product) }
            )
        }

    }
    fun btnClick(){
        btnAdd.setOnClickListener{
            //intent ke add contact activity
            val intent = Intent(requireContext(), AddContactActivity::class.java)
            startActivity(intent)
        }
        btnAddFromContact.setOnClickListener{
            // Buat intent untuk membuka aplikasi kontak
            //val contactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            //startActivityForResult(contactIntent, CONTACT_PICK_REQUEST)
            selectContact()

        }
        btnDefaultMessage.setOnClickListener{
            //intent ke default message activity
            val intent = Intent(requireContext(), DefaultMessageActivity::class.java)
            startActivity(intent)
        }
    }
    //contact_pick_request
    private val CONTACT_PICK_REQUEST = 1
    fun selectContact() {
        // Start an activity for the user to pick a phone number from contacts
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(intent, CONTACT_PICK_REQUEST)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CONTACT_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            // Data kontak yang dipilih
            val contactData = data?.data

            // Lakukan operasi untuk mengimpor dan menyimpan data kontak ke dalam file JSON di sini
            if (contactData != null) {
                // Proses data kontak yang dipilih dan simpan ke dalam file JSON
                processAndSaveContact(contactData)
            }
        }
    }
    @SuppressLint("Range")
    private fun processAndSaveContact(contactUri: Uri) {
        // Gunakan ContentResolver untuk mengambil data kontak
        val projection: Array<String> = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            requireContext().contentResolver.query(contactUri, projection, null, null, null).use { cursor ->
                // If the cursor returned is valid, get the phone number
                var number = ""
                var name = ""
                if (cursor!!.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    number = cursor.getString(numberIndex)
                }

                val c: Cursor =  requireActivity().managedQuery(contactUri, null, null, null, null)
                if(c.moveToFirst()){
                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                }
                val uid = UUID.randomUUID().toString()
                // Buat objek Contact baru
                val contact = Contact(name, number, false, uid)
                val contacts = readContactsFromFile(requireContext())

                // Tambahkan objek Contact baru ke daftar kontak
                contacts.add(contact)

                // Simpan ulang data kontak ke file JSON
                writeContactsToFile(requireContext(),contacts)

                // Tampilkan Snackbar berhasil menyimpan
                Snackbar.make(requireView(), "Data berhasil disimpan", Snackbar.LENGTH_SHORT).show()
                //dalam 1 detik intent ke fragment contact
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.putExtra("fragment", "contact")
                Handler().postDelayed({
                    startActivity(intent)
                }, 1000)

            }
        }
    }

    //fungsi ubah contact
    private fun ubahContact(contact: Contact) {
        val uid = contact.uid
        val nama = contact.name
        val nomor = contact.phone
        val newContactData = Contact(nama, nomor, true,uid) // Ganti dengan data kontak yang baru
        // Baca data kontak dari file JSON
        val contacts = readContactsFromFile(requireContext())
        // Cari kontak dengan UID yang sesuai
        val contactIndex = contacts.indexOfFirst { it.uid == uid }

        if (contactIndex != -1) {
            // Jika kontak dengan UID yang sesuai ditemukan
            // Ubah data kontak dengan data dari newContact
            contacts[contactIndex] = newContactData
            // Simpan kembali data kontak ke file JSON
            writeContactsToFile(requireContext(), contacts)
        }
        //load fragment
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("fragment", "contact")
        startActivity(intent)
        //snakbar pada fragment
        Snackbar.make(requireView(), "Kontak utama berhasil diubah", Snackbar.LENGTH_SHORT).show()
    }
    private fun ubahContactUtama(contact: Contact) {
        // Ubah data kontak dengan UID yang sesuai
        val newContactData = Contact(utamaTrueContacts[0].name, utamaTrueContacts[0].phone, false, utamaTrueContacts[0].uid)
        // Baca data kontak dari file JSON
        val contacts = readContactsFromFile(requireContext())
        // Cari kontak dengan UID yang sesuai
        val contactIndex = contacts.indexOfFirst { it.uid == utamaTrueContacts[0].uid }
        if (contactIndex != -1) {
            // Jika kontak dengan UID yang sesuai ditemukan
            // Ubah data kontak dengan data dari newContact
            contacts[contactIndex] = newContactData
            // Simpan kembali data kontak ke file JSON
            writeContactsToFile(requireContext(), contacts)
        }
    }
    private fun editContact(contact: Contact) {
        val intent = Intent(requireContext(), AddContactActivity::class.java)
        intent.putExtra("nama", contact.name)
        intent.putExtra("phone", contact.phone)
        intent.putExtra("uid", contact.uid)
        intent.putExtra("edit", true)
        requireActivity().startActivity(intent)
    }
    private fun deleteContact(contact: Contact) {
        // Baca data kontak dari file JSON
        val contacts = readContactsFromFile(requireContext())
        // Cari kontak dengan UID yang sesuai
        val contactIndex = contacts.indexOfFirst { it.uid == contact.uid }
        if (contactIndex != -1) {
            // Jika kontak dengan UID yang sesuai ditemukan
            // Hapus kontak dari daftar kontak
            contacts.removeAt(contactIndex)
            // Simpan kembali data kontak ke file JSON
            writeContactsToFile(requireContext(), contacts)
        }
        //load fragment
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("fragment", "contact")
        startActivity(intent)
        //snakbar pada fragment
        Snackbar.make(requireView(), "Kontak berhasil dihapus", Snackbar.LENGTH_SHORT).show()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContactFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}