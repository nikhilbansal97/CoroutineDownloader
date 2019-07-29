package com.app.nikhil.coroutinedownloader.utils

import android.content.Context
import java.io.File
import java.net.URI

class FileUtils(private val context: Context) {

    fun getFilePath(url: String): String {
        val fileName = getFileName(url)
        val fileExtension = getFileExtension(url)
        val externalDir = context.getExternalFilesDir(fileExtension)
        return externalDir?.path + fileName
    }

    fun getFile(url: String): File = File(context.getExternalFilesDir(null), getFileName(url))

    private fun getFileName(url: String): String {
        val uri = URI.create(url)
        val path = uri.path
        val index = path.indexOfLast { it == '/' }
        if (index != -1) {
            return path.substring(index + 1)
        }
        return ""
    }

    private fun getFileExtension(url: String): String {
        val fileName = getFileName(url)
        val index = fileName.indexOfLast { it == '.' }
        if (index != -1) {
            return fileName.substring(index + 1)
        }
        return ""
    }
}