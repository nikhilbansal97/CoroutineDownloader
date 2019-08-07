package com.app.nikhil.coroutinedownloader.downloadutils

import android.content.Intent
import com.app.nikhil.coroutinedownloader.utils.NotificationUtils
import dagger.android.DaggerIntentService
import javax.inject.Inject

class DownloadService : DaggerIntentService("DownloadService") {

  @Inject
  lateinit var downloader: Downloader
  @Inject
  lateinit var notificationUtils: NotificationUtils

  override fun onHandleIntent(p0: Intent?) {
    startForeground(1, notificationUtils.createNotification("Test", "Hi There!!"))
    downloader.resumeQueue()
  }

  override fun onDestroy() {
    downloader.disposeAll()
    super.onDestroy()
  }
}