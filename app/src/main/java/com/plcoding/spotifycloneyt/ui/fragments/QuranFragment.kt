package com.plcoding.spotifycloneyt.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.exoplayer.isPlaying
import com.plcoding.spotifycloneyt.exoplayer.toQuran
import com.plcoding.spotifycloneyt.other.Status.SUCCESS
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import com.plcoding.spotifycloneyt.ui.viewmodels.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_quran.*
import taha.younis.el7loapp.util.hideBottomVBar
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class QuranFragment : Fragment(R.layout.fragment_quran) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val quranViewModel: QuranViewModel by viewModels()// we will bind this viewModel with this fragment

    private var currPlayingQuran: QuranModel? = null

    private var playBackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    private var heartState: Int =  0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val quran = args.quran

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        subscribeTOObservers()

        // every time seekbar value changes this listener will be called
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // when user dragging what we want to do as long as dragging, we want update the current time and set that to our textView
                if (fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { // when user drag the seekbar
                shouldUpdateSeekBar =
                    false  // because doing drag we don't need other sources update our seekbar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { // when user stop drag seekbar and left his hand then we want seek to that position in our song
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })

        // play or pause song button
        ivPlayPauseDetail.setOnClickListener {
            currPlayingQuran?.let {
                mainViewModel.playOrToggleQuran(it, true)
            }
        }
        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousQuranChapter()
        }
        ivSkip.setOnClickListener {
            mainViewModel.skipToNextQuranChapter()
        }

        btnFavorite.setOnClickListener {
            if (heartState == 0){
                heartState = 1
                quranViewModel.saveQuranToFavorite(currPlayingQuran!!)
                btnFavorite.setImageResource(R.drawable.heart_white)
            }else{
                heartState = 0
                quranViewModel.deleteQuran(currPlayingQuran!!)
                btnFavorite.setImageResource(R.drawable.heart_gray)
            }
        }
    }

    // fun for update the title of the fragment and song image once new song is loaded
    private fun updateTitleAndQuranImage(quranModel: QuranModel) {
        tvQuranName.text = quranModel.title
        tvArtist.text = quranModel.subtitle
        glide.load(quranModel.imageUrl).into(ivQuranImage)
    }

    //setup observe for our media items and our currently playing song
    private fun subscribeTOObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        /* if we retrieve the current media items we will load the first song initially into this fragment, if we directly launch
                         the app then directly click on viewpager without playing anything the we still want that the first song is loaded into quran fragment so this why we used this observer */
                        result.data?.let { quranModel ->

                            if (currPlayingQuran == null && quranModel.isNotEmpty()) {
                                currPlayingQuran = quranModel[0]
                                updateTitleAndQuranImage(quranModel[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        quranViewModel.getSavedQuran().observe(viewLifecycleOwner){
            if (it.contains(currPlayingQuran)){
                btnFavorite.setImageResource(R.drawable.heart_white)
                heartState = 1
            }
        }

        // when current song changed we want notify here in this fragment to update information
        mainViewModel.curPlayingQuran.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            currPlayingQuran = it.toQuran()
            updateTitleAndQuranImage(currPlayingQuran!!)
        }

        // if we pause song from notification we want update image view ivPlayPauseDetail
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playBackState = it
            ivPlayPauseDetail.setImageResource(
                if (playBackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            // update seekbar when playBack changes, if it null we put 0 if not update it, seekbar.progress set the current progress to the given value
            seekBar.progress = it?.position?.toInt() ?: 0
        }
        quranViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            /* update seekbar with this progress (it), first we need to make sure that we want update
            our seekbar because say the user wants to seekbar himself and he drags the seekbar little bit
            then during the drag we don't want update the seekbar because seekbar value will jump away
            so we need public boolean to if we want to update that seekbar */
            if (shouldUpdateSeekBar) {
                seekBar.progress = it!!.toInt()
                // we want update our position texView
                setCurPlayerTimeToTextView(it)
            }
        }
        // for the textView that show how long our song is
        quranViewModel.curQuranDuration.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
            val datFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = datFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(milliSeconds: Long) {
        val datFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = datFormat.format(milliSeconds)
    }
}