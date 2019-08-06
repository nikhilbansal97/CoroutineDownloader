package com.app.nikhil.coroutinedownloader.ui.main

import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.nikhil.coroutinedownloader.R
import com.app.nikhil.coroutinedownloader.databinding.ActivityMainBinding
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadService
import com.app.nikhil.coroutinedownloader.entity.DownloadItem
import com.app.nikhil.coroutinedownloader.ui.base.BaseActivity
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter
import com.app.nikhil.coroutinedownloader.utils.FileExistsException
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.downloadItemsRecycler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

  override fun getViewModelClass(): Class<MainViewModel> = MainViewModel::class.java

  override fun getLayoutId(): Int = R.layout.activity_main

  @Inject
  lateinit var downloadItemAdapter: DownloadItemRecyclerAdapter
  @Inject
  lateinit var fileUtils: FileUtils

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initRecyclerView()
    setupListeners()
    startDownloadService()
  }

  private fun startDownloadService() {
    val serviceIntent = Intent(this, DownloadService::class.java)
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      startForegroundService(serviceIntent)
    } else {
      startService(serviceIntent)
    }
  }

  private fun setupListeners() {
    binding.downloadButton.setOnClickListener {
      binding.editTextUrl.text?.toString()
          ?.let { url -> downloadFile(url) }
    }
  }

  private fun downloadFile(url: String) {
    val downloadItem = DownloadItem(fileUtils.getFileName(url), url)
    try {
      viewModel.downloadResourceFromURL(url)
      downloadItemAdapter.addItem(downloadItem)
    } catch (e: FileExistsException) {
      showDialog(e.message)
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  private fun initRecyclerView() {
    downloadItemsRecycler.apply {
      adapter = downloadItemAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }
}
