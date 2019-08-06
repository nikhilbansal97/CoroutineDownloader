package com.app.nikhil.coroutinedownloader.utils

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import javax.inject.Inject

class NotificationUtils @Inject constructor(private val context: Context) {

  companion object {
    private const val NOTIFICATION_CHANNEL = "CHANNEL-001"
  }

  fun createNotification(title: String, text: String): Notification {
    return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
        .setContentTitle(title)
        .setContentText(text)
        .build()
  }
}