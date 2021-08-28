package com.app.nikhil.coroutinedownloader.ui.main

import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.nikhil.coroutinedownloader.R
import com.app.nikhil.coroutinedownloader.databinding.ActivityMainBinding
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadManager
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadService
import com.app.nikhil.coroutinedownloader.exceptions.FileExistsException
import com.app.nikhil.coroutinedownloader.ui.base.BaseActivity
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

  override fun getViewModelClass(): Class<MainViewModel> = MainViewModel::class.java

  override fun getLayoutId(): Int = R.layout.activity_main

  private lateinit var downloadService: DownloadService
  private lateinit var downloadItemAdapter: DownloadItemRecyclerAdapter

  @Inject
  lateinit var downloadManager: DownloadManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initRecyclerView()
    setupListeners()
    observeLiveData()
    viewModel.getAllDownloadItems()
  }

  private fun observeLiveData() {
    viewModel.downloadItemsLiveData.observe(this, {
      it?.let { items -> downloadItemAdapter.addAll(items) }
    })

    viewModel.downloadItemLiveData.observe(this) {
      it?.let { item -> downloadItemAdapter.addItem(item) }
    }

    viewModel.exceptionLiveData.observe(this) {
      it?.message?.let { message -> showMessage(message) }
    }
  }

  /*
  * Start the download service when the app starts.
  */
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
      binding.editTextUrl.text?.toString()?.let { url -> viewModel.download(url) }
    }
  }

  private fun downloadFile(url: String) {
    try {
      val item = downloadService.download(url)
      downloadItemAdapter.addItem(item)
      viewModel.saveDownloadItem(item)
    } catch (e: FileExistsException) {
      showDialog(e.message)
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  private fun initRecyclerView() {
    downloadItemAdapter = DownloadItemRecyclerAdapter(arrayListOf(), downloadManager)
    downloadItemsRecycler.apply {
      adapter = downloadItemAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }
}
