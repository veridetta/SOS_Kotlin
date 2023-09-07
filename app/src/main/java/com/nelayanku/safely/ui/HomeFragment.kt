package com.nelayanku.safely.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.nelayanku.safely.R
import com.nelayanku.safely.readContactsFromFile
import com.nelayanku.safely.service.ShakeDetectorService
import io.fotoapparat.view.CameraView
import java.io.IOException
import java.net.URLEncoder
import java.util.Locale
import android.os.Build
import android.view.SurfaceHolder
import android.view.SurfaceView

import com.nelayanku.safely.service.RecorderService
import com.nelayanku.safely.service.RecordingService


class HomeFragment : Fragment(), SurfaceHolder.Callback  {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Tambahkan variabel untuk menyimpan lokasi
    var latitude = 0.0
    var longitude = 0.0
    lateinit var tvLat : TextView
    lateinit var tvLong : TextView
    lateinit var tvAddress : TextView
    //btnSos, imgCamera, imgVideo, imgAudio
    lateinit var btnSos : ImageView
    lateinit var imgCamera : ImageView
    lateinit var imgVideo : ImageView
    lateinit var imgAudio : ImageView
    lateinit var cbShowPreview : CheckBox
    lateinit var cbShake : CheckBox
    lateinit var cameraActive : TextView
    lateinit var videoActive : TextView
    lateinit var audioActive : TextView
    lateinit var modalContainer : RelativeLayout
    lateinit var whatsappButton : Button
    lateinit var smsButton : Button
    lateinit var tutupButton : Button
    private lateinit var camera: Camera
    var isCamera = true
    var isVideo = false
    var isAudio = false
    var isPreview = false
    var isShake = false
    var isRecording = false
    var isRecordAudio = false
    lateinit var cameraView : CameraView
    lateinit var framelayout : FrameLayout
    private val PERMISSION_REQUEST_CODE = 101

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        init(view)
        // Memeriksa dan meminta izin-izin yang diperlukan jika belum diizinkan
        checkAndRequestPermissions()
        getLastLocation()
        initMedia()
        btnClick()

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }
    private fun init(view: View){
        //init
        tvLat = view.findViewById(R.id.tvLat)
        tvLong = view.findViewById(R.id.tvLong)
        tvAddress = view.findViewById(R.id.tvAddress)
        btnSos = view.findViewById(R.id.btnSos)
        imgCamera = view.findViewById(R.id.btnCamera)
        imgVideo = view.findViewById(R.id.btnVideo)
        imgAudio = view.findViewById(R.id.btnAudio)
        modalContainer = view.findViewById(R.id.modalContainer)
        //gone
        modalContainer.visibility = View.GONE
        whatsappButton = view.findViewById(R.id.whatsappButton)
        smsButton = view.findViewById(R.id.smsButton)
        tutupButton = view.findViewById(R.id.closeButton)
        cameraActive = view.findViewById(R.id.cameraActive)
        videoActive = view.findViewById(R.id.videoActive)
        audioActive = view.findViewById(R.id.audioActive)
        cbShowPreview = view.findViewById(R.id.cbShowPreview)
        cbShake = view.findViewById(R.id.cbShake)
        framelayout= view.findViewById(R.id.previewView)
        mSurfaceView = view.findViewById(R.id.surfaceView1)
        mSurfaceHolder = mSurfaceView!!.holder
        mSurfaceHolder!!.addCallback(this)
        mSurfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }
    private fun btnClick(){
        cbShowPreview.setOnClickListener {
            if (cbShowPreview.isChecked) {
                //if isCamera maka akan scroll dan focus ke surfaceView
                if (isCamera || isVideo){
                    //tampilkan surfaceView
                    isPreview = true
                    mSurfaceView!!.visibility = View.VISIBLE;
                    intentPreview()
                }else{

                }
                // Tampilkan preview kamera
                Toast.makeText(requireContext(), "Show Preview", Toast.LENGTH_SHORT).show()
            } else {
                // Sembunyikan preview kamera
                isPreview = false
                mSurfaceView!!.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Hide Preview", Toast.LENGTH_SHORT).show()
            }
        }
        btnSos.setOnClickListener {
            modalContainer.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "SOS", Toast.LENGTH_SHORT).show()
        }
        cbShake.setOnClickListener {
            if (cbShake.isChecked) {
                isShake = true
                //simpan ke sharedpreferences
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("shake", true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    editor.apply()
                } // Simpan perubahan
                //aktifkan service
                val serviceIntent = Intent(requireContext(), ShakeDetectorService::class.java)
                requireActivity().stopService(serviceIntent)
                requireActivity().startService(serviceIntent)
                Toast.makeText(requireContext(), "Shake", Toast.LENGTH_SHORT).show()
            } else {
                isShake = false
                //simpan ke sharedpreferences
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("shake", false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    editor.apply()
                } // Simpan perubahan
                val serviceIntent = Intent(requireContext(), ShakeDetectorService::class.java)
                requireActivity().stopService(serviceIntent)
                Toast.makeText(requireContext(), "No Shake", Toast.LENGTH_SHORT).show()
            }
        }
        whatsappButton.setOnClickListener {
            //pesan gabungan lat long dan address
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val lat = sharedPreferences.getFloat("latitude", 0.0f)
            val long = sharedPreferences.getFloat("longitude", 0.0f)
            val address = sharedPreferences.getString("address", "Alamat tidak ditemukan")
            val defPesan = "Saya sedang dalam bahaya, lokasi saya saat ini di {{address}}, latitude {{lat}}," +
                    " longitude {{long}}"
            var pesan = sharedPreferences.getString("pesan", defPesan)
            //extract text pesan ganti {{address}} dengan address, {{lat}} dengan lat, {{long}} dengan long
            val pesan1 = pesan?.replace("{{address}}", address.toString())
            val pesan2 = pesan1?.replace("{{lat}}", lat.toString())
            val pesan3 = pesan2?.replace("{{long}}", long.toString())
            pesan = pesan3
            //nomor hp ambil dari contact json where utama = true
            val contacts = readContactsFromFile(requireContext())
            //filter utama = true
            val utamaTrueContacts = contacts.filter { it.utama }
            //cek data jika tidak ada
            if (utamaTrueContacts.isEmpty()){
                Toast.makeText(requireContext(), "Tidak ada kontak utama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val phone = utamaTrueContacts[0].phone
            //convert phone ke 62
            val phone62 = phone.replaceFirst("0", "62")
            //kirim pesan ke whatsapp melalui intent
            val url = "https://api.whatsapp.com/send?phone=$phone62&text=$pesan"
            // Membuat Uri untuk intent WhatsApp dengan nomor telepon dan pesan
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone62&text=${URLEncoder.encode(pesan, "UTF-8")}")

            // Membuat intent untuk membuka WhatsApp
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // Periksa apakah WhatsApp terinstal di perangkat
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // WhatsApp tidak terinstal, tampilkan pesan kesalahan atau tindakan yang sesuai
                Toast.makeText(requireContext(), "WhatsApp tidak terinstal. Membuka web", Toast.LENGTH_SHORT).show()
                // Buka WhatsApp Web di browser
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(webIntent)
            }
            Toast.makeText(requireContext(), "Whatsapp", Toast.LENGTH_SHORT).show()
        }
        smsButton.setOnClickListener {
            Toast.makeText(requireContext(), "SMS", Toast.LENGTH_SHORT).show()
            //pesan gabungan lat long dan address
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val lat = sharedPreferences.getFloat("latitude", 0.0f)
            val long = sharedPreferences.getFloat("longitude", 0.0f)
            val address = sharedPreferences.getString("address", "Alamat tidak ditemukan")
            val defPesan = "Saya sedang dalam bahaya, lokasi saya saat ini di {{address}}, latitude {{lat}}," +
                    " longitude {{long}}"
            var pesan = sharedPreferences.getString("pesan", defPesan)
            //extract text pesan ganti {{address}} dengan address, {{lat}} dengan lat, {{long}} dengan long
            val pesan1 = pesan?.replace("{{address}}", address.toString())
            val pesan2 = pesan1?.replace("{{lat}}", lat.toString())
            val pesan3 = pesan2?.replace("{{long}}", long.toString())
            pesan = pesan3
            //nomor hp ambil dari contact json where utama = true
            val contacts = readContactsFromFile(requireContext())
            //filter utama = true
            val utamaTrueContacts = contacts.filter { it.utama }
            if (utamaTrueContacts.isEmpty()){
                Toast.makeText(requireContext(), "Tidak ada kontak utama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val phone = utamaTrueContacts[0].phone
            //kirim pesan melalui sms
            val smsIntent = Intent(Intent.ACTION_SENDTO)
            smsIntent.data = Uri.parse("smsto:$phone")
            smsIntent.putExtra("sms_body", pesan)
            startActivity(smsIntent)

        }
        tutupButton.setOnClickListener {
            modalContainer.visibility = View.GONE
        }
        //imgCamera, imgVideo, imgAudio diklik maka akan checkMedia
        imgCamera.setOnClickListener {
            isCamera = true
            checkMedia()
            val intent = Intent(requireContext(), RecorderService::class.java)
            intent.putExtra("picture", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            requireActivity().startService(intent)
            //timer 1detik
            val handler = android.os.Handler()
            handler.postDelayed({
                Toast.makeText(requireContext(), "Gambar diambil", Toast.LENGTH_SHORT).show()
                //klik switchbutton sampe ke false
                mSurfaceView!!.visibility = View.GONE;
                cbShowPreview.isChecked = false
                cameraActive.text = "Click to take!"
                //stop
                requireActivity().stopService(Intent(requireContext(), RecorderService::class.java))
            }, 3000)
        }
        imgVideo.setOnClickListener {
            isVideo = true
            if(isRecording){
                requireActivity().stopService(Intent(requireContext(), RecorderService::class.java))
                Toast.makeText(requireContext(), "Rekaman dihentikan", Toast.LENGTH_SHORT).show()
                videoActive.text = "Running"
                isRecording = false
            }else{
                val intent = Intent(requireContext(), RecorderService::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireActivity().startService(intent)
                //finish()
                videoActive.text = "Video Recording"
                Toast.makeText(requireContext(), "Rekaman dimulai", Toast.LENGTH_SHORT).show()
                isRecording = true
            }
            checkMedia()
        }
        imgAudio.setOnClickListener {
            isAudio = true
            checkMedia()
            if(isRecording){
                requireActivity().stopService(Intent(requireContext(), RecordingService::class.java))
                audioActive.text = "Running"
                isRecording = false
            }else{
                val intent = Intent(requireContext(), RecordingService::class.java)
                audioActive.text = "Audio Recording"
                Toast.makeText(requireContext(), "Rekaman dimulai", Toast.LENGTH_SHORT).show()
                isRecording = true
            }
        }
    }
    private fun initMedia(){
        //sembunyikan semua cameraActive, videoActive, audioActive
        cameraActive.visibility = View.INVISIBLE
        videoActive.visibility = View.INVISIBLE
        audioActive.visibility = View.INVISIBLE

    }
    private fun checkMedia(){
        //jika isCamera maka cameraActive akan muncul, yang lainnya akan false dan gone
        if (isCamera){
            isVideo = false
            isAudio = false
            cameraActive.visibility = View.VISIBLE
            videoActive.visibility = View.INVISIBLE
            audioActive.visibility = View.INVISIBLE
        }else if(isVideo) {
            isCamera = false
            isAudio = false
            cameraActive.visibility = View.INVISIBLE
            videoActive.visibility = View.VISIBLE
            audioActive.visibility = View.INVISIBLE
        }else if(isAudio){
            isCamera = false
            isVideo = false
            cameraActive.visibility = View.INVISIBLE
            videoActive.visibility = View.INVISIBLE
            audioActive.visibility = View.VISIBLE
        }
    }
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Dapatkan latitude dan longitude di sini
                 latitude = location?.latitude ?: 0.0
                 longitude = location?.longitude ?: 0.0
                // Tambahkan kode untuk menangani lokasi yang didapatkan
                tvLat.text = "Latitude "+ latitude.toString()
                tvLong.text = "Longitude " + longitude.toString()
                val addres = getAddress(requireContext(), latitude, longitude)
                //mengambil alamat
                tvAddress.text = addres
                //menyimpan latitude, long, dan alamat ke sharedpreferences
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putFloat("latitude", latitude.toFloat())
                editor.putFloat("longitude", longitude.toFloat())
                editor.putString("address", addres)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    editor.apply()
                } // Simpan perubahan

            }
            .addOnFailureListener { exception ->
                // Handle error jika gagal mendapatkan lokasi
            }
    }
    private fun getAddress(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var addressText = ""

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val addressParts = ArrayList<String>()

                // Mendapatkan alamat dalam bentuk teks
                if (address.maxAddressLineIndex >= 0) {
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }
                }

                // Menggabungkan bagian-bagian alamat menjadi satu teks
                addressText = addressParts.joinToString(separator = "\n")
            }
        } catch (e: IOException) {
            // Tangani kesalahan jika terjadi
            e.printStackTrace()
        }

        return addressText
    }
    // Fungsi untuk memeriksa dan meminta izin-izin yang diperlukan
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.USE_FULL_SCREEN_INTENT
        )
        val permissionsToRequest = ArrayList<String>()

        // Memeriksa izin-izin yang belum diizinkan
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }
        // Jika ada izin yang belum diizinkan, maka minta izin
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
    // Override untuk menangani hasil permintaan izin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Memeriksa izin-izin yang diizinkan
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // Izin diizinkan
                } else {
                    // Izin ditolak
                }
            }
        }
    }
    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        var mSurfaceView: SurfaceView? = null
        var mSurfaceHolder: SurfaceHolder? = null
        var mCamera: Camera? = null
        var mPreviewRunning = false
    }
    private fun intentPreview(){
        val intent = Intent(requireContext(), RecorderService::class.java)
        intent.putExtra("preview", true)
        requireActivity().startService(intent)
    }
    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}
