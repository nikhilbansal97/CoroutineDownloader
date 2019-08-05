package com.app.nikhil.coroutinedownloader.base

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.nikhil.coroutinedownloader.utils.Constants.REQUEST_CODE_EXTERNAL_PERMISSIONS
import dagger.android.AndroidInjection

abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {

  abstract fun getLayoutId(): Int

  abstract fun getViewModelClass(): Class<VM>

  lateinit var viewModel: VM
  lateinit var viewModelFactory: ViewModelProvider.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(getLayoutId())

    AndroidInjection.inject(this)
    viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass())

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