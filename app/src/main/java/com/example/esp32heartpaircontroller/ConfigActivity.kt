package com.example.esp32heartpaircontroller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_config.*
import android.R.id.edit
import android.R.id.home
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences



class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)


        val prefs = this.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        localNameInput.setText(prefs.getString("localUserName", getString(R.string.localUserName)))
        pairedNameInput.setText(prefs.getString("pairedUserName", getString(R.string.pairedUserName)))

        configOkButton.setOnClickListener{it ->
            // update the values
            prefs.edit().putString("localUserName", localNameInput.text.toString()).apply()
            prefs.edit().putString("pairedUserName", pairedNameInput.text.toString()).apply()

            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }
    }
}
