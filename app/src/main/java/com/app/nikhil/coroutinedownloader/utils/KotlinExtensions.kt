package com.app.nikhil.coroutinedownloader.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import java.net.URI

fun Uri.toURI(): URI = URI.create(toString())

fun ContentResolver.safeInsert(uri: Uri, contentValues: ContentValues): Uri =
  insert(uri, contentValues) ?: Uri.EMPTY