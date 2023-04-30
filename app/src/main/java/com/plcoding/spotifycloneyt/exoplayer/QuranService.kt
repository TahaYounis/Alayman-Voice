package com.plcoding.spotifycloneyt.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.plcoding.spotifycloneyt.exoplayer.callbacks.QuranPlaybackPreparer
import com.plcoding.spotifycloneyt.exoplayer.callbacks.QuranPlayerEventListener
import com.plcoding.spotifycloneyt.exoplayer.callbacks.QuranPlayerNotificationListener
import com.plcoding.spotifycloneyt.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifycloneyt.other.Constants.NETWORK_ERROR
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

//in exoplayer will handle all stuff that has something to do with our service or music playing stuff
//we will handle all the music playing logic in a background service
private const val SERVICE_TAG = "QuranService"
@AndroidEntryPoint
class QuranService : MediaBrowserServiceCompat() {
    /* we used mediaBrowserServiceCompat instead Service because it help us manage files in the app like if
    you want create albums or playlist contain musics MediaBrowserServiceCompat give us tools to to this easier */

    @Inject // then dagger hilt will automatically recognize that we want inject a variable here on object dataSourceFactory
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var firebaseQuranSource: FirebaseQuranSource

    private lateinit var quranNotificationManager: QuranNotificationManager


    /* declare a coroutine scope specific to the service so that we can use coroutine in service because it should do
 if you just use plain service because a service is not asynchronous by default because it still run in
 the main thread, and when we want to use our music source later on firebase to fetch those songs we want do that
 not on the main thread because that would block the ui */
    // we have coroutine to limit to the lifetime of our service
    private val serviceJob = Job()
    /* the scope in which we launch coroutines the service scope will deal with cancellation of coroutine
    so that define the lifetime of coroutine that we launch inside of this service and will make sure that
    when this service dies that also the coroutine in it that are still running that also will cancelled
    and not lead to memory leeks*/
    // coroutine scope has the properties of our main dispatcher and service job together, and that allow us define a custom service scope
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    /* we need to create media session because we if we play music with media browser service then we always have
    this media session, so the current session of playing music and that contains important information about
    the media session and we can use that to communicate with service */
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector // class used to connect to media session

    var isForegroundService = false // variable in which we save if the service is currently foreground or not

    private var curPlayingChapterOfQuran : MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private lateinit var quranPlayerEventListener : QuranPlayerEventListener

    companion object{
        var curChapterOfQuranDuration = 0L
           private set
    }

    override fun onCreate() {
        super.onCreate()
        //load our firebase quran source immediately after service created, we do asynchronously with coroutine
        serviceScope.launch {
            firebaseQuranSource.fetchMediaData()
        }
        // get the activity intent for our notification so when click on notification we want open our activity
        // we can get that by using packageManager, this will give us a normal intent that leads to our activity
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)
        }
        // we want initialize our media session
        mediaSession = MediaSessionCompat(this,SERVICE_TAG).apply {
            //set that to our activity intent
            setSessionActivity(activityIntent)
            // also want set mediaSession to active
            isActive = true
        }
        /* media session comes with session token that we can use it to get information about this media session
         and since we extend from MediaBrowserServiceCompat and that class have session toke too, so we need to assign
         the token of our media session to our service*/
        sessionToken = mediaSession.sessionToken

        // to update the current duration of the song that is playing
        quranNotificationManager = QuranNotificationManager(
            this,
            mediaSession.sessionToken,
            QuranPlayerNotificationListener(this)
            // lambda fun will call when the current song switches
        ){
            curChapterOfQuranDuration = exoPlayer.duration
        }

        var quranPlaybackPrepare = QuranPlaybackPreparer(firebaseQuranSource){
            // we need this lambda block to get current MediaMetadataCompat object after player is prepared
            curPlayingChapterOfQuran = it // this lambda block be called every time the player chose new song
            preparePlayer(
                firebaseQuranSource.listOfQuran,
                it,
                true
            )
        }
        // our media session connector is missing so also want to initialize that
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(quranPlaybackPrepare)
        mediaSessionConnector.setQueueNavigator(QuranQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        quranPlayerEventListener = QuranPlayerEventListener(this)
        exoPlayer.addListener(quranPlayerEventListener)
        quranNotificationManager.showNotification(exoPlayer) // show notification
    }
    // class used to propagate information about specific song the metadata to our notification
    private inner class QuranQueueNavigator : TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            val  sharedPreferences = applicationContext.getSharedPreferences("IndexOfQuran",0)
            val index = sharedPreferences.getInt("IndexOfChapterOfQuran",0)
            return firebaseQuranSource.listOfQuran[index].description
        }
    }
    // we want prepare our exoplayer
    private fun preparePlayer(
        listOfQuran : List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?, // the current song we want to play
        playNow: Boolean // if we directly play this song or not
    ){
        // if we didn't specific we didn't chose any song then we want play the first one if not we need find song of index itemToPlay
        val curChapterOfQuranIndex = if(curPlayingChapterOfQuran == null) 0 else listOfQuran.indexOf(itemToPlay)
        exoPlayer.addMediaSource(firebaseQuranSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(curChapterOfQuranIndex,0L) // to make song start from begging
        // true if we want song play directly when open the app, false when we want user open th song, and we want when the app first time opened the song not play so false and after play song make it true
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    /* make sure that all of coroutine launched in the server scope are cancelled when the service dies  */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(quranPlayerEventListener) // to prevent any memory leaks
        exoPlayer.release() // to release or leave the resource of exoplayer
    }

    /* this browsable app that we can have many files like playlists or albums, and if click on that we load
    bunch of media items so media item can again be a browsable item like a playlist or a song that we can play
    for that we need root id the id that refers the the very first media items in our case we just have
    firebase quran source so the chapters that we get from firebase, but in more complicated app that could be
    all your playlist at once displays at first, since we use bound service that mean several clint cant connect
    to that but in our case just our activity and viewModel that means we could potentially also deny clients to
    connect to specific id on our case we don't want that, if you want have some verification logic for clients then do that in onGetRoot*/
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }
    /* if you have more that playlist every playlist have id, and clients can subscribe to those ids for example
    play items in a specific playlist, and that will happened in onLoadChildren */
    override fun onLoadChildren( // if you want make your app more browsable you need to add more stuff here
        parentId: String, // id we can call to get a list of songs, we could call the root id which will give the default songs then we could have id for a specific playlist that would return the songs in playlist
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // parentId: the id client subscribed to, and according to that we have result to give back the results so the list of songs inside of the playlist
        // when we subscribe to parentId we need to send the corresponding result with the song or media item in that parent id
        when(parentId){ // we can add more root ids for more playlist or albums
            MEDIA_ROOT_ID -> {
                // use result to return media item inside of that root id, onLoadChildren called pretty early in this service so we need use whenReady to make sure source is ready before
                val resultSent = firebaseQuranSource.whenReady {
                    if (it){
                        result.sendResult(firebaseQuranSource.asMediaItems()) // because send result as media items so use asMediaItems()
                        /* usually when we call result first time send we want to prepare our player so we need a variable to check if our player
                        has been initialized because if we don't do this then that lead to our player automatically playing the song once open our app so we want prevent that*/
                        if (!isPlayerInitialized && firebaseQuranSource.listOfQuran.isNotEmpty()){
                            preparePlayer(firebaseQuranSource.listOfQuran, firebaseQuranSource.listOfQuran[0],false)
                            isPlayerInitialized = true
                        }
                    }else{ // if our source not initialized, network error
                        mediaSession.sendSessionEvent(NETWORK_ERROR,null)
                        result.sendResult(null)
                    }
                }
                /* because onLoadChildren perform early in service so if source not ready we want to till
                that results are not ready yet but they will become ready */
                if (!resultSent){
                    result.detach()
                }
            }
        }
    }
}