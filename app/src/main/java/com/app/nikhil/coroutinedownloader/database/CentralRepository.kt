package com.app.nikhil.coroutinedownloader.database

import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.models.DownloadState.PAUSED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CentralRepository @Inject constructor(private val database: DownloadDatabase) {

  suspend fun getAllDownloadItems(): List<DownloadItem> {
    return withContext(Dispatchers.IO) {
      database.getDao()
          .getAll()
    }
  }

  suspend fun saveAllDownloadItems(downloadItemList: List<DownloadItem>) {
    withContext(Dispatchers.IO) {
      for (item: DownloadItem in downloadItemList) {
        item.downloadProgress.state = PAUSED
      }
      database.getDao().insertAll(downloadItemList)
    }
  }

  suspend fun saveDownloadItem(item: DownloadItem) {
    withContext(Dispatchers.IO) {
      database.getDao().insert(item)
    }
  }
}