package com.plcoding.spotifycloneyt.exoplayer.callbacks

import android.app.Notification
import android.app.Service.STOP_FOREGROUND_DETACH
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.plcoding.spotifycloneyt.exoplayer.QuranService
import com.plcoding.spotifycloneyt.other.Constants.NOTIFICATION_ID

class QuranPlayerNotificationListener(
    private val quranService: QuranService
) : PlayerNotificationManager.NotificationListener{

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        /* the case when user has currently paused player so he can swipe that away, and if the music is
         playing then this is ongoing notification so we can't swipe it away*/
        /* what do we want to do when notification is canceled like swipe it away we want to use QuranService
        and stop the current foreground service */
        quranService.apply {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        quranService.apply {
            if(ongoing && !isForegroundService){
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext,this::class.java) // this refers to quranService class
                )
                startForeground(NOTIFICATION_ID,notification)
                isForegroundService = true
            }
        }
    }
}