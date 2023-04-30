package com.plcoding.spotifycloneyt.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.plcoding.spotifycloneyt.data.entities.QuranModel

// this fun will take mediaMetadataCompat object that the object we call this fun on and convert that to song which is easier to us to work with
fun MediaMetadataCompat.toQuran(): QuranModel? {
    // description of this mediaMetadataCompat object
    return description?.let {
        QuranModel(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString(),
        )
    }

}