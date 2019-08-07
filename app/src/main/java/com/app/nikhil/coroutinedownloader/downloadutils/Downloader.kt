package com.app.nikhil.coroutinedownloader.downloadutils

import com.app.nikhil.coroutinedownloader.entity.DownloadInfo
import kotlinx.coroutines.channels.Channel

interface Downloader {
  suspend fun pause(url: String)
  suspend fun disposeDownload(url: String)
  fun download(url: String)
  fun pauseQueue()
  fun resumeQueue()
  fun disposeAll()
  fun getChannel(url: String): Channel<DownloadInfo>?
}