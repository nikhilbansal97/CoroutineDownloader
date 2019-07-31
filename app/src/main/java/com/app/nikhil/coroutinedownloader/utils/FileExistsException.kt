package com.app.nikhil.coroutinedownloader.utils

class FileExistsException : Exception() {
  override val message: String
    get() = "File Already Exists"
}