package com.app.nikhil.coroutinedownloader.main

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.nikhil.coroutinedownloader.R
import com.app.nikhil.coroutinedownloader.base.BaseActivity
import com.app.nikhil.coroutinedownloader.downloadutils.DownloaderScope
import com.app.nikhil.coroutinedownloader.utils.DownloadItem
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter
import com.app.nikhil.coroutinedownloader.utils.FileExistsException
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.downloadButton
import kotlinx.android.synthetic.main.activity_main.downloadItemsRecycler
import kotlinx.android.synthetic.main.activity_main.textInputUrl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MainActivity : BaseActivity<MainViewModel>() {

  override fun getViewModelClass(): Class<MainViewModel> = MainViewModel::class.java

  override fun getLayoutId(): Int = R.layout.activity_main

  @Inject
  lateinit var downloader: DownloaderScope

  @Inject
  lateinit var fileUtils: FileUtils

  private lateinit var downloadItemAdapter: DownloadItemRecyclerAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initRecyclerView()
    setupListeners()
  }

  private fun setupListeners() {
    downloadButton.setOnClickListener {
      textInputUrl.editText?.text?.toString()
          ?.let { url -> downloadResourceFromURL(url) }
    }
  }

  private fun downloadResourceFromURL(url: String) {
    try {
      val downloadItem = DownloadItem(fileUtils.getFileName(url), url)
      downloader.downloadFile(url)
      downloadItemAdapter.addItem(downloadItem)
    } catch (e: FileExistsException) {
      showDialog(e.message)
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  private fun initRecyclerView() {
    downloadItemAdapter = DownloadItemRecyclerAdapter(arrayListOf(), downloader)
    downloadItemsRecycler.apply {
      adapter = downloadItemAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }
}
