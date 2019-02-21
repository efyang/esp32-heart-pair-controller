package com.example.esp32heartpaircontroller

import android.Manifest.permission.READ_CONTACTS
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.app.ActivityOptions
import android.content.Intent
import android.database.Cursor
import android.view.Menu
import android.view.MenuItem
import android.R.id.edit
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_home.*


class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var fragment: Fragment? = null;
        when (item.itemId) {
            R.id.navigation_home -> {
                fragment = HomeFragment();
            }
            R.id.navigation_dashboard -> {
                fragment = DashboardFragment();
            }
            R.id.navigation_device_network -> {
                fragment = NetworkSettingsFragment();
            }
        }
        return@OnNavigationItemSelectedListener loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            return true;
        }
        return false;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        loadFragment(HomeFragment())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        System.out.println(item)
        when (item?.itemId) {
            R.id.config -> {
                val intent = Intent(this, ConfigActivity::class.java).apply {}
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
