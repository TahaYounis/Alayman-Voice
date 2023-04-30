package com.plcoding.spotifycloneyt.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.QuranAdapter
import com.plcoding.spotifycloneyt.other.Status
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Context
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var quranAdapter: QuranAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setupRecyclerView()
        subscribeToObservers()

        quranAdapter.setItemClickListener {
            mainViewModel.playOrToggleQuran(it) // we will not keep toggle false because we want just play the song and not toggle`
        }
    }

    private fun setupRecyclerView() = rvAllQuran.apply {
        adapter = quranAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
    private fun subscribeToObservers() {
        // we want to subscribe to that media item, so we notify when media items are loaded
        mainViewModel.mediaItem.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    allQuranProgressBar.isVisible = false
                    // set the quran list of our recyclerView adapter to the data that attached to (it)
                    result.data?.let {
                        quranAdapter.quran = it
                    /* this will trigger setter method in variable list of quran model that have getter and setter and setter have method
                    differ.submitList so it automatically calculate the differences and update our recyclerView in very efficient way */
                    }
                }
                Status.ERROR -> Unit // we want do anything because this can never happened we never emit this error status here for these media items
                Status.LOADING -> allQuranProgressBar.isVisible = true // show progress bar
            }
        }
    }
}