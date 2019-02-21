package com.example.esp32heartpaircontroller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_config.*

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        configOkButton.setOnClickListener{it ->
            val intent = Intent(this, MainActivity::class.java).apply {}
            startActivity(intent)
        }
    }
}
