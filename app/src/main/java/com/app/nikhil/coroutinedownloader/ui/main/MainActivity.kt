package com.app.nikhil.coroutinedownloader.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.nikhil.coroutinedownloader.R
import com.app.nikhil.coroutinedownloader.databinding.ActivityMainBinding
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadService
import com.app.nikhil.coroutinedownloader.downloadutils.Downloader
import com.app.nikhil.coroutinedownloader.exceptions.FileExistsException
import com.app.nikhil.coroutinedownloader.ui.base.BaseActivity
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.downloadItemsRecycler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

  override fun getViewModelClass(): Class<MainViewModel> = MainViewModel::class.java

  override fun getLayoutId(): Int = R.layout.activity_main

  private var serviceBound = false
  private lateinit var downloadService: DownloadService
  private lateinit var downloadItemAdapter: DownloadItemRecyclerAdapter

  /*
  * Lazily initialize the ServiceConnection
  */
  private val connection: ServiceConnection by lazy {
    object : ServiceConnection {
      override fun onServiceConnected(
        componentName: ComponentName,
        ibinder: IBinder
      ) {
        downloadService = (ibinder as DownloadService.DownloadServiceBinder).getService()
        serviceBound = true
      }

      override fun onServiceDisconnected(componentName: ComponentName) {
        serviceBound = false
      }
    }
  }

  @Inject
  lateinit var downloadManager: Downloader

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initRecyclerView()
    setupListeners()
    observeLiveData()
    viewModel.getAllDownloadItems()
  }

  private fun observeLiveData() {
    viewModel.downloadItemsLiveData.observe(this, Observer {
      it?.let { items -> downloadItemAdapter.addAll(items) }
    })
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
    /*
    * Bind the service to get updates and keep the service running
    * when the app goes in background
    */
    bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
  }

  private fun setupListeners() {
    binding.downloadButton.setOnClickListener {
      binding.editTextUrl.text?.toString()
          ?.let { url -> downloadFile(url) }
    }
  }

  override fun onStart() {
    super.onStart()
    startDownloadService()
  }

  override fun onStop() {
    super.onStop()
    if (serviceBound) {
      unbindService(connection)
    }
    viewModel.saveDownloadItemsProgress(downloadItemAdapter.getDownloadProgressList())
  }

  private fun downloadFile(url: String) {
    try {
      while (!serviceBound) {
        Timber.d("Waiting for the service to bind.")
      }
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
