package com.app.nikhil.coroutinedownloader.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.nikhil.coroutinedownloader.database.CentralRepository
import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.usecase.DownloadUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val centralRepository: CentralRepository,
  private val downloadUseCase: DownloadUseCase
) : ViewModel() {

  private val _downloadItemsListLiveData = MutableLiveData<List<DownloadItem>>()
  val downloadItemsLiveData: LiveData<List<DownloadItem>>
    get() = _downloadItemsListLiveData

  private val _downloadItemLiveData = MutableLiveData<DownloadItem>()
  val downloadItemLiveData: LiveData<DownloadItem>
    get() = _downloadItemLiveData

  private val _exceptionLiveData = MutableLiveData<Exception>()
  val exceptionLiveData: LiveData<Exception>
    get() = _exceptionLiveData

  fun getAllDownloadItems() {
    viewModelScope.launch {
      val downloadItemsList = centralRepository.getAllDownloadItems()
      _downloadItemsListLiveData.postValue(downloadItemsList)
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

  fun download(url: String) {
    viewModelScope.launch {
      try {
        val item = downloadUseCase.perform(url)
        Timber.d("[CDM] Received the download item. Sending to UI")
        _downloadItemLiveData.postValue(item)
      } catch (e: Exception) {
        _exceptionLiveData.postValue(e)
      }
    }
  }
}