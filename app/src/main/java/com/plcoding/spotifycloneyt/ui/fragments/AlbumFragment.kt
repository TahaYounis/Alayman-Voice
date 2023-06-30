package com.plcoding.spotifycloneyt.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.QuranAdapter
import com.plcoding.spotifycloneyt.other.Status
import com.plcoding.spotifycloneyt.ui.interfaces.SendDataFromFragmentToActivity
import com.plcoding.spotifycloneyt.ui.viewmodels.PlayListViewModel
import com.plcoding.spotifycloneyt.ui.viewmodels.BaseArtistViewModelFactory
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_album.*
import taha.younis.el7loapp.util.showBottomNavigationView
import javax.inject.Inject

@AndroidEntryPoint
class AlbumFragment: Fragment(R.layout.fragment_album) {

    private val args by navArgs<AlbumFragmentArgs>()

    private var type: String? = null

    @Inject
    lateinit var quranAdapter: QuranAdapter

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var firestore: FirebaseFirestore

    private val playListViewModel by viewModels<PlayListViewModel> {
        BaseArtistViewModelFactory(firestore, type!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = args.type

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setupRecyclerView()

        subscribeToObservers()

        quranAdapter.setItemClickListener {
            val sendDataToActivity: SendDataFromFragmentToActivity =
                activity as SendDataFromFragmentToActivity
            sendDataToActivity.transferredDataFromFragment(quranAdapter.quran, it)
            mainViewModel.playOrToggleQuran(it) // we will not keep toggle false because we want just play the song and not toggle`
        }
    }

    private fun setupRecyclerView() = rvAlbumList.apply {
        adapter = quranAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        // for load artist's list
        playListViewModel.albumList.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    albumProgressBar.isVisible = false
                    result.data?.let {
                        quranAdapter.quran = it
                    }
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), result.message.toString(), Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> albumProgressBar.isVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}