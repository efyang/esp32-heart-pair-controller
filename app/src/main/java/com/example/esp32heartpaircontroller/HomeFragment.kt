package com.example.esp32heartpaircontroller

import android.content.Context
import android.graphics.PorterDuff
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    lateinit var localImageViews: Array<ImageView>
    lateinit var pairedImageViews: Array<ImageView>
    val offAlphas: FloatArray = floatArrayOf(0f, 0.1f, 0.1f, 0.1f, 0.1f)
    val onAlphas: FloatArray = floatArrayOf(0.5f, 1f, 1f, 1f, 1f)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        System.out.println("inflate")
        val parentActivity = activity!! as MainActivity
        val view = inflater.inflate(R.layout.fragment_home, null)

        val prefs = this.activity!!.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        val localNameDisplayView = view.findViewById(R.id.localNameDisplay) as TextView
        System.out.println(prefs == null)
        System.out.println(prefs.getString("localUserName", "nul"))
        localNameDisplayView.text = prefs.getString("localUserName", getString(R.string.localUserName))
        val pairedNameDisplayView = view.findViewById(R.id.pairedNameDisplay) as TextView
        pairedNameDisplayView.text = prefs.getString("pairedUserName", getString(R.string.pairedUserName))

        val localLoveColorImageView: ImageView = view.findViewById(R.id.localLoveColorImageView)
        val localHappyColorImageView: ImageView = view.findViewById(R.id.localHappyColorImageView)
        val localSadColorImageView: ImageView = view.findViewById(R.id.localSadColorImageView)
        val localFearColorImageView: ImageView = view.findViewById(R.id.localFearColorImageView)
        val localAngerColorImageView: ImageView = view.findViewById(R.id.localAngerColorImageView)
        val pairedLoveColorImageView: ImageView = view.findViewById(R.id.pairedLoveColorImageView)
        val pairedHappyColorImageView: ImageView = view.findViewById(R.id.pairedHappyColorImageView)
        val pairedSadColorImageView: ImageView = view.findViewById(R.id.pairedSadColorImageView)
        val pairedFearColorImageView: ImageView = view.findViewById(R.id.pairedFearColorImageView)
        val pairedAngerColorImageView: ImageView = view.findViewById(R.id.pairedAngerColorImageView)

        localImageViews = arrayOf(localLoveColorImageView, localHappyColorImageView, localSadColorImageView, localFearColorImageView, localAngerColorImageView)
        pairedImageViews = arrayOf(pairedLoveColorImageView, pairedHappyColorImageView, pairedSadColorImageView, pairedFearColorImageView, pairedAngerColorImageView)

        reloadColors()

        return view
    }

    fun setColor(v: ImageView, color: Int) {
        v.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    fun reloadColors() {
        val parentActivity = activity!! as MainActivity
        for ((v, i) in localImageViews zip (0..5)) {
            setColor(v, parentActivity.moodColors[i])
            v.alpha = offAlphas[i]
        }

        for ((v, i) in pairedImageViews zip (0..5)) {
            setColor(v, parentActivity.moodColors[i])
            v.alpha = onAlphas[i]
        }
    }
}
