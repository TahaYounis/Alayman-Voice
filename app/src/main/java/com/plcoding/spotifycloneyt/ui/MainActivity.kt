package com.plcoding.spotifycloneyt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.SwipeQuranAdapter
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.exoplayer.isPlaying
import com.plcoding.spotifycloneyt.exoplayer.toQuran
import com.plcoding.spotifycloneyt.other.Status.*
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

// if we inject something into android components like activity,fragment,service.. ,we need to annotate this component
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels() // this way bind viewModel to lifecycle of MainActivity

    @Inject
    lateinit var swipeQuranAdapter: SwipeQuranAdapter // recyclerView adapter for viewPager, so you can swipe through the songs

    @Inject
    lateinit var glide: RequestManager

    //global variable for the currently playing song
    private var curPlayingQuran: QuranModel? = null

    // variable for our currently playing song for our playback state for so whether player is on pause if it prepared all this stuff
    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToOurObservers()

        // assign injected adapter to our vp song
        viewpagerQuran.adapter = swipeQuranAdapter
        // play new song when swap to
        viewpagerQuran.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // this fun called every time when swipe or change the current item of our viewPager
                // check if song is currently playing then we will play the song at the position parameter that we swipe to
                if (playbackState?.isPlaying == true){
                    mainViewModel.playOrToggleQuran(swipeQuranAdapter.quran[position])
                }else{
                    // when song is pause we want to update current song without toggle it
                    curPlayingQuran = swipeQuranAdapter.quran[position]
                }
            }
        })

        ivPlayPause.setOnClickListener{
            // toggle the state of our currently playing song
            curPlayingQuran?.let {
                mainViewModel.playOrToggleQuran(it, true)
            }
        }

        swipeQuranAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(
                R.id.globalActionToQuranFragment
            )
        }

        navHostFragment.findNavController().addOnDestinationChangedListener{_,destination,_ ->
            when(destination.id){
                R.id.quranFragment -> hideBottomBar()
                R.id.homeFragments -> showBottomBar()
                else -> showBottomBar()
            }
        }
    }
    // fun to hide bottom bar (image, viewpager, and playPauseImage)
    private fun hideBottomBar(){
        ivCurQuranImage.isVisible = false
        ivPlayPause.isVisible = false
        viewpagerQuran.isVisible = false
    }
    private fun showBottomBar(){
        ivCurQuranImage.isVisible = true
        ivPlayPause.isVisible = true
        viewpagerQuran.isVisible = true
    }
    //write fun to switch this viewpager to the current song basically, so when our viewpager automatically swipes to the corresponding song
    private fun switchViewPagerToCurrentQuran(quranModel: QuranModel) { // take the chapter of quran that we want switch to
        // first we want to check at which index in our swipe list this quranModel actually is
        val newItemIndex = swipeQuranAdapter.quran.indexOf(quranModel)
        if (newItemIndex != -1) { // this check because this fun will return -1 if this song doesn't exist in that list because if it -1 get out of bound error
            viewpagerQuran.currentItem = newItemIndex //currentItem index of current item displayed in viewPager
            curPlayingQuran = quranModel
        }
    }

    // fun to subscribe to our observers
    private fun subscribeToOurObservers() {
        // observe on media items so we can fill our viewPager with these items, and also display right item when launch our app
        mainViewModel.mediaItem.observe(this) {
            it?.let {
                when (it.status) {
                    SUCCESS -> {
                        // first get reference to the actual media items
                        it.data?.let { quranModel ->
                            swipeQuranAdapter.quran = quranModel // it: list we got here in observer
                            // load the image from first song into the viewImage that next to viewPager in bottom
                            // this media items observer called single time when app launches
                            if (quranModel.isNotEmpty()) // this check because if it was empty and we want display the image from the first song then will crash
                                glide.load((curPlayingQuran) ?: quranModel[0].imageUrl)
                                    .into(ivCurQuranImage)
                            switchViewPagerToCurrentQuran(curPlayingQuran ?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit
                }
            }
        }
        // everytime we get new information like if the song is switches the this observer will trigger, and we will just update the currently playing song of this mainActivity and also switch the viewPager accordingly
        mainViewModel.curPlayingQuran.observe(this) {
            if (it == null) return@observe

            // if not null update curPlayingQuran with it, but it type of mediaMetadataCompat and curPlayingQuran type of QuranModel
            // so write extension fun for this mediaMetadataCompat class that convert its instance to custom quranModel object
            curPlayingQuran = it.toQuran()
            glide.load(curPlayingQuran?.imageUrl).into(ivCurQuranImage)
            switchViewPagerToCurrentQuran(curPlayingQuran ?: return@observe)

        }
        // this observer will be called every time the playback state changes like pause or play song
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        mainViewModel.isConnected.observe(this) {
            // we used event object for not show snack bar twice, if we rotate the screen usually this observer will fire off again but since we wrapped this around event object this won't happen because inside event class we handle that only emitted once
            // first time return resource of boolean but then this getContent if not handle function is called so the second time return null
            it?.getContentIfNotHandle()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        rootLayout,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
        mainViewModel.networkError.observe(this) {
            // we used event object for not show snack bar twice, if we rotate the screen usually this observer will fire off again but since we wrapped this around event object this won't happen because inside event class we handle that only emitted once
            // first time return resource of boolean but then this getContent if not handle function is called so the second time return null
            it?.getContentIfNotHandle()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        rootLayout,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }
}