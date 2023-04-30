package com.plcoding.spotifycloneyt.exoplayer

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING

// put extension variables
inline val PlaybackStateCompat.isPrepared // easily check if our quran service is in prepared state
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_PAUSED
//if one if this state is true we know that music player in prepare state

inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING
//if one if this state is true we know that music player in prepare state

inline val PlaybackStateCompat.isPlayEnabled // if the option to play a song is enabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L || // (binary and) for examples (AND-OR-XOR), that expression will be true if play is enabled
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L && // check if the play pause itself is enabled and if we are in the pause state then we can play the song
                  state == PlaybackStateCompat.STATE_PAUSED) //, because if we are in the play state then it doesn't make sense to check if playEnabled because we already playing song

// to calculate curPlayerPosition
//inline val PlaybackStateCompat.currentPlaybackPosition: Long
//    get() = if (state == STATE_PLAYING){
//        /* we only get a specific position that was last updateed in our player, so exoplayer won't continuously update the current playback position instead it will only do
//         instead it will only do it here and then and with these here and then values we can calculate the exact positio */
//                val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime // return amount of milliseconds since system boot and sub that to lastPositionUpdateTime to get time different
//                // calculate playback position with that
//        (position + (timeDelta * playbackSpeed)).toLong()
//    }else position

inline val PlaybackStateCompat.currentPlaybackPosition: Long
    get() = if(state == STATE_PLAYING) {
        val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (timeDelta * playbackSpeed)).toLong()
    } else position