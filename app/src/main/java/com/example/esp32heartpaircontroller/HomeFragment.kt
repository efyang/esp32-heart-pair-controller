package com.example.esp32heartpaircontroller

import android.content.Context
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    lateinit var localImageViews: Array<ImageView>
    lateinit var pairedImageViews: Array<ImageView>
    lateinit var localTextViews: Array<TextView>
    lateinit var pairedTextViews: Array<TextView>
    val offAlphas: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f)
    val onAlphas: FloatArray = floatArrayOf(0.5f, 1f, 1f, 1f, 1f)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        System.out.println("inflate")
        val parentActivity = activity!! as MainActivity
        val view = inflater.inflate(R.layout.fragment_home, null)

        val prefs = this.activity!!.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        val localNameDisplayView = view.findViewById(R.id.localNameDisplay) as TextView
        localNameDisplayView.text = prefs.getString("localUserName", getString(R.string.localUserName))
        val pairedNameDisplayView = view.findViewById(R.id.pairedNameDisplay) as TextView
        pairedNameDisplayView.text = prefs.getString("pairedUserName", getString(R.string.pairedUserName))

        val localLoveColorImageView: ImageView = view.findViewById(R.id.localLoveColorImageView)
        val localHappyColorImageView: ImageView = view.findViewById(R.id.localHappyColorImageView)
        val localSadColorImageView: ImageView = view.findViewById(R.id.localSadColorImageView)
        val localFearColorImageView: ImageView = view.findViewById(R.id.localFearColorImageView)
        val localAngerColorImageView: ImageView = view.findViewById(R.id.localAngerColorImageView)
        val localPortraitImageView: ImageView = view.findViewById(R.id.localPortrait)

        val localHappyColorTextView: TextView = view.findViewById(R.id.localHappyText)
        val localSadColorTextView: TextView = view.findViewById(R.id.localSadText)
        val localFearColorTextView: TextView = view.findViewById(R.id.localFearText)
        val localAngerColorTextView: TextView = view.findViewById(R.id.localAngerText)

        val pairedLoveColorImageView: ImageView = view.findViewById(R.id.pairedLoveColorImageView)
        val pairedHappyColorImageView: ImageView = view.findViewById(R.id.pairedHappyColorImageView)
        val pairedSadColorImageView: ImageView = view.findViewById(R.id.pairedSadColorImageView)
        val pairedFearColorImageView: ImageView = view.findViewById(R.id.pairedFearColorImageView)
        val pairedAngerColorImageView: ImageView = view.findViewById(R.id.pairedAngerColorImageView)
        val pairedPortraitImageView: ImageView = view.findViewById(R.id.pairedPortrait)

        val pairedHappyColorTextView: TextView = view.findViewById(R.id.pairedHappyText)
        val pairedSadColorTextView: TextView = view.findViewById(R.id.pairedSadText)
        val pairedFearColorTextView: TextView = view.findViewById(R.id.pairedFearText)
        val pairedAngerColorTextView: TextView = view.findViewById(R.id.pairedAngerText)

        localPortraitImageView.setImageURI(
            Uri.parse(prefs.getString("local_image_uri",
            "android.resource://com.example.esp32heartpaircontroller/"+R.drawable.ic_person_black_24dp)))
        pairedPortraitImageView.setImageURI(
            Uri.parse(prefs.getString("paired_image_uri",
                "android.resource://com.example.esp32heartpaircontroller/"+R.drawable.ic_person_black_24dp)))

        localPortraitImageView.setOnLongClickListener {
            ImagePicker.with(this)
                .crop(1f, 1f)               //Crop Square image(Optional)
                .compress(1024)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
                .start(MainActivity.LOCAL_IMAGE_REQ_CODE)
            true
        }

        pairedPortraitImageView.setOnLongClickListener {
            ImagePicker.with(this)
                .crop(1f, 1f)               //Crop Square image(Optional)
                .compress(1024)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
                .start(MainActivity.PAIRED_IMAGE_REQ_CODE)
            true
        }

        val opmode_spinner = view.findViewById(R.id.opmode_spinner) as Spinner
        opmode_spinner.setSelection(prefs.getInt("opmode", 0))
        println(opmode_spinner)

        opmode_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefs.edit().putInt("opmode", position).apply()
                val device_mac = prefs.getString("device_mac_address", resources.getString(R.string.device_mac_address))
                println("write pos " + position)

                val bleManager = BleManager.getInstance()
                if (bleManager.isConnected(device_mac)) {
                    val device: BleDevice = bleManager.allConnectedDevice.filter { d -> d!!.mac == device_mac }[0]
                    bleManager.write(device,
                        "d60df0e4-8a6f-4982-bf47-dab7e3b5d119",
                        "ae2c2e59-fb28-4737-9144-7dc72d69ccf4",
                        byteArrayOf(position.toByte()),
                        object: BleWriteCallback() {
                            override fun onWriteFailure(exception: BleException?) {
                            }

                            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                            }
                        })
                }
            }
        }

        localImageViews = arrayOf(localLoveColorImageView, localHappyColorImageView, localSadColorImageView, localFearColorImageView, localAngerColorImageView)
        localTextViews = arrayOf(localHappyColorTextView, localSadColorTextView, localFearColorTextView, localAngerColorTextView)
        pairedImageViews = arrayOf(pairedLoveColorImageView, pairedHappyColorImageView, pairedSadColorImageView, pairedFearColorImageView, pairedAngerColorImageView)
        pairedTextViews = arrayOf(pairedHappyColorTextView, pairedSadColorTextView, pairedFearColorTextView, pairedAngerColorTextView)

        reloadColors()

        return view
    }

    fun reloadPortraits() {
        val prefs = this.activity!!.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        val localPortraitImageView: ImageView = view!!.findViewById(R.id.localPortrait)
        val pairedPortraitImageView: ImageView = view!!.findViewById(R.id.pairedPortrait)

        localPortraitImageView.setImageURI(
            Uri.parse(prefs.getString("local_image_uri",
                "android.resource://com.example.esp32heartpaircontroller/"+R.drawable.ic_person_black_24dp)))
        pairedPortraitImageView.setImageURI(
            Uri.parse(prefs.getString("paired_image_uri",
                "android.resource://com.example.esp32heartpaircontroller/"+R.drawable.ic_person_black_24dp)))

    }

    fun setColor(v: ImageView, color: Int) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)
        hsl[1] = 0.75.toFloat()
        hsl[2] = 0.5.toFloat()
        v.background.setColorFilter(ColorUtils.HSLToColor(hsl), PorterDuff.Mode.MULTIPLY)
    }

    fun reloadColors() {
        val parentActivity = activity!! as MainActivity
        for ((v, i) in localImageViews zip (0..5)) {
            setColor(v, parentActivity.moodColors[i])
            v.alpha = offAlphas[i]
            if (i > 0) {
                localTextViews[i - 1].alpha = 1f
            }
        }

        for ((v, i) in pairedImageViews zip (0..5)) {
            setColor(v, parentActivity.moodColors[i])
            v.alpha = onAlphas[i]
            if (i > 0) {
                localTextViews[i - 1].alpha = 0f
            }
        }
    }
}
