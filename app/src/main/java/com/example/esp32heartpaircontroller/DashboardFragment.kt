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
import android.widget.ImageView
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.coroutines.async


class DashboardFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, null)

        val colorPicker: HSLColorPicker = view.findViewById(R.id.colorPicker)
        val imageView: ImageView = view.findViewById(R.id.imageView)

        imageView.background.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        colorPicker.setColor(resources.getColor(R.color.colorPrimary))


        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            }
        })

        return view
    }
}
