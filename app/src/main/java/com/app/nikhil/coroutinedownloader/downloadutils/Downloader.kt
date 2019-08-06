package com.app.nikhil.coroutinedownloader.downloadutils

import com.app.nikhil.coroutinedownloader.entity.DownloadInfo
import kotlinx.coroutines.channels.Channel

interface Downloader {
  fun download(url: String)
  fun pause(url: String)
  fun pauseQueue()
  fun resumeQueue()
  fun dispose()
  fun getChannel(url: String): Channel<DownloadInfo>?
}