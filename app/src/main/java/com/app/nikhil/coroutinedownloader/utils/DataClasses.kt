package com.app.nikhil.coroutinedownloader.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel

data class DownloadItem(
  val fileName: String,
  val url: String,
  var channel: Channel<DownloadInfo>,
  var job: Job? = null
) {
  fun pause() {
    job?.cancel()
    channel.close()
  }
}

data class DownloadInfo(
  val percentage: String,
  val bytesDownloaded: String,
  val totalBytes: String
)