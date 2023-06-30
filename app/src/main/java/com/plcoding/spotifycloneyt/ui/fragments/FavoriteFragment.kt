package com.plcoding.spotifycloneyt.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.QuranAdapter
import com.plcoding.spotifycloneyt.ui.interfaces.SendDataFromFragmentToActivity
import com.plcoding.spotifycloneyt.ui.viewmodels.FavoriteViewModel
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import com.plcoding.spotifycloneyt.ui.viewmodels.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.android.synthetic.main.fragment_favorite.*
import taha.younis.el7loapp.util.showBottomNavigationView
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteFragment:Fragment(R.layout.fragment_favorite) {

    private val quranViewModel: QuranViewModel by viewModels()// we will bind this viewModel with this fragment

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var quranAdapter: QuranAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupRecyclerView()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            // when we swap we will delete the item from adapter list
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val quran = quranAdapter.quran[position]
                quranViewModel.deleteQuran(quran)
                Snackbar.make(view,"تم الحذف",Snackbar.LENGTH_LONG).apply {
                    setAction("تراجع"){
                        quranViewModel.saveQuranToFavorite(quran)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(rvFavoriteList)
        }

        quranAdapter.setItemClickListener {
            val sendDataToActivity: SendDataFromFragmentToActivity =
                activity as SendDataFromFragmentToActivity
            sendDataToActivity.transferredDataFromFragment(listOf(it), it)
            mainViewModel.playOrToggleQuran(it) // we will not keep toggle false because we want just play the song and not toggle`
        }

        quranViewModel.getSavedQuran().observe(viewLifecycleOwner, Observer {
            quranAdapter.quran = it
        })

        onHomeClick()
    }

    private fun setupRecyclerView() {
        rvFavoriteList.apply {
            adapter = quranAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
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