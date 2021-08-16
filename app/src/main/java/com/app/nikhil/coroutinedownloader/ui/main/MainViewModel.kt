package com.app.nikhil.coroutinedownloader.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nikhil.coroutinedownloader.database.CentralRepository
import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val centralRepository: CentralRepository,
  private val fileUtils: FileUtils
) : ViewModel() {

  private val _infoListLiveData = MutableLiveData<List<DownloadItem>>()
  val downloadItemsLiveData: LiveData<List<DownloadItem>>
    get() = _infoListLiveData

  fun getAllDownloadItems() {
    viewModelScope.launch {
      val infoList = centralRepository.getAllDownloadItems()
      infoList.forEach {
        it.fileSize = fileUtils.getFileSize(it.fileName)
        it.downloadProgress.totalBytes = it.fileSize.toString()
      }
      _infoListLiveData.postValue(infoList)
    }
  }

  fun saveDownloadItemsProgress(downloadItemList: List<DownloadItem>) {
    viewModelScope.launch {
      try {
        centralRepository.saveAllDownloadItems(downloadItemList)
      } catch (e: Exception) {
        Timber.e(e)
      }
    }
  }

  fun saveDownloadItem(item: DownloadItem) {
    viewModelScope.launch {
      centralRepository.saveDownloadItem(item)
    }
  }
}