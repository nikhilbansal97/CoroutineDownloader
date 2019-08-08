package com.app.nikhil.coroutinedownloader.downloadutils

import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import kotlinx.coroutines.channels.Channel

interface Downloader {
  suspend fun download(url: String)
  suspend fun pause(url: String)
  fun disposeDownload(url: String)
  fun pauseQueue()
  fun resumeQueue()
  fun disposeAll()
  fun getChannel(url: String): Channel<DownloadProgress>?
}