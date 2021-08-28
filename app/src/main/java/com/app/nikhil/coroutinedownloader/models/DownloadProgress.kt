package com.app.nikhil.coroutinedownloader.models

data class DownloadProgress(
  var megaBytesDownloaded: String,
  var percentage: Int,
  var percentageDisplay: String,
  var totalMegaBytes: String,
  var bytesDownloaded: Long,
  var totalBytes: Long,
  var state: DownloadState
) {
  companion object {
    val EMPTY: DownloadProgress
      get() = DownloadProgress(
        megaBytesDownloaded = "0",
        percentage = 0,
        percentageDisplay = "0",
        totalMegaBytes = "0",
        bytesDownloaded = 0L,
        totalBytes = 0L,
        state = DownloadState.PENDING
      )
  }
}