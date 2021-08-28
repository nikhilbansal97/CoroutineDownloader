package com.app.nikhil.coroutinedownloader.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.nikhil.coroutinedownloader.models.DownloadItem

@Dao
interface DatabaseDAO {

  @Query("SELECT * FROM DownloadItemsTable")
  suspend fun getAll(): List<DownloadItem>

  @Query("SELECT * FROM DownloadItemsTable WHERE url = :downloadUrl")
  suspend fun getItem(downloadUrl: String): DownloadItem

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(downloadItem: DownloadItem)

  @Insert
  suspend fun insertAll(downloadItemList: List<DownloadItem>)

  @Query("SELECT * FROM DownloadItemsTable")
  fun getAllItemsLive(): LiveData<List<DownloadItem>>
}