package com.app.nikhil.coroutinedownloader.injection.module

import android.content.Context
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadManager
import com.app.nikhil.coroutinedownloader.downloadutils.Downloader
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import com.app.nikhil.coroutinedownloader.utils.NotificationUtils
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
class AppModule {

  @Provides
  @Singleton
  fun provideFileUtils(context: Context): FileUtils = FileUtils(context)

  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

  @Provides
  @Singleton
  fun provideDownloadRecyclerAdapter(downloader: Downloader): DownloadItemRecyclerAdapter {
    return DownloadItemRecyclerAdapter(arrayListOf(), downloader)
  }

  @Provides
  @Singleton
  fun provideNotificationUtils(context: Context): NotificationUtils {
    return NotificationUtils(context)
  }

  @Provides
  @Singleton
  fun provideDownloader(
    okHttpClient: OkHttpClient,
    fileUtils: FileUtils
  ): Downloader = DownloadManager(okHttpClient, fileUtils)
}