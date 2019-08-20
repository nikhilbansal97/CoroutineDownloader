package com.app.nikhil.coroutinedownloader.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import com.app.nikhil.coroutinedownloader.R
import javax.inject.Inject

class NotificationUtils @Inject constructor(private val context: Context) {

  companion object {
    private const val CHANNEL_ID = "CHANNEL-001"
    private const val CHANNEL_NAME = "CoroutineDownloader Channel"
  }

  private var currentId = 2

  private val manager: NotificationManager by lazy {
    context.getSystemService(
        NOTIFICATION_SERVICE
    ) as NotificationManager
  }

  private val builder: NotificationCompat.Builder by lazy {
    val builder = NotificationCompat.Builder(context)
        .setSmallIcon(R.mipmap.ic_launcher)

    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      builder.apply {
        setChannelId(CHANNEL_ID)
        setOngoing(true)
      }
    }
    return@lazy builder
  }

  fun createNotification(
    text: String
  ): Notification {
    builder.setContentText(text)
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      manager.createNotificationChannel(
          NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
      )
    }
    return builder.build()
  }

  fun getSimpleBuilder(): NotificationCompat.Builder = builder

  fun showNotification(notification: Notification) {
    manager.notify(currentId++, notification)
  }
}