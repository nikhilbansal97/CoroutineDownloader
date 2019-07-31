package com.app.nikhil.coroutinedownloader.main

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter
import com.app.nikhil.coroutinedownloader.R
import com.app.nikhil.coroutinedownloader.base.BaseActivity
import com.app.nikhil.coroutinedownloader.utils.DownloadInfo
import com.app.nikhil.coroutinedownloader.utils.DownloadItem
import com.app.nikhil.coroutinedownloader.utils.Downloader
import com.app.nikhil.coroutinedownloader.utils.FileExistsException
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.downloadButton
import kotlinx.android.synthetic.main.activity_main.downloadItemsRecycler
import kotlinx.android.synthetic.main.activity_main.textInputUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel

class MainActivity : BaseActivity() {

  override fun getLayoutId(): Int = R.layout.activity_main

  private val downloadScope = CoroutineScope(Dispatchers.IO)
  private val downloader = Downloader(downloadScope, this)
  private val downloadItemAdapter = DownloadItemRecyclerAdapter(arrayListOf(), downloader)
  private val fileUtils = FileUtils(this)

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
    val channel = Channel<DownloadInfo>()
    val downloadItem = DownloadItem(fileUtils.getFileName(url), url, channel)
    try {
      downloadItem.job = downloader.downloadFile(url, channel)
      downloadItemAdapter.addItem(downloadItem)
    } catch (e: FileExistsException) {
      showDialog(e.message)
    }
  }

  private fun initRecyclerView() {
    downloadItemsRecycler.apply {
      adapter = downloadItemAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }
  }
}
