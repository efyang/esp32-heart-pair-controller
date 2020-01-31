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
import android.app.Activity
import android.app.WallpaperColors
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.switch_item.*
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var prefs: SharedPreferences
    lateinit var defaultMoodColors: IntArray
    lateinit var moodColors: IntArray
    lateinit var moodNames: Array<String>
    var localMoodStatus: Array<Boolean> = arrayOf(false, false, false, false, false)
    var pairedMoodStatus: Array<Boolean> = arrayOf(false, false, false, false, false)
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
        moodColors[0] = prefs.getInt("love_color", defaultMoodColors[0])
        moodColors[1] = prefs.getInt("happy_color", defaultMoodColors[1])
        moodColors[2] = prefs.getInt("sad_color", defaultMoodColors[2])
        moodColors[3] = prefs.getInt("fear_color", defaultMoodColors[3])
        moodColors[4] = prefs.getInt("anger_color", defaultMoodColors[4])
        moodNames = resources.getStringArray(R.array.moodNames)
        lampColor = prefs.getInt("lamp_color", resources.getColor(R.color.defaultLampColor))
        setBluetooth(true)
        checkPermissions()
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        ble_connect()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        loadFragment(HomeFragment())
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            prefs = this.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
            val fileUri = data?.data
            when (requestCode and 0x0000ffff) {
                LOCAL_IMAGE_REQ_CODE -> {
                    prefs.edit().putString("local_image_uri", fileUri.toString()).apply()
                }
                PAIRED_IMAGE_REQ_CODE -> {
                    prefs.edit().putString("paired_image_uri", fileUri.toString()).apply()
                }
            }
            val homeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as HomeFragment?
            if (homeFragment != null) {
                homeFragment.reloadPortraits()
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
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

    fun ble_connect() {
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
                // update stuff here on connection


                val bleManager = BleManager.getInstance()
                if (bleManager.isConnected(device_mac)) {
                    val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
                    // read opmode
                    bleManager.read(device,
                        "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                        "ae2c2e59-fb28-4737-9144-7dc72d69ccf4",
                        object: BleReadCallback() {
                            override fun onReadSuccess(data: ByteArray?) {
                                prefs.edit().putInt("opmode", data!![0].toInt()).apply()
                            }

                            override fun onReadFailure(exception: BleException?) {
                            }
                        })

                    // read brightness
                    bleManager.read(device,
                        "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                        "69ae6147-39d8-4d0e-8a5a-12e221041015",
                        object: BleReadCallback() {
                            override fun onReadSuccess(data: ByteArray?) {
                                prefs.edit().putInt("master_brightness", data!![0].toInt()).apply()
                                val fragmentContainer = supportFragmentManager.findFragmentById(R.id.fragment_container)
                                if (fragmentContainer != null && fragmentContainer is HomeFragment) {
                                    fragmentContainer.reloadBrightness()
                                }
                            }

                            override fun onReadFailure(exception: BleException?) {
                            }
                        })

                    // read mood bitstring
                    bleManager.read(device,
                        "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                        "cc6d6901-70d8-43e0-af2b-d5bd0eacf32a",
                        object: BleReadCallback() {
                            override fun onReadSuccess(data: ByteArray?) {
                                println("read bitstring " + Arrays.toString(data))
                                readMoodBitstring(data)
                            }

                            override fun onReadFailure(exception: BleException?) {
                            }
                        })

                    bleManager.notify(device,
                        "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                        "cc6d6901-70d8-43e0-af2b-d5bd0eacf32a",
                        object: BleNotifyCallback() {
                            override fun onCharacteristicChanged(data: ByteArray?) {
                                readMoodBitstring(data)
                            }

                            override fun onNotifyFailure(exception: BleException?) {

                            }

                            override fun onNotifySuccess() {

                            }
                        })
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

    fun readMoodBitstring(data: ByteArray?) {
        // bits 0-4 are local, 5-9 are remote
        val mood_bitstring = ((data!![1].toInt() and 0xFF) shl 8) or (data!![0].toInt() and 0xFF)

        val local_love = ((mood_bitstring shr 9) and 1) == 1
        val local_happy = ((mood_bitstring shr 8) and 1) == 1
        val local_sad = ((mood_bitstring shr 7) and 1) == 1
        val local_fear = ((mood_bitstring shr 6) and 1) == 1
        val local_anger = ((mood_bitstring shr 5) and 1) == 1
        val paired_love = ((mood_bitstring shr 4) and 1) == 1
        val paired_happy = ((mood_bitstring shr 3) and 1) == 1
        val paired_sad = ((mood_bitstring shr 2) and 1) == 1
        val paired_fear = ((mood_bitstring shr 1) and 1) == 1
        val paired_anger = ((mood_bitstring shr 0) and 1) == 1

        pairedMoodStatus = arrayOf(paired_love, paired_happy, paired_sad, paired_fear, paired_anger)
        localMoodStatus = arrayOf(local_love, local_happy, local_sad, local_fear, local_anger)

        val fragmentContainer = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragmentContainer != null && fragmentContainer is HomeFragment) {
            fragmentContainer.reloadColors()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        System.out.println(item)
        when (item?.itemId) {
            R.id.user_config -> {
                val intent = Intent(this, ConfigActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.ble_settings -> {
                val intent = Intent(this, BLEConfigActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.device_connect -> {
                ble_connect()
            }
            R.id.network_settings -> {
                val intent = Intent(this, NetworkConfigActivity::class.java).apply {}
                startActivity(intent)
            }
            R.id.save_config -> {
                val device_mac = prefs.getString("device_mac_address", resources.getString(R.string.device_mac_address))
                val bleManager = BleManager.getInstance()
                if (bleManager.isConnected(device_mac)) {

                    val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
                    bleManager.write(device,
                        "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                        "eb02ef6a-07cd-4bdb-babe-3375579dc9af",
                        byteArrayOf(1),
                        object: BleWriteCallback() {
                            override fun onWriteFailure(exception: BleException?) {
                            }

                            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                                Toast.makeText(applicationContext, "Configuration Saved to Device", Toast.LENGTH_SHORT).show()
                            }
                        })
                }

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

        const val LOCAL_IMAGE_REQ_CODE = 101
        const val PAIRED_IMAGE_REQ_CODE = 102
    }
}
