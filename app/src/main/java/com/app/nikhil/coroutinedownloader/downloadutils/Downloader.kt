package com.app.nikhil.coroutinedownloader.downloadutils

import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import kotlinx.coroutines.channels.BroadcastChannel

interface Downloader {
  suspend fun pause(downloadItem: DownloadItem)
  suspend fun resumeQueue()
  suspend fun pauseQueue()
  fun download(url: String): DownloadItem
  fun onProgressChanged(url: String, function: (item: DownloadProgress) -> Unit)
  fun disposeDownload(url: String)
  fun disposeAll()
  fun getChannel(url: String): BroadcastChannel<DownloadProgress>?
}