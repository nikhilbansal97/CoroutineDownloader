package com.app.nikhil.coroutinedownloader.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

@Entity
class DownloadItem(
  @PrimaryKey
  val url: String,
  val fileName: String,
  var fileSize: Long = 0
) {
  @Ignore
  var downloadProgress: DownloadProgress = DownloadProgress()
  @Ignore
  var channel: BroadcastChannel<DownloadProgress> = ConflatedBroadcastChannel()
}

class DownloadProgress(
  var bytesDownloaded: String = "",
  var percentage: Int = 0,
  var percentageDisplay: String = "",
  var totalBytes: String = "",
  var state: DownloadState = DownloadState.PENDING
)