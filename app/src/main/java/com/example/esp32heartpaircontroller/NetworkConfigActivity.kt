package com.example.esp32heartpaircontroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_network.*


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

            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }
    }
}
