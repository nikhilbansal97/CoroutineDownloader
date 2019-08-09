package com.app.nikhil.coroutinedownloader.downloadutils

import com.app.nikhil.coroutinedownloader.exceptions.FileExistsException
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import com.app.nikhil.coroutinedownloader.models.DownloadState
import com.app.nikhil.coroutinedownloader.models.DownloadState.COMPLETED
import com.app.nikhil.coroutinedownloader.models.DownloadState.DOWNLOADING
import com.app.nikhil.coroutinedownloader.models.DownloadState.PAUSED
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSink
import okio.BufferedSource
import okio.appendingSink
import okio.buffer
import okio.sink
import timber.log.Timber
import java.math.RoundingMode.CEILING
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
class DownloadManager @Inject constructor(
  private val okHttpClient: OkHttpClient,
  private val fileUtils: FileUtils,
  private val downloadScope: CoroutineScope
) : Downloader {

  companion object {
    private const val MEGA_BYTES_MULTIPLIER = 0.000001
    private const val DECIMAL_PERCENT_FORMAT = "#.##"
  }

  private val percentageFormat =
    DecimalFormat(DECIMAL_PERCENT_FORMAT).apply { roundingMode = CEILING }

  private val downloadMap: MutableMap<String, Pair<Job, Channel<DownloadProgress>>> = mutableMapOf()

  // Pause the Queue when the service is destroyed.
  override fun pauseQueue() {}

  // Resume the Queue when the service is started.
  override fun resumeQueue() {}

  // Dispose the resources occupied by the downloader and cancel all coroutines.
  override fun disposeAll() {}

  override fun disposeDownload(url: String) {
    downloadMap[url]?.first?.cancel()
    downloadMap[url]?.second?.close()
  }

  override suspend fun download(url: String) {
    val request = Request.Builder()
        .url(url)
        .build()

    val channel = Channel<DownloadProgress>()
    val job = downloadScope.launch { suspendedDownload(request, url, channel) }
    downloadMap[url] = Pair(job, channel)
  }

  private suspend fun suspendedDownload(
    request: Request,
    url: String,
    channel: Channel<DownloadProgress>
  ) {
    try {
      // Create a connection and get the details about the file.
      val response = okHttpClient.newCall(request)
          .execute()
      // Get the file object for the file to be downloaded.
      val file = fileUtils.getFile(url)
      // If the body of response is not empty
      response.body?.let { body ->
        if (file.exists() && file.length() == body.contentLength()) {
          throw FileExistsException()
        }
        // Create a buffered output stream (BufferedSink) for the file.
        val fileBufferedSink: BufferedSink = when {
          file.length() != 0L -> file.appendingSink()
              .buffer()
          else -> file.sink()
              .buffer()
        }
        // Get the buffered input stream (BufferedStream) for the file.
        val networkBufferedSource = body.source()
        bufferedRead(
            networkBufferedSource, fileBufferedSink, DEFAULT_BUFFER_SIZE.toLong(),
            body.contentLength(), channel, file.length(), url, file.name
        )
      }
    } catch (e: Exception) {
      Timber.e(e)
    } finally {
      disposeDownload(url)
    }
  }

  override suspend fun pause(downloadProgress: DownloadProgress) {
    downloadMap[downloadProgress.url]?.let { pair ->
      pair.first.cancel()

      while (!pair.first.isCancelled) { }
      publishUpdates(pair.second, downloadProgress.apply { state = PAUSED })
      pair.second.close()
    }
  }

  override fun getChannel(url: String): Channel<DownloadProgress>? {
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
    channel: Channel<DownloadProgress>,
    seek: Long = 0L,
    url: String,
    fileName: String
  ) {
    var bytesRead = seek
    try {
      source.skip(seek)
      var noOfBytes = source.read(sink.buffer, bufferSize)
      while (noOfBytes != -1L && coroutineContext[Job]?.isActive == true) {
        bytesRead += noOfBytes
        publishUpdates(channel, bytesRead, totalBytes, url, fileName, DOWNLOADING)
        noOfBytes = source.read(sink.buffer, bufferSize)
      }
      if (bytesRead != totalBytes) {
        bytesRead = source.read(sink.buffer, totalBytes - bytesRead)
      }
      publishUpdates(channel, bytesRead, totalBytes, url, fileName, COMPLETED)
    } catch (e: Exception) {
      Timber.e(e)
    } finally {
      source.close()
      sink.close()
    }
  }

  private suspend fun publishUpdates(
    channel: Channel<DownloadProgress>,
    bytesRead: Long,
    totalBytes: Long,
    url: String,
    fileName: String,
    downloadState: DownloadState
  ) {
    try {
      if (!channel.isClosedForSend) {
        channel.send(
            DownloadProgress(
                url = url,
                percentage = getPercentage(bytesRead, totalBytes),
                bytesDownloaded = convertBytesToMB(bytesRead),
                totalBytes = convertBytesToMB(totalBytes),
                state = downloadState,
                fileName = fileName
            )
        )
      }
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  private suspend fun publishUpdates(
    channel: Channel<DownloadProgress>,
    downloadProgress: DownloadProgress
  ) {
    if (!channel.isClosedForSend) {
      channel.send(downloadProgress)
    }
  }

  private fun getPercentage(
    bytesRead: Long,
    totalBytes: Long
  ): String = percentageFormat.format((bytesRead.toDouble() / totalBytes.toDouble()) * 100)

  private fun convertBytesToMB(bytes: Long): String =
    percentageFormat.format(bytes * MEGA_BYTES_MULTIPLIER)

}