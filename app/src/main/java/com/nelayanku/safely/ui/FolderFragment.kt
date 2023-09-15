package com.nelayanku.safely.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.nelayanku.safely.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nelayanku.safely.adapter.ContactAdapter
import com.nelayanku.safely.adapter.MediaListAdapter
import com.nelayanku.safely.model.MediaItem
import io.fotoapparat.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class FolderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mediaListAdapter: MediaListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment ini
        val view = inflater.inflate(R.layout.fragment_folder, container, false)
        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.rvMedia)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //ambil mediaList dari folder
        val folder = File(requireContext().getExternalFilesDir("sos-app"), "media")
        val files = folder.listFiles()
        val mediaList = mutableListOf<MediaItem>()
        if (files != null) {
                for (file in files!!) {
                    val name = file.name
                    ///menghitung ukuran, jika lebih dari 1MB maka akan dijadikan MB
                    val size = if (file.length() / (1024 * 1024) > 1) {
                        "${file.length() / (1024 * 1024)} MB"
                    } else {
                        "${file.length() / 1024} KB"
                    }
                    val lastModified = SimpleDateFormat("dd/MM/yyyy").format(Date(file.lastModified()))
                    //ambil type file
                    val type = name.substring(name.lastIndexOf(".") + 1)
                    // Tambahkan data media ke daftar
                    mediaList.add(MediaItem(name, "", size, lastModified, type))
            }
        }
        // Inisialisasi adapter dengan daftar data media
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            adapter = MediaListAdapter(
                mediaList,
                { file -> gDrive(file) },
                { file -> deleteFile(file)},
                { file -> openMedia(file) }
            )
        }
        return view
    }
    //fungsi delete
    fun deleteFile(file: MediaItem) {
        val folder = File(requireContext().getExternalFilesDir("sos-app"), "media")
        val files = folder.listFiles()
        for (filex in files!!) {
            if (filex.name == file.name) {
                filex.delete()
            }
        }
        //refresh fragment
        val fragment = FolderFragment()
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
    //fungsi gdrive
    fun gDrive(file: MediaItem) {
        val folder = File(requireContext().getExternalFilesDir("sos-app"), "media")
        val files = folder.listFiles()
        for (file in files!!) {
            if (file.name == file.name) {
                //share intent ke gdive
                //jika tipe jpg
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "jpg") {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().opPackageName + ".provider",
                            file
                        )
                    } else {
                        TODO("VERSION.SDK_INT < Q")

                    }
                    shareIntent.type = "image/jpeg"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(Intent.createChooser(shareIntent, "Share Image"))
                }else if(file.name.substring(file.name.lastIndexOf(".") + 1) == "ogg"){
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "audio/ogg"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, file)
                    startActivity(Intent.createChooser(shareIntent, "Share Sound"))
                }else if(file.name.substring(file.name.lastIndexOf(".") + 1) == "mp4"){
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "video/mp4"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, file)
                    startActivity(Intent.createChooser(shareIntent, "Share Video"))
                }
            }
        }
    }
    fun openMedia(filenya: MediaItem){
        //ada media gambar audio dan video, saya ingin membukanya dengan aplikasi default
        val folder = File(requireContext().getExternalFilesDir("sos-app"), "media")
        val files = folder.listFiles()
        for (file in files!!) {
            if (file.name == filenya.name) {
                //jika tipe jpg
                if (file.name.substring(file.name.lastIndexOf(".") + 1) == "jpg") {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW

                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().opPackageName + ".provider",
                            file
                        )
                    } else {
                        TODO("VERSION.SDK_INT < Q")

                    }

                    intent.setDataAndType(uri, "image/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Izinkan akses ke URI
                    startActivity(intent)
                }
                else if(file.name.substring(file.name.lastIndexOf(".") + 1) == "ogg"){
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().opPackageName + ".provider",
                            file
                        )
                    } else {
                        TODO("VERSION.SDK_INT < Q")

                    }
                    intent.setDataAndType(uri, "audio/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Izinkan akses ke URI
                    startActivity(intent)
                }
                else if(file.name.substring(file.name.lastIndexOf(".") + 1) == "mp4"){
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().opPackageName + ".provider",
                            file
                        )
                    } else {
                        TODO("VERSION.SDK_INT < Q")

                    }
                    intent.setDataAndType(uri, "video/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Izinkan akses ke URI
                    startActivity(intent)
                }
            }
        }
    }
}
