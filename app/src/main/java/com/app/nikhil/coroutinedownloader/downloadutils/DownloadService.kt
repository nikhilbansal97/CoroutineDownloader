package com.app.nikhil.coroutinedownloader.downloadutils

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.utils.NotificationUtils
import dagger.android.DaggerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadService : DaggerService() {

  private val serviceScope by lazy { CoroutineScope(Dispatchers.IO) }
  private val mainScope by lazy { CoroutineScope(Dispatchers.Main) }

  companion object {
    private const val MAX_PROGRESS = 100
    private const val NOTIFICATION_ID = 1
    private const val NOTIFICATION_MESSAGE = "Download Manager active."
  }

  @Inject
  lateinit var downloadManager: DownloadManager
  @Inject
  lateinit var notificationUtils: NotificationUtils

  override fun onCreate() {
    super.onCreate()
    startForeground(NOTIFICATION_ID, notificationUtils.createNotification( NOTIFICATION_MESSAGE))
  }

  override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int
  ): Int {
    return super.onStartCommand(intent, flags, startId)
  }

  override fun onBind(p0: Intent?): IBinder? = null

  fun download(url: String): DownloadItem {
    val downloadItem = downloadManager.download(url)
    val receiveChannel = downloadItem.channel.openSubscription()
    mainScope.launch {
      receiveChannel.consumeEach {
        if (it.percentage % 20 == 0) {
          val builder = notificationUtils.getSimpleBuilder().apply {
            setProgress(MAX_PROGRESS, it.percentage, false)
          }
          if (it.percentage == MAX_PROGRESS) {
            builder.setContentTitle("Downloading")
          } else {
            builder.setContentTitle("Downloaded")
            receiveChannel.cancel()
          }
          notificationUtils.showNotification(builder.build())
        }
      }
    }
    return downloadItem
  }

  override fun onDestroy() {
    downloadManager.disposeAll()
    serviceScope.cancel()
    super.onDestroy()
  }

  inner class DownloadServiceBinder : Binder() {
    fun getService(): DownloadService = this@DownloadService
  }
}