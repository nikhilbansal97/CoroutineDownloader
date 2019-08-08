package com.app.nikhil.coroutinedownloader.database

import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CentralRepository @Inject constructor(private val database: DownloadDatabase) {

  suspend fun getAllDownloadItemsInfo(): List<DownloadProgress> {
    return withContext(Dispatchers.IO) {
      database.getDao()
          .getAll()
    }
  }
}