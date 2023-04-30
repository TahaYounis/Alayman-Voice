package com.plcoding.spotifycloneyt.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat.Token
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.other.Constants.NOTIFICATION_CHANNEL_ID
import com.plcoding.spotifycloneyt.other.Constants.NOTIFICATION_ID

class QuranNotificationManager(
    private val context: Context,
    sessionToken: Token,
    notificationListener: NotificationListener, // listener that contains functions that will be called when our notification is created for example swap away by the user
    private val newQuranCallBack: () -> Unit // because in this class we can detect when a new song starts plying, we will need this because we need to update the current duration of song
) {

    private val notificationManager: PlayerNotificationManager //custom class from exoplayer that can manage the media notification

    init {
        val mediaController = MediaControllerCompat(context,sessionToken)
        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
                /* mediaController use for control media we will pass pass session to it to get information about the currently playing song and control media */
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_music)
                setMediaSessionToken(sessionToken)// give our notification manager access to our media session in our quran service
            }
    }
    // fun to show notification used this fun in quran service class
    fun showNotification(player: Player){ // take current exoplayer
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter{
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newQuranCallBack()
            // return the title of the current playing song
            return mediaController.metadata.description.title.toString()
        }
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            //return the pending intent that leads to our activity
            return mediaController.sessionActivity
        }
        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }
        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? { // return null until image loading and then call callback in onResourceReady to return bitmap
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        /* we want to call this callback fun because the reason why we have callback is if we want to
                        asynchronously load an image here then it might take a little bit of time until this image loading,
                        and for that we have callback fun so when image finally loaded call callback fun */
                        callback.onBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            return null
        }
    }
}