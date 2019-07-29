package com.app.nikhil.coroutinedownloader

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    initTimber()
  }

  private fun initTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    }
  }
}