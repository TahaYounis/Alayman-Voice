package taha.younis.el7loapp.util

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.ui.MainActivity

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigation)
    bottomNavigationView.visibility = View.GONE
}
fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigation)
    bottomNavigationView.visibility = View.VISIBLE
}