package com.example.esp32heartpaircontroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.android.synthetic.main.activity_network.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt


class NetworkConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network)

        val prefs = this.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)

        if (prefs.getBoolean("wifi_mode_normal", true)) {
            wifi_mode_spinner?.setSelection(0)
        } else {
            wifi_mode_spinner?.setSelection(1)
        }

        edit_ssid.setText(prefs.getString("wifi_ssid", ""))
        edit_user.setText(prefs.getString("wifi_user", ""))
        edit_pass.setText(prefs.getString("wifi_pass", ""))

        wifi_mode_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    prefs.edit().putBoolean("wifi_mode_normal", true).apply()
                    user_textview?.visibility = View.GONE
                    edit_user?.visibility = View.GONE
                    space1?.visibility = View.GONE
                    space2?.visibility = View.GONE
                } else if (position == 1) {
                    prefs.edit().putBoolean("wifi_mode_normal", false).apply()
                    user_textview?.visibility = View.VISIBLE
                    edit_user?.visibility = View.VISIBLE
                    space1?.visibility = View.VISIBLE
                    space2?.visibility = View.VISIBLE
                }
            }
        }

        network_cancel_button.setOnClickListener{
            Toast.makeText(applicationContext, "Network configuration cancelled.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }

        network_connect_button.setOnClickListener{
            Toast.makeText(applicationContext, "Network configured, trying to connect...", Toast.LENGTH_SHORT).show()

            prefs.edit().putString("wifi_ssid", edit_ssid.text.toString()).apply()
            prefs.edit().putString("wifi_user", edit_user.text.toString()).apply()
            prefs.edit().putString("wifi_pass", edit_pass.text.toString()).apply()


            val device_mac = prefs.getString("device_mac_address", resources.getString(R.string.device_mac_address))

            val bleManager = BleManager.getInstance()
            if (bleManager.isConnected(device_mac)) {
                val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
                println("send ssid")
                write_multiple_packets(bleManager,
                    device,
                    "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                    "1cea50e2-c88d-4bbb-9ae3-6b637cee1041",
                    edit_ssid.text.toString())

                println("send pass")
                write_multiple_packets(bleManager,
                    device,
                    "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                    "37f294c1-c9c7-48c8-b944-4b453725f8ea",
                    edit_pass.text.toString())

                println("send user")
                write_multiple_packets(bleManager,
                    device,
                    "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                    "8dead309-383e-4725-ac09-a6cbc0e5bef7",
                    edit_user.text.toString())

                bleManager.write(device,
                    "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                    "991cde05-b892-497f-ad4a-0768b59cfbba",
                    byteArrayOf(if (prefs.getBoolean("wifi_mode_normal", true)) 0 else 1),
                    object: BleWriteCallback() {
                        override fun onWriteFailure(exception: BleException?) {
                        }

                        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        }
                    })
            }

            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }
    }

    // chunk a message into multiple packets to prevent overflows
    fun write_multiple_packets(bleManager: BleManager, device: BleDevice, uuid_service: String, uuid_write: String, s: String) {
        var i = 0
        while (i * 20 < s.length) {
            println(min(s.length, 20*(i+1)))
            bleManager.write(device,
                uuid_service,
                uuid_write,
                s.substring(20*i, min(s.length, 20*(i+1))).toByteArray(),
                object: BleWriteCallback() {
                    override fun onWriteFailure(exception: BleException?) {
                        println("write pass fail")
                    }

                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        println("write pass success")
                    }
                })
            i++
            Thread.sleep(500)
        }
    }
}
