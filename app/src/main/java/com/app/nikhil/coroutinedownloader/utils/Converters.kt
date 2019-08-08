package com.app.nikhil.coroutinedownloader.utils

import androidx.room.TypeConverter
import com.app.nikhil.coroutinedownloader.models.DownloadState

class Converters {

  @TypeConverter
  fun fromDownloadState(state: DownloadState): Int = state.value

  @TypeConverter
  fun toDownloadState(value: Int): DownloadState {
    return when (value) {
      1 -> DownloadState.PENDING
      2 -> DownloadState.DOWNLOADING
      3 -> DownloadState.PAUSED
      else -> DownloadState.COMPLETED
    }
  }
}