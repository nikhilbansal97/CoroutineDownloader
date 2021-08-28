package com.app.nikhil.coroutinedownloader.database

import androidx.lifecycle.LiveData
import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.models.DownloadState.PAUSED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CentralRepository @Inject constructor(private val database: DownloadDatabase) {

  suspend fun getAllDownloadItems(): List<DownloadItem> {
    return withContext(Dispatchers.IO) {
      database.getDao().getAll()
    }
  }

  fun getAllDownloadItemsLive(): LiveData<List<DownloadItem>> {
    return database.getDao().getAllItemsLive()
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

  suspend fun startSavingDownloadProgress(item: DownloadItem) {
    withContext(Dispatchers.IO) {
      Timber.d("[CDM] Starting a SQLite transaction")
      database.runInTransaction {
        this.launch {
          Timber.d("[CDM] Starting consuming the channel")
          item.channel.consumeEach { downloadProgress ->
            Timber.d("[CDM] received download progress ${downloadProgress.percentageDisplay}")
            database.getDao().insert(item)
          }
        }
      }
    }
  }
}