package com.plcoding.spotifycloneyt.exoplayer.callbacks

import android.app.Service
import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.plcoding.spotifycloneyt.exoplayer.QuranService

/* check for when the playback state changes for example when player paused  */
class QuranPlayerEventListener(
    private val quranService: QuranService // be able to access quran service to stop the foreground service from within this listener
) : Player.Listener {
    // we will use just 2 fun form that class

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == Player.STATE_READY && !playWhenReady   ){
            // if the player ready but we shouldn't play it automatically we want stop service
            quranService.stopForeground(Service.STOP_FOREGROUND_DETACH)
        }
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        super.onPlayerErrorChanged(error)
        Toast.makeText(quranService, error?.message.toString(), Toast.LENGTH_LONG).show()
    }
}