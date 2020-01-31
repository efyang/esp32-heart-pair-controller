package com.example.esp32heartpaircontroller

import android.content.Context
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
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.coroutines.async
import java.util.*
import android.R.color
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.exception.BleException
import kotlin.collections.HashMap


class DashboardFragment : Fragment() {

    var currentTab = 0
    lateinit var colorPicker: HSLColorPicker
    lateinit var imageView: ImageView
    val TAB_UUID_MAPPINGS: Array<String> = arrayOf("b6b60a30-305f-4a7e-98a3-dde8d017459d",
        "beb5483e-36e1-4688-b7f5-ea07361b26a8",
        "f6845c39-075f-4b91-a505-2e16a69d3d57",
        "c7dc21a4-3114-4ab2-8254-ef3c91b97b32",
        "838fc38a-df30-42cb-9b55-2f3596dd0506")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, null)
        val prefs = this.activity!!.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        val device_mac = prefs.getString("device_mac_address", resources.getString(R.string.device_mac_address))
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
                when (currentTab) {
                    0 -> {prefs.edit().putInt("love_color", color).apply()}
                    1 -> {prefs.edit().putInt("happy_color", color).apply()}
                    2 -> {prefs.edit().putInt("sad_color", color).apply()}
                    3 -> {prefs.edit().putInt("fear_color", color).apply()}
                    4 -> {prefs.edit().putInt("anger_color", color).apply()}
                }
                bleSetColor(color, currentTab, device_mac)
            }
        })

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position!!

            val bleManager = BleManager.getInstance()
            if (bleManager.isConnected(device_mac)) {
            val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
            bleManager.read(device,
                    "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                    TAB_UUID_MAPPINGS[currentTab],
                    object: BleReadCallback() {
                        override fun onReadSuccess(data: ByteArray?) {
                            val c = (0xFF shl 24) or
                                    (((data!![0]).toInt() and 0xFF) shl 16) or
                                    (((data!![1]).toInt() and 0xFF) shl 8) or
                                    (((data!![2]).toInt() and 0xFF) shl 0)
                            parentActivity.moodColors[currentTab] = c
                            when (currentTab) {
                                0 -> {prefs.edit().putInt("love_color", c).apply()}
                                1 -> {prefs.edit().putInt("happy_color", c).apply()}
                                2 -> {prefs.edit().putInt("sad_color", c).apply()}
                                3 -> {prefs.edit().putInt("fear_color", c).apply()}
                                4 -> {prefs.edit().putInt("anger_color", c).apply()}
                            }
                            if (tabs.selectedTabPosition == currentTab) {
                                setColor(c)
                            }
                        }

                        override fun onReadFailure(exception: BleException?) {
                        }
                    })
            }

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
            bleSetColor(color, currentTab, device_mac)
            tabs.setSelectedTabIndicatorColor(color)
            Toast.makeText(context, "Reset " + parentActivity.moodNames[currentTab] + " Color", Toast.LENGTH_SHORT).show()
        }

        val color = parentActivity.moodColors[currentTab]
        setColor(color)
        tabs.setSelectedTabIndicatorColor(color)
        moodName.text = parentActivity.moodNames[currentTab] + " Color"

        return view
    }

    fun setColor(color: Int) {
        colorPicker.setColor(color)
        imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    fun bleSetColor(color: Int, tab: Int, device_mac: String) {
       val bleManager = BleManager.getInstance()
            if (bleManager.isConnected(device_mac)) {
                val r = (color shr 16 and 0xFF).toByte()
                val g = (color shr 8 and 0xFF).toByte()
                val b = (color shr 0 and 0xFF).toByte()

                val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
                bleManager.write(device,
                    "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                    TAB_UUID_MAPPINGS[tab],
                    byteArrayOf(r,g,b),
                    object:BleWriteCallback() {
                        override fun onWriteFailure(exception: BleException?) {
                        }

                        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        }
                    })
            }
    }
}
