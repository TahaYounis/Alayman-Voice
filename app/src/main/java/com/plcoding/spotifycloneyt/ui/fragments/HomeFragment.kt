package com.plcoding.spotifycloneyt.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.ArtistAdapter
import com.plcoding.spotifycloneyt.adapters.QuranAdapter
import com.plcoding.spotifycloneyt.other.Status
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import taha.younis.el7loapp.util.showBottomBottomBar
import taha.younis.el7loapp.util.showBottomNavigationView
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var quranAdapter: QuranAdapter

    @Inject
    lateinit var artistAdapter: ArtistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupArtistsRv()

        subscribeToObservers()

        quranAdapter.setItemClickListener {
            mainViewModel.playOrToggleQuran(it) // we will not keep toggle false because we want just play the song and not toggle`
        }
        artistAdapter.setItemClickListener {
            val b = Bundle().apply { putParcelable("list",it) }
            findNavController().navigate(R.id.action_homeFragments_to_artistFragment,b)
        }
        ed_searchHome.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragments_to_searchFragment)
        }

        quranAlbum.setOnClickListener {
            val b = Bundle()
            b.putString("type", "قران")
            findNavController().navigate(R.id.action_homeFragments_to_albumFragment,b)
        }
        _7deesAlbum.setOnClickListener {
            val b = Bundle()
            b.putString("type", "احاديث")
            findNavController().navigate(R.id.action_homeFragments_to_albumFragment,b)
        }
        azkarAlbum.setOnClickListener {
            val b = Bundle()
            b.putString("type", "اذكار")
            findNavController().navigate(R.id.action_homeFragments_to_albumFragment,b)
        }
        anashidAlbum.setOnClickListener {
            val b = Bundle()
            b.putString("type", "اناشيد")
            findNavController().navigate(R.id.action_homeFragments_to_albumFragment,b)
        }
        abtehalatAlbum.setOnClickListener {
            val b = Bundle()
            b.putString("type", "ابتهالات")
            findNavController().navigate(R.id.action_homeFragments_to_albumFragment,b)
        }

    }

    private fun setupArtistsRv() = rvArtist.apply {
        adapter = artistAdapter
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun subscribeToObservers() {
        // we want to subscribe to that media item, so we notify when media items are loaded
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    allQuranProgressBar.isVisible = false
                    // set the quran list of our recyclerView adapter to the data that attached to (it)
                    result.data?.let {
//                        quranAdapter.quran = it
                        /* this will trigger setter method in variable list of quran model that have getter and setter and setter have method
                        differ.submitList so it automatically calculate the differences and update our recyclerView in very efficient way */
                    }
                    val items = result.data?.distinctBy { Pair(it.subtitle,it.imageUrl) }
                    artistAdapter.quran = items!!
                }
                Status.ERROR -> Unit // we want do anything because this can never happened we never emit this error status here for these media items
                Status.LOADING -> allQuranProgressBar.isVisible = true // show progress bar
            }
        }
    }
    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}