package com.nelayanku.safely

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nelayanku.safely.service.ShakeDetectorService
import com.nelayanku.safely.service.WakelockService
import com.nelayanku.safely.ui.ContactFragment
import com.nelayanku.safely.ui.FolderFragment
import com.nelayanku.safely.ui.HomeFragment
import com.nelayanku.safely.ui.SettingFragment

class MainActivity : AppCompatActivity() {
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    var screen = false
    var shake = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        initFragment()
        initSharedpref()
    }
    fun initSharedpref(){
        //get sharedpref
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        //get value
        screen = sharedPref.getBoolean("screen", false)
        shake = sharedPref.getBoolean("shake", false)
    }
    fun initFragment(){
        val homeFragment = HomeFragment()
        //dapatkan intent dari activity sebelumnya
        val intent = intent
        //dapatkan data dari intent
        val fragment = intent.getStringExtra("fragment")

        if (fragment != null) {
            if (fragment == "folder") {
                val orderFragment = FolderFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.nav_folder
            }else if (fragment == "home") {
                val orderFragment = HomeFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.nav_home
            }else if (fragment == "setting") {
                val orderFragment = SettingFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.nav_setting
            }else if (fragment == "contact") {
                val orderFragment = ContactFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.nav_contact
            }
            else{
                replaceFragment(homeFragment)
            }
        }else{
            replaceFragment(homeFragment)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val homeFragment = HomeFragment()
                    replaceFragment(homeFragment)
                    true
                }
                R.id.nav_folder -> {
                    val orderFragment = FolderFragment()
                    replaceFragment(orderFragment)
                    true
                }
                R.id.nav_setting -> {
                    val walletFragment = SettingFragment()
                    replaceFragment(walletFragment)
                    true
                }
                R.id.nav_contact -> {
                    val profileFragment = ContactFragment()
                    replaceFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}