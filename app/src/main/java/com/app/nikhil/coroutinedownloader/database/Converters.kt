package com.app.nikhil.coroutinedownloader.database

import androidx.room.TypeConverter
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import com.app.nikhil.coroutinedownloader.models.DownloadState
import com.google.gson.Gson

class Converters {

  private val gson = Gson()

  @TypeConverter
  fun fromDownloadState(state: DownloadState): Int = state.ordinal

  @TypeConverter
  fun toDownloadState(value: Int): DownloadState {
    return when (value) {
      DownloadState.PENDING.ordinal -> DownloadState.PENDING
      DownloadState.DOWNLOADING.ordinal -> DownloadState.DOWNLOADING
      DownloadState.COMPLETED.ordinal -> DownloadState.COMPLETED
      else -> DownloadState.PAUSED
    }
  }

  @TypeConverter
  fun fromDownloadProgress(progress: DownloadProgress): String {
    return gson.toJson(progress)
  }

  @TypeConverter
  fun toDownloadProgress(progressString: String): DownloadProgress {
    return gson.fromJson(progressString, DownloadProgress::class.java)
  }
}