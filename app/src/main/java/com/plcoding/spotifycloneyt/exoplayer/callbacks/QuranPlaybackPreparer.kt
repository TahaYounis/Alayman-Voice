package com.plcoding.spotifycloneyt.exoplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.plcoding.spotifycloneyt.exoplayer.FirebaseQuranSource

/* class provide functions that will be called for specific preparation events so when our player is prepared
and ready to play then we will implement a player event listener that is also a class that listens to specific
player events then we will write the actual functionality to prepare our players */
class QuranPlaybackPreparer(
    private val firebaseQuranSource: FirebaseQuranSource, // firebase source because we need to access to that
    private val playerPrepared : (MediaMetadataCompat?) -> Unit // lambda fun that will called once our player is prepared so we can respond to that from within quran service
) :MediaSessionConnector.PlaybackPreparer {

    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = false // we don't need it now, This method is called when the media session receives a custom command. It is responsible for handling the command and preparing the media for playback if necessary.

    override fun getSupportedPrepareActions(): Long {
        // return the type of actions that we supported in our player
        /* we want be able to prepare specific song with its media id that the reason of create field for
        media id in database, media id used for select specific song */
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID // be able to play (not prepare) a song with media id
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit // we don't need it now

    // This method is called when the media session receives a request to start playback of a specific media item identified by a media ID. It is responsible for preparing the media for playback, such as loading the media file or streaming the media from a remote server.
    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        // prepare specific song the user selected to play it afterwards
        firebaseQuranSource.whenReady {
            // to check if firebase source ready then we want to prepare it, because before that there is no reason to prepare it
            // mediaId that the the user want to prepare or play, playWhenReady once this is ready we want directly play
            /* first get item to play as a media metadata compat object and since in our FirebaseQuranSource we have
             a list of MediaMetadataCompat that fetch data and then want to find corresponding object with mediaId */
            val itemToPlay = firebaseQuranSource.listOfQuran.find {
                mediaId == it.description.mediaId
            }
            playerPrepared(itemToPlay)
            // so we have access to the chosen song in our MusicService afterwards
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit // we don't need it now

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit // we don't need it now
}