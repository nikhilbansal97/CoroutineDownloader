package com.app.nikhil.coroutinedownloader.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.nikhil.coroutinedownloader.database.CentralRepository
import com.app.nikhil.coroutinedownloader.downloadutils.Downloader
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import com.app.nikhil.coroutinedownloader.ui.base.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val downloader: Downloader,
  private val centralRepository: CentralRepository
) : BaseViewModel() {

  private val _infoListLiveData = MutableLiveData<List<DownloadProgress>>()
  val progressListLiveData: LiveData<List<DownloadProgress>>
    get() = _infoListLiveData

  fun getAllDownloadInfo() {
    viewModelScope.launch {
      val infoList = async { centralRepository.getAllDownloadItemsInfo() }
      _infoListLiveData.postValue(infoList.await())
    }
  }

  fun downloadResourceFromURL(url: String) {
    try {
      viewModelScope.launch { downloader.download(url) }
    } catch (e: Exception) {
      throw e
    }
  }
}