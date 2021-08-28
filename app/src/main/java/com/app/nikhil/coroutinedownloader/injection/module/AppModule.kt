package com.app.nikhil.coroutinedownloader.injection.module

import android.content.Context
import androidx.room.Room
import com.app.nikhil.coroutinedownloader.database.DownloadDatabase
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadManager
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadManagerImpl
import com.app.nikhil.coroutinedownloader.injection.qualifier.IOScope
import com.app.nikhil.coroutinedownloader.utils.Constants
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import com.app.nikhil.coroutinedownloader.utils.NotificationUtils
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
  fun provideDatabase(context: Context): DownloadDatabase {
    return Room.databaseBuilder(context, DownloadDatabase::class.java, Constants.DATABASE_NAME).build()
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
    fileUtils: FileUtils,
    @IOScope scope: CoroutineScope
  ): DownloadManager = DownloadManagerImpl(okHttpClient, fileUtils, scope)

  @Provides
  @IOScope
  fun provideIOCoroutineScope(): CoroutineScope {
    return CoroutineScope(Dispatchers.IO)
  }
}