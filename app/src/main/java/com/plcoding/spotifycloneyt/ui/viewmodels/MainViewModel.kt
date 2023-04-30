package com.plcoding.spotifycloneyt.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.exoplayer.QuranServiceConnection
import com.plcoding.spotifycloneyt.exoplayer.isPlayEnabled
import com.plcoding.spotifycloneyt.exoplayer.isPlaying
import com.plcoding.spotifycloneyt.exoplayer.isPrepared
import com.plcoding.spotifycloneyt.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifycloneyt.other.Resources
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/* when we want inject dependencies using dagger hilt into ViewModel this doesn't work as usual because viewModel
need ViewModelFactory if you want use constructor for them because of that we used annotation, it till dagger hilt
this is a viewModel it needs to treat this in a special way it need to create a new viewModel factory for it then
inject those dependencies for us that specify in constructor */
@HiltViewModel
class MainViewModel @Inject constructor(
    // don't forget provide fun in AppModule so dagger hilt know what should inject here for us,
    private val quranServiceConnection: QuranServiceConnection
) : ViewModel(){
    // for the new type of livedata that contains list of media items for our mainActivity so we can display them in recyclerView
    private val _mediaItems = MutableLiveData<Resources<List<QuranModel>>>()
    val mediaItem: LiveData<Resources<List<QuranModel>>> = _mediaItems

    val isConnected = quranServiceConnection.isConnected
    val networkError = quranServiceConnection.networkError
    val curPlayingQuran = quranServiceConnection.curPlayingQuran
    val playbackState = quranServiceConnection.playbackState

    // we want subscribe to our media root id, so that can observe on the media items it will return
    init {
        _mediaItems.postValue(Resources.loading(null)) // post the loading status because now we start to query this media items
        quranServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){ // callback to be notified once this subscription is finish
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map{
                    QuranModel( // want to go to this list and map this mediaItem to our custom QuranModel objects that our livedata accepts
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString(),
                    )
                }
                _mediaItems.postValue(Resources.success(items))
            }
        })
    }
    // fun to control our currently playing song for example skip or skip to previous song and fun to seek to specific position
    fun skipToNextQuranChapter(){
        quranServiceConnection.transportControls.skipToNext()
    }
    fun skipToPreviousQuranChapter(){
        quranServiceConnection.transportControls.skipToPrevious()
    }
    fun seekTo(position : Long){ // we will need this fun in our song fragment and that seek bar
        quranServiceConnection.transportControls.seekTo(position)
    }

    // fun to play a song to toggle the currently playing state, and implement some extension variables (from google) for the playback state compat class
    fun playOrToggleQuran(mediaItem: QuranModel, toggle: Boolean=false){ // toggle boolean by default we don't toggle the state, if we press on pause button for example toggle will be true, if we tap on the new song toggle will be false because then we want to play that new song
        val isPrepared = playbackState.value?.isPrepared ?: false // if our player is prepared and we get the information from the extension variables, if equal to null (?:) we will set false so we then assume that player is not prepared
        // if our player is prepared, id of media item we want play or toggle now equal to curPlayingQuran value id, this how to get media id of currently playing song
        if (isPrepared && mediaItem.mediaId ==
            curPlayingQuran.value?.getString(METADATA_KEY_MEDIA_ID)){
            // get reference to oru playback state
            playbackState.value?.let { playbackState->
                when{
                    playbackState.isPlaying -> if(toggle) quranServiceConnection.transportControls.pause() // check if toggle is true because if is true we know that we want to toggle the state because we are playing we now want to paused that song
                    // means that we kind of pause the song and then we want to play the same song again in that case we want play that song
                    playbackState.isPlayEnabled -> quranServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        }else{ // we know that we want to play a new song so the song is not currently playing
            quranServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        quranServiceConnection.unSubscribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){}) // empty callback we don't need it
    }
}