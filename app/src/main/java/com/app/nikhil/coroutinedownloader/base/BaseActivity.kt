package com.app.nikhil.coroutinedownloader.base

import android.content.DialogInterface
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.nikhil.coroutinedownloader.utils.Constants
import com.app.nikhil.coroutinedownloader.utils.Constants.REQUEST_CODE_EXTERNAL_PERMISSIONS
import com.app.nikhil.coroutinedownloader.utils.Downloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus

abstract class BaseActivity : AppCompatActivity() {

  abstract fun getLayoutId(): Int

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(getLayoutId())

    if (externalStoragePresent()) {
      requestStoragePermission()
    }
  }

  private fun requestStoragePermission() {
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_EXTERNAL_PERMISSIONS)
    }
  }

  private fun externalStoragePresent(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
  }

  fun showDialog(msg: String) {
    AlertDialog.Builder(this)
      .setMessage(msg)
      .show()
  }
}