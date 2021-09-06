package com.app.nikhil.coroutinedownloader.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import okio.BufferedSink
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.File
import java.net.URI

class FileUtils(private val context: Context) {

  companion object {
    private const val MIME_TYPE_VIDEO = "video"
    private const val MIME_TYPE_AUDIO = "audio"
    private const val MIME_TYPE_IMAGE = "image"
  }

  private val contentResolver: ContentResolver by lazy { context.contentResolver }

  fun getFilePath(url: String): String {
    val fileName = getFileName(url)
    val fileExtension = getFileExtension(url)
    val externalDir = context.getExternalFilesDir(fileExtension)
    return externalDir?.path + fileName
  }

  fun getFileUri(url: String, mimeType: String?): Uri? {
    return insertIntoAppropriateMediaStore(url, mimeType)
  }

  @SuppressLint("InlinedApi")
  private fun insertIntoAppropriateMediaStore(url: String, mimeType: String?): Uri? {
    return when {
      mimeType == null -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
          val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, getFileName(url))
            put(MediaStore.Downloads.IS_PENDING, 1)
          }
          contentResolver.safeInsert(collectionUri, contentValues)
        } else {
          val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
          val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, getFileName(url))
            put(MediaStore.Files.FileColumns.IS_PENDING, 1)
          }
          contentResolver.safeInsert(collectionUri, contentValues)
        }
      }
      mimeType.contains(MIME_TYPE_VIDEO) -> {
        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
          put(MediaStore.Video.Media.DISPLAY_NAME, getFileName(url))
          put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val cursor = contentResolver.query(
          Uri.parse("$collectionUri"),
          arrayOf(MediaStore.Video.VideoColumns.DISPLAY_NAME),
          null,
          null
        )
        Timber.d("Got cursor $cursor")
        cursor?.let { cursor ->
          val nameIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)
          Timber.d("Cursor has nameIndex $nameIndex")
          while (cursor.moveToNext()) {
            Timber.d("Cursor has next()")
            val name = cursor.getString(nameIndex)
            Timber.d("Got $name in MediaStore!")
          }
          Timber.d("Cursor access completed")
        }
        contentResolver.insert(collectionUri, contentValues)
      }
      mimeType.contains(MIME_TYPE_IMAGE) -> {
        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
          put(MediaStore.Images.Media.DISPLAY_NAME, getFileName(url))
          put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        contentResolver.safeInsert(collectionUri, contentValues)
      }
      mimeType.contains(MIME_TYPE_AUDIO) -> {
        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
          put(MediaStore.Audio.Media.DISPLAY_NAME, getFileName(url))
          put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        contentResolver.safeInsert(collectionUri, contentValues)
      }
      else -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
          val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, getFileName(url))
            put(MediaStore.Downloads.IS_PENDING, 1)
          }
          contentResolver.safeInsert(collectionUri, contentValues)
        } else {
          val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
          val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, getFileName(url))
            put(MediaStore.Files.FileColumns.IS_PENDING, 1)
          }
          contentResolver.safeInsert(collectionUri, contentValues)
        }
      }
    }
  }

  fun getFileName(url: String): String {
    val uri = URI.create(url)
    val path = uri.path
    val index = path.indexOfLast { it == '/' }
    if (index != -1) {
      return path.substring(index + 1)
    }
    return "Noname"
  }

  private fun getFileExtension(url: String): String {
    val fileName = getFileName(url)
    val index = fileName.indexOfLast { it == '.' }
    if (index != -1) {
      return fileName.substring(index + 1)
    }
    return ""
  }

  fun getFileSize(fileName: String): Long {
    val file = File(context.getExternalFilesDir(null), fileName)
    return if (file.exists()) file.length() else 0
  }

  fun getNewBufferedSink(uri: Uri): BufferedSink? {
    return contentResolver.openOutputStream(uri)?.sink()?.buffer()
  }

  fun downloadCompleted(uri: String) {
    val finalUri = Uri.parse(uri) ?: return
    val contentValues: ContentValues = createDownloadCompleteContentValues(finalUri)
    contentResolver.update(finalUri, contentValues, null, null)
  }

  @SuppressLint("InlinedApi")
  private fun createDownloadCompleteContentValues(uri: Uri): ContentValues {
    return ContentValues().apply {
      put(MediaStore.Video.Media.IS_PENDING, 0)
    }
  }
}