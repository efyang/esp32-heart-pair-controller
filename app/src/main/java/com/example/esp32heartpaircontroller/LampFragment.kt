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
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
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
        val prefs = this.activity!!.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        val device_mac = prefs.getString("device_mac_address", resources.getString(R.string.device_mac_address))
        val parentActivity = activity!! as MainActivity
        colorPicker = view.findViewById(R.id.colorPicker2)
        imageView = view.findViewById(R.id.imageView2)
        val enableToggle: Button = view.findViewById(R.id.lampToggleButton)
        //set_enabled(colorPicker, imageView)

        enableToggle.setOnClickListener {
            lamp_enabled = !lamp_enabled
            //set_enabled(colorPicker, imageView)

            val bleManager = BleManager.getInstance()
            if (bleManager.isConnected(device_mac)) {
                val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
                bleManager.write(device,
                    "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                    "ae2c2e59-fb28-4737-9144-7dc72d69ccf4",
                    byteArrayOf(if (lamp_enabled) 1 else 0),
                    object: BleWriteCallback() {
                        override fun onWriteFailure(exception: BleException?) {
                        }

                        override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        }
                    })
            }
        }

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                bleSetColor(color, device_mac)
            }
        })

        setColor(resources.getColor(R.color.colorPrimary))

        val bleManager = BleManager.getInstance()
        if (bleManager.isConnected(device_mac)) {
            val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
            bleManager.read(device,
                "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                "c367b354-c1cf-43d6-8c3f-24288fc231ce",
                object: BleReadCallback() {
                    override fun onReadSuccess(data: ByteArray?) {
                        val c = (0xFF shl 24) or
                                (((data!![0]).toInt() and 0xFF) shl 16) or
                                (((data!![1]).toInt() and 0xFF) shl 8) or
                                (((data!![2]).toInt() and 0xFF) shl 0)
                        setColor(c)
                    }

                    override fun onReadFailure(exception: BleException?) {
                    }
                })
        }
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
        imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    fun bleSetColor(color: Int, device_mac: String) {
        val bleManager = BleManager.getInstance()
        if (bleManager.isConnected(device_mac)) {
            val r = (color shr 16 and 0xFF).toByte()
            val g = (color shr 8 and 0xFF).toByte()
            val b = (color shr 0 and 0xFF).toByte()

            val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
            bleManager.write(device,
                "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                "c367b354-c1cf-43d6-8c3f-24288fc231ce",
                byteArrayOf(r,g,b),
                object: BleWriteCallback() {
                    override fun onWriteFailure(exception: BleException?) {
                    }

                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    }
                })
        }
    }
}
