package com.example.esp32heartpaircontroller

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        System.out.println("inflate")
        val view = inflater.inflate(R.layout.fragment_home, null)

        val prefs = this.activity!!.getSharedPreferences("com.example.esp32heartpaircontroller", Context.MODE_PRIVATE)
        val localNameDisplayView = view.findViewById(R.id.localNameDisplay) as TextView
        System.out.println(prefs == null)
        System.out.println(prefs.getString("localUserName", "nul"))
        localNameDisplayView.text = prefs.getString("localUserName", getString(R.string.localUserName))
        val pairedNameDisplayView = view.findViewById(R.id.pairedNameDisplay) as TextView
        pairedNameDisplayView.text = prefs.getString("pairedUserName", getString(R.string.pairedUserName))
        return view
    }
}
