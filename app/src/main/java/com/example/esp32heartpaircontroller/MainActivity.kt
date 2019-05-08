package com.example.esp32heartpaircontroller

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_CONTACTS
import android.os.Bundle
import android.provider.ContactsContract
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
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
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.clj.fastble.BleManager
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

        setBluetooth(true);
        checkPermissions()
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        loadFragment(HomeFragment())
    }

    fun setBluetooth(enable: Boolean): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = bluetoothAdapter.isEnabled
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable()
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable()
        }
        // No need to change bluetooth state
        return true
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


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> if (grantResults.size > 0) {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted(permissions[i])
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show()
            return
        }

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionDeniedList = ArrayList<String>()
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray<String>()
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION)
        }
    }

    private fun onPermissionGranted(permission: String) {
        when (permission) {
            android.Manifest.permission.ACCESS_FINE_LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.notifyTitle)
                        .setMessage(R.string.gpsNotifyMsg)
                        .setNegativeButton(R.string.cancel
                        ) { dialog, which -> finish() }
                        .setPositiveButton(R.string.setting
                        ) { dialog, which ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                        }

                        .setCancelable(false)
                        .show()
            } else {
            }
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                ?: return false
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    companion object {

        private val TAG = MainActivity::class.java!!.getSimpleName()
        private val REQUEST_CODE_OPEN_GPS = 1
        private val REQUEST_CODE_PERMISSION_LOCATION = 2
    }
}
