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
import android.widget.Toast
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.coroutines.async


class DashboardFragment : Fragment() {

    var currentTab = 0
    lateinit var colorPicker: HSLColorPicker
    lateinit var imageView: ImageView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, null)
        val parentActivity = activity!! as MainActivity

        colorPicker = view.findViewById(R.id.colorPicker)
        imageView = view.findViewById(R.id.imageView)
        val moodName: TextView = view.findViewById(R.id.moodName)
        val colorResetButton: Button = view.findViewById(R.id.colorResetButton)

        imageView.background.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        colorPicker.setColor(resources.getColor(R.color.colorPrimary))

        val tabs: TabLayout = view.findViewById(R.id.tabLayout)
        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                parentActivity.moodColors[currentTab] = color
                tabs.setSelectedTabIndicatorColor(color)
            }
        })

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position!!
                val color = parentActivity.moodColors[currentTab]
                setColor(color)
                tabs.setSelectedTabIndicatorColor(color)
                moodName.text = parentActivity.moodNames[currentTab] + " Color"
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }
        })

        colorResetButton.setOnClickListener {
            parentActivity.moodColors[currentTab] = parentActivity.defaultMoodColors[currentTab]
            val color = parentActivity.moodColors[currentTab]
            setColor(color)
            tabs.setSelectedTabIndicatorColor(color)
            Toast.makeText(context, "Reset " + parentActivity.moodNames[currentTab] + " Color", Toast.LENGTH_SHORT).show()
        }

        tabs.getTabAt(1)?.select()
        tabs.getTabAt(0)?.select()

        return view
    }

    fun setColor(color: Int) {
        colorPicker.setColor(color)
        imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }
}
