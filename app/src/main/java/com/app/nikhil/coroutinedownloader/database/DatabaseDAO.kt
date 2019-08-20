package com.app.nikhil.coroutinedownloader.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.nikhil.coroutinedownloader.models.DownloadItem

@Dao
interface DatabaseDAO {

  @Query("SELECT * FROM downloaditem")
  fun getAll(): List<DownloadItem>

  @Query("SELECT * FROM downloaditem WHERE url = :downloadUrl")
  fun getItem(downloadUrl: String): DownloadItem

  @Insert
  fun insert(downloadItem: DownloadItem)

  @Insert
  fun insertAll(downloadItemList: List<DownloadItem>)
}