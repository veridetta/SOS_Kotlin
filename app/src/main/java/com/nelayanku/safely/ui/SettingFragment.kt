package com.nelayanku.safely.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import com.nelayanku.safely.R
import com.nelayanku.safely.service.ShakeDetectorService
import com.nelayanku.safely.service.WakelockService

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
class SettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    //btnTimer (Linearlayout), sbShake, swScreen
    lateinit var btnTimer : LinearLayout
    lateinit var sbShake : SeekBar
    lateinit var swScreen : SwitchCompat
    lateinit var tvShake : TextView
    lateinit var cbVideo : CheckBox
    lateinit var cbAudio : CheckBox
    lateinit var cbPhoto : CheckBox
    lateinit var spMode : Spinner

    var shakeValue : Int = 0
    var isScreenOn : Boolean = false

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
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        btnClick()
        initSharedPref()
    }
    fun init(){
        btnTimer = view?.findViewById(R.id.btnTimer)!!
        sbShake = view?.findViewById(R.id.sbShake)!!
        swScreen = view?.findViewById(R.id.swScreen)!!
        tvShake = view?.findViewById(R.id.tvShake)!!
        cbVideo = view?.findViewById(R.id.cbVideo)!!
        cbAudio = view?.findViewById(R.id.cbAudio)!!
        cbPhoto = view?.findViewById(R.id.cbPhoto)!!
        spMode = view?.findViewById(R.id.spMode)!!
        //set spMode ambil dari string
        val mode = resources.getStringArray(R.array.cam_mode)
        val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, mode) }
        spMode.adapter = adapter
    }

    fun btnClick(){
        btnTimer.setOnClickListener {
            //pindah ke jadwal activity
            val intent = Intent(activity, JadwalActivity::class.java)
            startActivity(intent)
        }
        sbShake.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                // TODO Auto-generated method stub
                shakeValue = progress
                tvShake.text = "$shakeValue"    //set textview dengan value dari seekbar

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // TODO Auto-generated method stub
            }
            @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // TODO Auto-generated method stub
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("shakeValue", shakeValue)
                editor.apply()
                //terapkan pada shakedetector untuk mengubah sensitivitas (ShakedetectorService)
                val serviceIntent = Intent(requireContext(), ShakeDetectorService::class.java)
                requireActivity().startService(serviceIntent)
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            }
        })
        swScreen.setOnCheckedChangeListener { buttonView, isChecked ->
            val intent = Intent(requireContext(), WakelockService::class.java)

            if (isChecked) {
                // Ketika switch diaktifkan (ON), maka kita memulai layanan
                intent.action = "ACTION_START_SERVICE"
                requireContext().startService(intent)
            } else {
                // Ketika switch dimatikan (OFF), maka kita menghentikan layanan
                intent.action = "ACTION_STOP_SERVICE"
                requireContext().stopService(intent)
            }
        }
        cbVideo.setOnClickListener {
            //uncheck pada yang lain
            cbAudio.isChecked = false
            cbPhoto.isChecked = false
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("video", cbVideo.isChecked)
            editor.putBoolean("audio", cbAudio.isChecked)
            editor.putBoolean("photo", cbPhoto.isChecked)
            editor.apply()
        }
        cbAudio.setOnClickListener {
            //uncheck pada yang lain
            cbVideo.isChecked = false
            cbPhoto.isChecked = false
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("video", cbVideo.isChecked)
            editor.putBoolean("audio", cbAudio.isChecked)
            editor.putBoolean("photo", cbPhoto.isChecked)

            editor.apply()
        }
        cbPhoto.setOnClickListener {
            //uncheck pada yang lain
            cbVideo.isChecked = false
            cbAudio.isChecked = false
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("video", cbVideo.isChecked)
            editor.putBoolean("audio", cbAudio.isChecked)
            editor.putBoolean("photo", cbPhoto.isChecked)
            editor.apply()
        }
        spMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long){
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("mode", spMode.selectedItem.toString())
                editor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // TODO Auto-generated method stub
            }
        }

    }
    fun initSharedPref(){
        //ambil value shake dan screen dari sharedpreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        shakeValue = sharedPreferences.getInt("shakeValue", 0)
        //isScreenOn = sharedPreferences.getBoolean("screen", false)
        //set value ke sbShake dan swScreen
        sbShake.progress = shakeValue
        tvShake.text = "$shakeValue"
        //ambil video,photo, audio
        val video = sharedPreferences.getBoolean("video", false)
        val audio = sharedPreferences.getBoolean("audio", false)
        val photo = sharedPreferences.getBoolean("photo", false)
        val mode = sharedPreferences.getString("mode", "Depan")
        //set value ke spMode
        if (mode.equals("Depan")){
            spMode.setSelection(0)
        }else{
            spMode.setSelection(1)
        }
        //set value ke checkbox
        cbVideo.isChecked = video
        cbAudio.isChecked = audio
        cbPhoto.isChecked = photo
    }
    val screen = false
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}