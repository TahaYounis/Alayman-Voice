package com.plcoding.spotifycloneyt.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.plcoding.spotifycloneyt.other.Constants.NETWORK_ERROR
import com.plcoding.spotifycloneyt.other.Event
import com.plcoding.spotifycloneyt.other.ResourcesForLiveData

/* class take care of or sits between our activity or fragment and service, the communicator that exchange
information from activity to service or from service to activity, make use of resource class for error handling
and event class prevent specific events are fired off multiple time so that we observe on them multiple time */
class QuranServiceConnection(
    context:Context
) {
    // implement a bunch of livedata objects that contains the information from our service, and we will observe on later on in our fragments to get these information and get updates if data in our service changes
    private val _isConnected =MutableLiveData<Event<ResourcesForLiveData<Boolean>>>() // livedata contain contains current state if connection between activity and quran service is active
    val isConnected: LiveData<Event<ResourcesForLiveData<Boolean>>> = _isConnected // livedata we observe on from outside

    private val _networkError =MutableLiveData<Event<ResourcesForLiveData<Boolean>>>()
    val networkError: LiveData<Event<ResourcesForLiveData<Boolean>>> = _networkError

    private val _playbackState =MutableLiveData<PlaybackStateCompat?>() // whether the player is currently playing or paused
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _curPlayingQuran =MutableLiveData<MediaMetadataCompat?>() // mediaMetadataCompat object contains meta information about song is currently playing
    val curPlayingQuran: LiveData<MediaMetadataCompat?> = _curPlayingQuran

    lateinit var mediaController: MediaControllerCompat // object we can use on the one hand to get access to transport controls that is used to for example pause or play song and skip song and stuff like that

    // we need instance of MediaBrowserConnectionCallback to create our media browser instance, get info. about service
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    // mediaBrowser mainly used to get access to that access token and subscribe and unsubscribe to our service to our actual media ids
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, QuranService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        // apply because we want call the fun that it connects onConnected()
        connect()
    }

    // get reference to these transport controls, used to skip songs or previous song and pause or resume player and more stuff
    val transportControls: MediaControllerCompat.TransportControls
        // we used getter because we cannot yet instantiate this media controller object here because it lateinit var
        get() = mediaController.transportControls
    //we need access to the session token of our service and we only have access to that in a callback that we will define here in service connection

    // fun to subscribe to specific media id, call from viewModel to start subscription to specific media id to get access to our media items form firebase
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId, callback)
    }
    fun unSubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId, callback)
    }

    // callback for media browser, to listen to events when connection happened and when suspended and when failed
    private inner class MediaBrowserConnectionCallback (
        private val context: Context) : MediaBrowserCompat.ConnectionCallback(){
            // once quran service connection is active call this fun, we create instance of mediaController because for that we need a sessionToken once we connected here we have access to that
            override fun onConnected() {
                mediaController = MediaControllerCompat(context,mediaBrowser.sessionToken).apply {
                    // call apply because we want to register a callback which
                    registerCallback(MediaControllerCallback())
                }
                // post connection status to our _isConnected live data
                _isConnected.postValue(Event(ResourcesForLiveData.success(true)))
            }

            override fun onConnectionSuspended() {
                _isConnected.postValue(
                    Event(ResourcesForLiveData.error(
                    "The connection was suspended", false
                )))
            }
        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(ResourcesForLiveData.error(
                "Couldn't connect to media browser", false
            )
                )
            )
        }
        }

    // implement callback when important data of our service changes
    private inner class MediaControllerCallback : MediaControllerCompat.Callback(){
        // when playback state changes for example user pause or resume it this fun will be called
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state) // use our playback state livedata to post the new state so we can observe on that from our fragments
        }
        // call this fun when for example skip song with new metadata information about new song
        // whenever currently song metadata changes then we want to post new value to _curPlayingQuran livedata
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingQuran.postValue(metadata)
        }
        // used to kind of send custom events from our to this connection callback, and we will used that to be notified when there is network error
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event){
                //if we send such a session event here we want to use our network error livedata
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        ResourcesForLiveData.error(
                            // post error resource here if we catch this network error
                            "Please check your internet connection",
                            null
                        )))
            }
        }
        // use event when session destroyed
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}