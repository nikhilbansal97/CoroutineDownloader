package com.app.nikhil.coroutinedownloader.injection.module

import android.content.Context
import com.app.nikhil.coroutinedownloader.downloadutils.DownloaderScope
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

  @Provides
  @Singleton
  fun provideFileUtils(context: Context): FileUtils {
    return FileUtils(context)
  }

  @Provides
  @Singleton
  fun provideDownloaderScope(context: Context): DownloaderScope {
    return DownloaderScope(context)
  }
}