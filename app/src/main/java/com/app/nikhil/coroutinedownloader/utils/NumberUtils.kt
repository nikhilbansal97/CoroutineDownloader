package com.app.nikhil.coroutinedownloader.utils

import java.math.RoundingMode
import java.text.DecimalFormat

object NumberUtils {

  private const val MEGA_BYTES_MULTIPLIER = 0.000001
  private const val DECIMAL_PERCENT_FORMAT = "#.##"
  private val percentageFormat =
    DecimalFormat(DECIMAL_PERCENT_FORMAT).apply {
      roundingMode =
        RoundingMode.CEILING
    }

  fun getDisplayPercentage(
    bytesRead: Long,
    totalBytes: Long
  ): String = percentageFormat.format((bytesRead.toDouble() / totalBytes.toDouble()) * 100)

  fun getPercentage(
    bytesRead: Long,
    totalBytes: Long
  ): Int = ((bytesRead.toDouble() / totalBytes.toDouble()) * 100).toInt()

  fun convertBytesToMB(bytes: Long): String =
    percentageFormat.format(bytes * MEGA_BYTES_MULTIPLIER)
}