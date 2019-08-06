package com.app.nikhil.coroutinedownloader.entity

data class DownloadInfo(
  val url: String,
  val percentage: String,
  val bytesDownloaded: String,
  val totalBytes: String
)