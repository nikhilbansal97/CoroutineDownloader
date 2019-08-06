package com.app.nikhil.coroutinedownloader.ui.main

import com.app.nikhil.coroutinedownloader.downloadutils.Downloader
import com.app.nikhil.coroutinedownloader.ui.base.BaseViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val downloader: Downloader
) : BaseViewModel() {

  fun downloadResourceFromURL(url: String) {
    try {
      downloader.download(url)
    } catch (e: Exception ) {
      throw e
    }
  }
}