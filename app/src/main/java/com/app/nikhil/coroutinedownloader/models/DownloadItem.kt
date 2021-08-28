package com.app.nikhil.coroutinedownloader.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

@Entity(tableName = "DownloadItemsTable")
data class DownloadItem(
  @PrimaryKey
  val url: String,
  val fileName: String,
  var downloadProgress: DownloadProgress = DownloadProgress.EMPTY
) {
  @Ignore
  lateinit var channel: ConflatedBroadcastChannel<DownloadProgress>
}