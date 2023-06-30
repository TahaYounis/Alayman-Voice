package taha.younis.el7loapp.util

import android.view.View
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.ui.MainActivity

fun Fragment.hideBottomVBar(){
    val bottomBar =
        (activity as MainActivity).findViewById<CardView>(R.id.cardCurQuran)
    bottomBar.visibility = View.GONE
}
fun Fragment.showBottomBottomBar(){
    val bottomBar =
        (activity as MainActivity).findViewById<CardView>(R.id.cardCurQuran)
    bottomBar.visibility = View.VISIBLE
}