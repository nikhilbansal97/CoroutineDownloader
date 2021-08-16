package com.app.nikhil.coroutinedownloader.ui.base

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.nikhil.coroutinedownloader.utils.Constants.REQUEST_CODE_EXTERNAL_PERMISSIONS
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity<VM : ViewModel, B: ViewDataBinding> : DaggerAppCompatActivity() {

  abstract fun getLayoutId(): Int

  abstract fun getViewModelClass(): Class<VM>

  lateinit var binding: B
  lateinit var viewModel: VM
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)
    initUI()
    if (externalStoragePresent()) {
      requestStoragePermission()
    }
  }

  private fun initUI() {
    viewModel = ViewModelProvider(this, viewModelFactory).get(getViewModelClass())
    binding = DataBindingUtil.setContentView(this, getLayoutId())
    binding.setVariable(BR.viewModel, viewModel)
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