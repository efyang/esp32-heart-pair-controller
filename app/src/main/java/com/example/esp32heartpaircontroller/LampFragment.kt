package com.example.esp32heartpaircontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.launch
import android.graphics.PorterDuff
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.coroutines.async


class LampFragment : Fragment() {

    private var lamp_enabled = false;
    lateinit var colorPicker: HSLColorPicker
    lateinit var imageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lamp, null)
        val parentActivity = activity!! as MainActivity
        colorPicker = view.findViewById(R.id.colorPicker2)
        imageView = view.findViewById(R.id.imageView2)
        val enableToggle: Button = view.findViewById(R.id.lampToggleButton)
        set_enabled(colorPicker, imageView)

        enableToggle.setOnClickListener {
            lamp_enabled = !lamp_enabled
            set_enabled(colorPicker, imageView)
        }

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            }
        })

        setColor(resources.getColor(R.color.colorPrimary))
        return view
    }

    fun set_enabled(colorPicker: HSLColorPicker, imageView: ImageView) {
        colorPicker.isEnabled = lamp_enabled
        colorPicker.isClickable = lamp_enabled
        colorPicker.touchable = lamp_enabled
        imageView.isEnabled = lamp_enabled
        val opacity: Float = if (lamp_enabled) 1f else 0.1f;
        colorPicker.alpha = opacity
        imageView.alpha = opacity
    }

    fun setColor(color: Int) {
        colorPicker.setColor(color)
        imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }
}
