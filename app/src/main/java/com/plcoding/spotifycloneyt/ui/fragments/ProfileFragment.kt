package com.plcoding.spotifycloneyt.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.plcoding.spotifycloneyt.R
import taha.younis.el7loapp.util.showBottomNavigationView

class ProfileFragment: Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onHomeClick()

    }
    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
    private fun onHomeClick() {
        val btm = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        btm?.menu?.getItem(0)?.setOnMenuItemClickListener {
            activity?.onBackPressed()
            true
        }
    }
}