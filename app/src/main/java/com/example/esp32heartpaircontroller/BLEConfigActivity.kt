package com.example.esp32heartpaircontroller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.R.id.edit
import android.R.id.home
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_ble_config.*


class BLEConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_config)


        val prefs = this.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        bleMacAddressInput.setText(prefs.getString("device_mac_address", resources.getString(R.string.device_mac_address)))

        bleConfigButton.setOnClickListener{
            prefs.edit().putString("device_mac_address", bleMacAddressInput.text.toString()).apply()
            // update the values
            Toast.makeText(applicationContext, "BLE Configured.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }

        bleConfigTextView.isEnabled = bleInputSwitch.isChecked
        bleMacAddressInput.isEnabled = bleInputSwitch.isChecked
        bleConfigButton.isEnabled = bleInputSwitch.isChecked

        bleInputSwitch.setOnClickListener{
            bleConfigTextView.isEnabled = bleInputSwitch.isChecked
            bleMacAddressInput.isEnabled = bleInputSwitch.isChecked
            bleConfigButton.isEnabled = bleInputSwitch.isChecked
        }
    }
}
