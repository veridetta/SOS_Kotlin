package com.nelayanku.safely.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
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
class SettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    //btnTimer (Linearlayout), sbShake, swScreen
    lateinit var btnTimer : LinearLayout
    lateinit var sbShake : SeekBar
    lateinit var swScreen : SwitchCompat

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

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // TODO Auto-generated method stub
            }
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

    }
    fun initSharedPref(){
        //ambil value shake dan screen dari sharedpreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        shakeValue = sharedPreferences.getInt("shakeValue", 0)
        //isScreenOn = sharedPreferences.getBoolean("screen", false)
        //set value ke sbShake dan swScreen
        sbShake.progress = shakeValue
        //swScreen.isChecked = isScreenOn
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