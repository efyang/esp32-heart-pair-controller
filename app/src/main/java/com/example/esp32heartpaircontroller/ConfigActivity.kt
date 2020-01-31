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
import android.widget.Toast


class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)


        val prefs = this.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        localNameInput.setText(prefs.getString("localUserName", getString(R.string.localUserName)))
        pairedNameInput.setText(prefs.getString("pairedUserName", getString(R.string.pairedUserName)))
        localComment.setText(prefs.getString("localComment", getString(R.string.localComment)))
        pairedComment.setText(prefs.getString("pairedComment", getString(R.string.pairedComment)))

        configOkButton.setOnClickListener{it ->
            // update the values
            prefs.edit().putString("localUserName", localNameInput.text.toString()).apply()
            prefs.edit().putString("pairedUserName", pairedNameInput.text.toString()).apply()
            prefs.edit().putString("localComment", localComment.text.toString()).apply()
            prefs.edit().putString("pairedComment", pairedComment.text.toString()).apply()

            Toast.makeText(applicationContext, "Names Set.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }
    }
}
