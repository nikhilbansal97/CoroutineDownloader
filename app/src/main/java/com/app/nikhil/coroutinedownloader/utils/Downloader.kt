package com.app.nikhil.coroutinedownloader.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch
import okhttp3.*
import okio.*
import timber.log.Timber
import java.io.BufferedInputStream
import java.math.RoundingMode.CEILING
import java.nio.file.FileAlreadyExistsException
import java.text.DecimalFormat

class Downloader(
  scope: CoroutineScope,
  context: Context
) : CoroutineScope by scope {

  private val okHttpClient = OkHttpClient()
  private val fileUtils = FileUtils(context)
  private val BYTES_CONVERTER = 0.000001
  private val percentageFormat = DecimalFormat("#.##").apply { roundingMode = CEILING }

  fun downloadFile(
    url: String,
    channel: Channel<DownloadInfo>
  ): Job {
    val request = Request.Builder()
        .url(url)
        .build()

    return launch {
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
          val fileBufferedSink: BufferedSink = if (file.length() != 0L) {
            file.appendingSink()
                .buffer()
          } else {
            file.sink().buffer()
          }
          // Get the buffered input stream (BufferedStream) for the file.
          val networkBufferedSource = body.source()
          bufferedRead(
            networkBufferedSource, fileBufferedSink, DEFAULT_BUFFER_SIZE.toLong(), body.contentLength(), channel, file.length()
          )
        }
      } catch (e: Exception) {
        throw e
      }
    }
  }

  /*
  * Read from a BufferedSource and write it in BufferedSink
  */
  private fun bufferedRead(
    source: BufferedSource,
    sink: BufferedSink,
    bufferSize: Long,
    totalBytes: Long,
    channel: Channel<DownloadInfo>,
    seek: Long = 0L
  ) {
    var bytesRead = seek
    try {
      source.skip(seek)
      var noOfBytes = source.read(sink.buffer, bufferSize)
      while (noOfBytes != -1L) {
        bytesRead += noOfBytes
        channel.sendBlocking(
            DownloadInfo(
                percentage = getPercentage(bytesRead, totalBytes),
                bytesDownloaded = convertBytesToMB(bytesRead),
                totalBytes = convertBytesToMB(totalBytes)
            )
        )
        noOfBytes = source.read(sink.buffer, bufferSize)
      }
      if (bytesRead != totalBytes) {
        bytesRead = source.read(sink.buffer, totalBytes - bytesRead)
        channel.sendBlocking(
            DownloadInfo(
                percentage = getPercentage(bytesRead, totalBytes),
                bytesDownloaded = convertBytesToMB(bytesRead),
                totalBytes = convertBytesToMB(totalBytes)
            )
        )
      }
    } catch (e: Exception) {
      throw e
    } finally {
      source.close()
      sink.close()
    }
  }

  private fun getPercentage(
    bytesRead: Long,
    totalBytes: Long
  ): String = percentageFormat.format((bytesRead.toDouble() / totalBytes.toDouble()) * 100)

  private fun convertBytesToMB(bytes: Long): String = percentageFormat.format(bytes * BYTES_CONVERTER)
}