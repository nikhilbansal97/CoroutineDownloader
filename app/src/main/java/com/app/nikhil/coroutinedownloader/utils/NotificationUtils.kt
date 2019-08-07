package com.app.nikhil.coroutinedownloader.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import javax.inject.Inject

class NotificationUtils @Inject constructor(private val context: Context) {

  companion object {
    private const val CHANNEL_ID = "CHANNEL-001"
    private const val CHANNEL_NAME = "CoroutineDownloader Channel"
  }

  fun createNotification(
    title: String,
    text: String
  ): Notification {
    val builder = NotificationCompat.Builder(context)
        .setContentTitle(title)
        .setContentText(text)

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(
          NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
      )
      builder.apply {
        setChannelId(CHANNEL_ID)
        setOngoing(true)
      }
    }
    return builder.build()
  }
}