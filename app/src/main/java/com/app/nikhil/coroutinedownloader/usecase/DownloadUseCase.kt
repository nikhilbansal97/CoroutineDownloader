package com.app.nikhil.coroutinedownloader.usecase

import com.app.nikhil.coroutinedownloader.database.CentralRepository
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadManager
import com.app.nikhil.coroutinedownloader.models.DownloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadUseCase @Inject constructor(
  private val downloadManager: DownloadManager,
  private val centralRepository: CentralRepository
) : BaseSuspendUseCase<DownloadItem, String> {

  private val downloadScope = CoroutineScope(Dispatchers.IO)

  override suspend fun perform(param: String): DownloadItem {
    val item = downloadManager.download(param)
    // Launch a separate coroutine since this will start a database transaction which is
    // synchronous and hence blocking.
    downloadScope.launch { centralRepository.startSavingDownloadProgress(item) }
    return item
  }
}