package com.app.nikhil.coroutinedownloader.downloadutils

import android.content.Context
import com.app.nikhil.coroutinedownloader.utils.DownloadInfo
import com.app.nikhil.coroutinedownloader.utils.FileExistsException
import com.app.nikhil.coroutinedownloader.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSink
import okio.BufferedSource
import okio.appendingSink
import okio.buffer
import okio.sink
import java.math.RoundingMode.CEILING
import java.text.DecimalFormat
import kotlin.coroutines.CoroutineContext

class DownloaderScope(
  context: Context,
  override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {

  private val downloadMap: MutableMap<String, Pair<Job, Channel<DownloadInfo>>> = mutableMapOf()

  private val okHttpClient = OkHttpClient()
  private val fileUtils = FileUtils(context)
  private val BYTES_CONVERTER = 0.000001
  private val percentageFormat = DecimalFormat("#.##").apply { roundingMode = CEILING }

  @ExperimentalCoroutinesApi fun downloadFile(url: String) {
    val request = Request.Builder()
        .url(url)
        .build()

    val channel = Channel<DownloadInfo>()
    val job = launch {
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
              body.contentLength(), channel, file.length(), url
          )
        }
      } catch (e: Exception) {
        throw e
      }
    }
    downloadMap[url] = Pair(job, channel)
  }

  fun pauseDownload(url: String) {
    downloadMap[url]?.let { pair ->
      pair.first.cancel()
      pair.second.close()
    }
  }

  fun getChannelForURL(url: String): Channel<DownloadInfo>? {
    return downloadMap[url]?.second
  }

  /*
  * Read from a BufferedSource and write it in BufferedSink
  */
  @ExperimentalCoroutinesApi
  private fun bufferedRead(
    source: BufferedSource,
    sink: BufferedSink,
    bufferSize: Long,
    totalBytes: Long,
    channel: Channel<DownloadInfo>,
    seek: Long = 0L,
    url: String
  ) {
    var bytesRead = seek
    try {
      source.skip(seek)
      var noOfBytes = source.read(sink.buffer, bufferSize)
      while (noOfBytes != -1L) {
        bytesRead += noOfBytes
        noOfBytes = source.read(sink.buffer, bufferSize)
        publishUpdates(channel, bytesRead, totalBytes, url)
      }
      if (bytesRead != totalBytes) {
        bytesRead = source.read(sink.buffer, totalBytes - bytesRead)
        publishUpdates(channel, bytesRead, totalBytes, url)
      }
    } catch (e: Exception) {
      throw e
    } finally {
      source.close()
      sink.close()
    }
  }

  @ExperimentalCoroutinesApi
  private fun publishUpdates(
    channel: Channel<DownloadInfo>,
    bytesRead: Long,
    totalBytes: Long,
    url: String
  ) {
    if (!channel.isClosedForSend) {
      channel.sendBlocking(
          DownloadInfo(
              url = url,
              percentage = getPercentage(bytesRead, totalBytes),
              bytesDownloaded = convertBytesToMB(bytesRead),
              totalBytes = convertBytesToMB(totalBytes)
          )
      )
    }
  }

  private fun getPercentage(
    bytesRead: Long,
    totalBytes: Long
  ): String = percentageFormat.format((bytesRead.toDouble() / totalBytes.toDouble()) * 100)

  private fun convertBytesToMB(bytes: Long): String =
    percentageFormat.format(bytes * BYTES_CONVERTER)

}