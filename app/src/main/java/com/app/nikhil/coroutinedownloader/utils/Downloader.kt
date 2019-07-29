package com.app.nikhil.coroutinedownloader.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.*
import timber.log.Timber

class Downloader(scope: CoroutineScope, context: Context) : CoroutineScope by scope {

    private val okHttpClient = OkHttpClient()
    private val fileUtils = FileUtils(context)
    private val BUFFER_SIZE = 20L

    fun downloadFile(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        launch {
            try {
                // Create a connection and get the details about the file.
                val response = okHttpClient.newCall(request).execute()
                // Get the file object for the file to be downloaded.
                val file = fileUtils.getFile(url)

                // If the body of response is not empty
                response.body?.let { body ->
                    if (!file.exists() || file.length() == 0L) {
                        // The file needs to be downloaded from starting.
                        // Create a buffered output stream (BufferedSink) for the file.
                        val fileBufferedSink = file.sink().buffer()
                        // Get the buffered input stream (BufferedStream) for the file.
                        val networkBufferedSource = body.source()
                        // Read from the network and write in file.
                        bufferedRead(networkBufferedSource, fileBufferedSink, BUFFER_SIZE, body.contentLength())
                    } else {
                        Timber.d("File Exists")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    /*
    * Read from a BufferedSource and write it in BufferedSink
    */
    private fun bufferedRead(source: BufferedSource, sink: BufferedSink, bufferSize: Long, totalBytes: Long) {
        val buffer = Buffer()
        var bytesRead = 0L
        try {
            var noOfBytes = source.read(sink.buffer, bufferSize)
            while (noOfBytes != -1L) {
                bytesRead += noOfBytes
                noOfBytes = source.read(sink.buffer, bufferSize)
            }
            if (bytesRead != totalBytes) {
                source.read(sink.buffer, totalBytes - bytesRead)
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            source.close()
            sink.close()
            buffer.close()
        }
    }
}