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
import android.app.WallpaperColors
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.switch_item.*
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences
    lateinit var defaultMoodColors: IntArray
    lateinit var moodColors: IntArray
    lateinit var moodNames: Array<String>
    var lampColor: Int = 0
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var fragment: Fragment? = null;
        when (item.itemId) {
            R.id.navigation_home -> {
                fragment = HomeFragment()
            }
            R.id.navigation_dashboard -> {
                fragment = DashboardFragment()
            }
            R.id.navigation_device_network -> {
                fragment = NetworkSettingsFragment()
            }
            R.id.navigation_lamp -> {
                fragment = LampFragment()
            }
        }
        return@OnNavigationItemSelectedListener loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = this.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)

        defaultMoodColors = resources.getIntArray(R.array.defaultMoodColors)
        moodColors = defaultMoodColors.copyOf()
        moodNames = resources.getStringArray(R.array.moodNames)
        lampColor = resources.getColor(R.color.defaultLampColor)
        setBluetooth(true)
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
        val power_switch: Switch = menu?.findItem(R.id.power_switch)!!.actionView.findViewById(R.id.layout_switch)
        power_switch.setOnClickListener {
            System.out.println("power switch")
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        System.out.println(item)
        when (item?.itemId) {
            R.id.name_config -> {
                val intent = Intent(this, ConfigActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.ble_settings -> {
                val intent = Intent(this, BLEConfigActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.color_reset -> {
                moodColors = defaultMoodColors.copyOf()
                lampColor = resources.getColor(R.color.defaultLampColor)
                Toast.makeText(applicationContext, "Reset All Colors.", Toast.LENGTH_SHORT).show()
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFragment is LampFragment) {
                    val f = (currentFragment as LampFragment)
                    f.setColor(lampColor)
                } else if (currentFragment is DashboardFragment) {
                    val f = (currentFragment as DashboardFragment)
                    f.setColor(moodColors[f.currentTab])
                } else if (currentFragment is HomeFragment) {
                    val f = (currentFragment as HomeFragment)
                    f.reloadColors()
                }
            }
            R.id.device_connect -> {
                val device_mac = prefs.getString("device_mac_address", resources.getString(R.string.device_mac_address))
                BleManager.getInstance().connect(device_mac, object:BleGattCallback() {
                    override fun onStartConnect() {
                        Toast.makeText(applicationContext, "Connecting to Device...", Toast.LENGTH_SHORT).show()
                    }

                    override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                        Toast.makeText(applicationContext, "Failed to Connect to Device", Toast.LENGTH_SHORT).show()
                    }

                    override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                        Toast.makeText(applicationContext, "Connected to Device", Toast.LENGTH_SHORT).show()
                        for (service in gatt.services) {
                            for (characteristic in service.characteristics) {
                                println("Found characteristic " + characteristic.uuid + " from service " + service.uuid)
                            }
                        }
                    }

                    override fun onDisConnected(
                        isActiveDisConnected: Boolean,
                        device: BleDevice?,
                        gatt: BluetoothGatt?,
                        status: Int
                    ) {
                        Toast.makeText(applicationContext, "Disconnected From Device", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            R.id.save_config -> {
                Toast.makeText(applicationContext, "Configuration Saved to Device", Toast.LENGTH_SHORT).show()
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
