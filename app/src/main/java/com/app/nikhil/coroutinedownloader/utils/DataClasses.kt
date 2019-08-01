package com.app.nikhil.coroutinedownloader.utils

data class DownloadItem(
  val fileName: String,
  val url: String
)

data class DownloadInfo(
  val percentage: String,
  val bytesDownloaded: String,
  val totalBytes: String
)