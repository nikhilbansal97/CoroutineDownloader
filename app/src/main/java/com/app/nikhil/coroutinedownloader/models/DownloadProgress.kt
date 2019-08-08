package com.app.nikhil.coroutinedownloader.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadProgress(
  @PrimaryKey val url: String,
  val fileName: String,
  var percentage: String = "0",
  var bytesDownloaded: String = "0",
  var totalBytes: String = "0",
  var state: DownloadState = DownloadState.PENDING
)