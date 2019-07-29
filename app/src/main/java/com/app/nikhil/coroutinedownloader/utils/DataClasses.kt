package com.app.nikhil.coroutinedownloader.utils

import kotlinx.coroutines.channels.Channel

data class DownloadItem(
  val fileName: String,
  val url: String,
  val channel: Channel<DownloadInfo>
)

data class DownloadInfo(
  val percentage: String,
  val bytesDownloaded: String,
  val totalBytes: String
)