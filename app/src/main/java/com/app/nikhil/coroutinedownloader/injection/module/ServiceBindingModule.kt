package com.app.nikhil.coroutinedownloader.injection.module

import com.app.nikhil.coroutinedownloader.downloadutils.DownloadService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {

  @ContributesAndroidInjector
  abstract fun bindDownloadService(): DownloadService
}