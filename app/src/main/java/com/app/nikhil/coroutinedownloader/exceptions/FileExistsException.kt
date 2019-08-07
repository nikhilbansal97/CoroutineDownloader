package com.app.nikhil.coroutinedownloader.exceptions

class FileExistsException : Exception() {
  override val message: String
    get() = "File Already Exists"
}