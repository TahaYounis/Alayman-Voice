package com.plcoding.spotifycloneyt.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.plcoding.spotifycloneyt.data.entities.QuranModel
import com.plcoding.spotifycloneyt.data.remote.MyDatabase
import com.plcoding.spotifycloneyt.exoplayer.State.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/* create a class for quran source which will fetch the data from firebase.
we will on the one hand make sure to get all the songs from fireStore database, and also convert this songs
format into a format that we need for our service so for our media sessions and all that stuff */
class FirebaseQuranSource @Inject constructor(
    private val myDatabase: MyDatabase
){
    /* when we download our data from fireStore this usually takes a little bit of time, and since we will
    launch that in the coroutine we somehow need a mechanism to check when the music source (our songs) are
    finished downloading (so created state class), and in our service we need often need immediate result for that,
    and with onReadyListener we can schedule actions that we want perform when that music source is finished. */

        // implement the list that contain media meta data compat, it can potentially hold much more meta information about the song
    // and that the format we need our songs to be in our type of service
    var listOfQuran = emptyList<MediaMetadataCompat>()
    // fun gets all of our song objects from firebase, withContext(Dispatchers.IO) to switch our coroutine
    // to IO thread which optimized for IO operations such as network or database operations
    suspend fun fetchMediaData(){
        state = STATE_INITIALIZING
        withContext(Dispatchers.IO) {
            val allQuran = myDatabase.getAllQuran()
            listOfQuran = allQuran.map { chapterOfQuran ->
                MediaMetadataCompat.Builder()
                    .putString(METADATA_KEY_ARTIST, chapterOfQuran.subtitle)
                    .putString(METADATA_KEY_MEDIA_ID, chapterOfQuran.mediaId)
                    .putString(METADATA_KEY_TITLE, chapterOfQuran.title)
                    .putString(METADATA_KEY_DISPLAY_TITLE, chapterOfQuran.title)
                    .putString(METADATA_KEY_DISPLAY_ICON_URI, chapterOfQuran.imageUrl)
                    .putString(METADATA_KEY_MEDIA_URI, chapterOfQuran.songUrl)
                    .putString(METADATA_KEY_ALBUM_ART_URI, chapterOfQuran.imageUrl)
                    .putString(METADATA_KEY_DISPLAY_SUBTITLE, chapterOfQuran.subtitle)
                    .putString(METADATA_KEY_DISPLAY_DESCRIPTION, chapterOfQuran.subtitle)
                    .build()
            }
        }
        state = STATE_INITIALIZED /* wherever we set the value of state to something we will run setter, and when STATE_INITIALIZED
         we will go through our onReadyListener list (list of lambda fun) and call all this list with INITIALIZED equal to true */
        /* whenever we wait for quran source to finish loading the lambda fun will be called there,
        so we can continue with normal stuff that we need to do when that is loaded */
    }

    /* ConcatenatingMediaSource contains information for exoplayer from where it can stream that actual song
       , create concatenating music source  to list several single music sources to play song and when it finish automatically
       play the second song and so on, convert list of quran into media source object */
        fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()
        listOfQuran.forEach{ mediaMetadataCompat->
            // create single media source and add to concatenatingMediaSource
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(mediaMetadataCompat.getString(METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }
    /*another fun also about formatting, which a kind of a single item in our list for example song or either
    a playlist an album so a browsable so when click on that item in and it will play new list of songs,
    we need a list of media items in our quran service so we will create a fun to convert that*/
    fun asMediaItems() = listOfQuran.map { mediaMetadataCompat->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(mediaMetadataCompat.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(mediaMetadataCompat.description.title)
            .setSubtitle(mediaMetadataCompat.description.subtitle)
            .setMediaId(mediaMetadataCompat.description.mediaId)
            .setIconUri(mediaMetadataCompat.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    // FLAG_PLAYABLE: if it song and will play direct FLAG_BROWSABLE: if it album or playlist and will browse to another page
    }.toMutableList()

    // Those lambda fun take boolean that will tell us if the source was initialized or not, when music source ready then execute the block of code that comes afterwards
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    // create instance of class state to check the state our music source is currently in
    private var state: State = STATE_CREATED
        set(value) {
            // check if we set music source(value) to initialized or error then we know it is finished so no more will happened
            if (value == STATE_INITIALIZED || value == STATE_ERROR){
                // we want do that in thread save way, so can also change state form multiple thread at once
                synchronized(onReadyListeners){
                    // no other thread can access onReadyListener list at the same time
                    field = value // field: current value of state, value: new value, so assign new state to the state
                    onReadyListeners.forEach {listener->
                        listener(state == STATE_INITIALIZED) // if it not equal STATE_INITIALIZED then it equal STATE_ERROR
                    }
                }
            }else{
                field = value
            }
        }

    // fun will add onReadyListener to list, action: it the action want perform that want to perform when music source ready
    fun whenReady(action: (Boolean) -> Unit):Boolean{
        if (state == STATE_CREATED || state == STATE_INITIALIZING){
// schedule action for later part, when music source ready then want call action and add it to our list
            onReadyListeners += action
            return false // false because our music source not ready
        }else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

//another class to define several states this music source can be in
enum class State {
    STATE_CREATED,
    STATE_INITIALIZING, // before we download our songs we will set it to initializing
    STATE_INITIALIZED, // after we download our songs we will set it to initialized
    STATE_ERROR
}