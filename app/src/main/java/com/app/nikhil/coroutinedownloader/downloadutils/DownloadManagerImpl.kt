package com.app.nikhil.coroutinedownloader.downloadutils

import android.net.Uri
import com.app.nikhil.coroutinedownloader.exceptions.FileAlreadyDownloadingException
import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import com.app.nikhil.coroutinedownloader.models.DownloadState
import com.app.nikhil.coroutinedownloader.models.DownloadState.*
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import com.app.nikhil.coroutinedownloader.utils.NumberUtils.convertBytesToMB
import com.app.nikhil.coroutinedownloader.utils.NumberUtils.getDisplayPercentage
import com.app.nikhil.coroutinedownloader.utils.NumberUtils.getPercentage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
class DownloadManagerImpl @Inject constructor(
  private val okHttpClient: OkHttpClient,
  private val fileUtils: FileUtils,
  private val downloadScope: CoroutineScope
) : DownloadManager {

  private val downloadMap: MutableMap<String, Pair<Job, BroadcastChannel<DownloadProgress>>> =
    hashMapOf()

  // Pause the Queue when the service is destroyed.
  override suspend fun pauseQueue() {}

  // Resume the Queue when the service is started.
  override suspend fun resumeQueue() {
    for (url: String in downloadMap.keys) {
      download(url)
    }
  }

  override fun onProgressChanged(
    url: String,
    function: (item: DownloadProgress) -> Unit
  ) {
    downloadScope.launch(Dispatchers.IO) {
      downloadMap[url]?.second?.consumeEach { progress ->
        function(progress)
        if (progress.percentage == 100) {
          onDownloadCompleted(progress.uri)
        }
      }
    }
  }

  private fun onDownloadCompleted(uri: String) {
    fileUtils.downloadCompleted(uri)
  }

  // Dispose the resources occupied by the downloader and cancel all coroutines.
  override fun disposeAll() {}

  override fun disposeDownload(url: String) {
    downloadMap[url]?.first?.cancel()
    downloadMap[url]?.second?.close()
  }

  override fun download(url: String): DownloadItem {
    if (alreadyDownloading(url)) {
      throw FileAlreadyDownloadingException()
    }

    // Create the request
    val request = Request.Builder()
      .url(url)
      .build()

    // Create a DownloadItem
    val downloadItem =
      DownloadItem(url, fileUtils.getFileName(url)).apply {
        channel = ConflatedBroadcastChannel()
      }

    try {
      /*
      * Launch a coroutine that will start the download and post the updates
      * to the channel of the DownloadItem for this Url
      */
      val job = downloadScope.launch { suspendedDownload(request, url, downloadItem.channel) }
      // Create an entry in the in-memory map
      downloadMap[url] = Pair(job, downloadItem.channel)
    } catch (e: Exception) {
      throw e
    }
    return downloadItem
  }

  private suspend fun suspendedDownload(
    request: Request,
    url: String,
    channel: BroadcastChannel<DownloadProgress>
  ) {
    try {
      // Create a connection and get the details about the file.
      val response = okHttpClient.newCall(request)
        .execute()
      // Get the file object for the file to be downloaded.
      val uri = fileUtils.getFileUri(url, response.header("Content-Type"))
      if (uri?.toString().isNullOrEmpty()) {
        Timber.e("uri is null, item was not added to MediaStore!")
        return
      }
      // If the body of response is not empty
      response.body?.let { body ->
        val file: File? = getFileIfExists(uri)
        // Create a buffered output stream (BufferedSink) for the file.
        val fileBufferedSink: BufferedSink? = when {
          file != null -> {
            // File exists
            return withContext(Dispatchers.IO) {
              when {
                file.length() != 0L -> file.appendingSink().buffer()
                else -> file.sink().buffer()
              }
            }
          }
          else -> fileUtils.getNewBufferedSink(uri!!)
        }
        // Get the buffered input stream (BufferedStream) for the file.
        if (fileBufferedSink == null) {
          Timber.e("Unable to write to file!")
          return
        }
        val networkBufferedSource = body.source()
        bufferedRead(
          networkBufferedSource, fileBufferedSink, DEFAULT_BUFFER_SIZE.toLong(),
          body.contentLength(), channel, file?.length() ?: 0L, uri
        )
      }
    } catch (e: Exception) {
      Timber.e(e)
    } finally {
      disposeDownload(url)
    }
  }

  private fun getFileIfExists(uri: Uri?): File? {
    var file: File? = null
    try {
      file = File(uri.toString())
      file.sink().close()
    } catch (e: Exception) {
      // The file doesn't exist
      file = null
    }
    return file
  }

  private fun alreadyDownloading(url: String): Boolean {
    return downloadMap[url]?.let { !it.first.isCancelled } ?: false
  }

  override suspend fun pause(downloadItem: DownloadItem) {
    downloadMap[downloadItem.url]?.let { pair ->
      pair.first.cancel()
      while (!pair.first.isCancelled) { /* Wait for the job to be cancelled. */
      }
      publishUpdates(pair.second, downloadItem.downloadProgress.apply { this.state = PAUSED })
      pair.second.close()
    }
  }

  override fun getChannel(url: String): BroadcastChannel<DownloadProgress>? {
    return downloadMap[url]?.second
  }

  /*
  * Read from a BufferedSource and write it in BufferedSink
  */
  private suspend fun bufferedRead(
    source: BufferedSource,
    sink: BufferedSink,
    bufferSize: Long,
    totalBytes: Long,
    channel: BroadcastChannel<DownloadProgress>,
    seek: Long = 0L,
    uri: Uri
  ) {
    var bytesRead = seek
    try {
      // Skip the no of bytes already downloaded
      source.skip(seek)
      var noOfBytes = source.read(sink.buffer, bufferSize)
      while (noOfBytes != -1L && coroutineContext[Job]?.isActive == true) {
        bytesRead += noOfBytes
        publishUpdates(channel, bytesRead, totalBytes, DOWNLOADING, uri)
        noOfBytes = source.read(sink.buffer, bufferSize)
      }
      if (bytesRead != totalBytes) {
        bytesRead = source.read(sink.buffer, totalBytes - bytesRead)
      }
      publishUpdates(channel, bytesRead, totalBytes, COMPLETED, uri)
    } catch (e: Exception) {
      Timber.e(e)
    } finally {
      source.close()
      sink.close()
    }
  }

  private suspend fun publishUpdates(
    channel: BroadcastChannel<DownloadProgress>,
    bytesRead: Long,
    totalBytes: Long,
    downloadState: DownloadState,
    uri: Uri
  ) {
    try {
      if (!channel.isClosedForSend) {
        channel.send(
          DownloadProgress(
            megaBytesDownloaded = convertBytesToMB(bytesRead),
            percentageDisplay = getDisplayPercentage(bytesRead, totalBytes),
            percentage = getPercentage(bytesRead, totalBytes),
            totalMegaBytes = convertBytesToMB(totalBytes),
            bytesDownloaded = bytesRead,
            totalBytes = totalBytes,
            state = downloadState,
            uri = uri.toString()
          )
        )
      }
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  private suspend fun publishUpdates(
    channel: BroadcastChannel<DownloadProgress>,
    downloadProgress: DownloadProgress
  ) {
    if (!channel.isClosedForSend) {
      channel.send(downloadProgress)
    }
  }
}