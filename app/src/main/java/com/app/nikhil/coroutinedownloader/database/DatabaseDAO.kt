package com.app.nikhil.coroutinedownloader.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.nikhil.coroutinedownloader.models.DownloadProgress

@Dao
interface DatabaseDAO {

  @Query("SELECT * FROM downloadprogress")
  fun getAll(): List<DownloadProgress>

  @Query("SELECT * FROM downloadprogress WHERE url = :downloadUrl")
  fun getItem(downloadUrl: String): DownloadProgress

  @Insert
  fun insert(downloadProgress: DownloadProgress)

  @Insert
  fun insertAll(vararg downloadProgress: DownloadProgress)
}