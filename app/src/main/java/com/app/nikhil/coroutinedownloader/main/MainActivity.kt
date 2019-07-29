package com.app.nikhil.coroutinedownloader.main

import android.os.Bundle
import com.app.nikhil.coroutinedownloader.R
import com.app.nikhil.coroutinedownloader.base.BaseActivity
import com.app.nikhil.coroutinedownloader.utils.Downloader
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.downloadButton
import kotlinx.android.synthetic.main.activity_main.textInputUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import java.io.File

class MainActivity : BaseActivity() {

  override fun getLayoutId(): Int = R.layout.activity_main

  private val downloader = Downloader(CoroutineScope(Dispatchers.IO), this)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    downloadButton.setOnClickListener {
      textInputUrl.editText?.text?.toString()
          ?.let { url -> downloader.downloadFile(url) }
    }
  }
}
