package com.app.nikhil.coroutinedownloader.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import com.app.nikhil.coroutinedownloader.utils.Constants
import com.app.nikhil.coroutinedownloader.utils.Converters

@Database(
    entities = [DownloadProgress::class], version = Constants.DATABASE_VERSION, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DownloadDatabase : RoomDatabase() {
  abstract fun getDao(): DatabaseDAO
}