package com.app.nikhil.coroutinedownloader.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.app.nikhil.coroutinedownloader.R.layout
import com.app.nikhil.coroutinedownloader.R.string
import com.app.nikhil.coroutinedownloader.downloadutils.Downloader
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import com.app.nikhil.coroutinedownloader.models.DownloadState.*
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter.DownloadItemViewHolder
import kotlinx.android.synthetic.main.layout_download_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class DownloadItemRecyclerAdapter(
  private val downloadItems: ArrayList<DownloadProgress>,
  private val downloadManager: Downloader
) : RecyclerView.Adapter<DownloadItemViewHolder>() {

  private val mainScope = CoroutineScope(Dispatchers.Main)
  private val progressMap: MutableMap<String, DownloadProgress> = mutableMapOf()

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): DownloadItemViewHolder {
    return DownloadItemViewHolder(
        LayoutInflater.from(parent.context).inflate(
            layout.layout_download_item, parent, false
        )
    )
  }

  override fun getItemCount(): Int = downloadItems.size

  override fun onBindViewHolder(
    holder: DownloadItemViewHolder,
    position: Int
  ) {
    holder.bind(downloadItems[position])
  }

  fun addItem(downloadItem: DownloadProgress) {
    downloadItems.add(downloadItem)
    progressMap[downloadItem.url] = downloadItem
    notifyDataSetChanged()
  }

  inner class DownloadItemViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
    fun bind(downloadItem: DownloadProgress) {
      item.downloadItemName.text = downloadItem.fileName
      setPauseResumeListener(downloadItem.url)
      consumeDownloadProgressChannel(downloadItem.url)
    }

    @ExperimentalCoroutinesApi
    private fun consumeDownloadProgressChannel(url: String) {
      try {
        mainScope.launch {
          downloadManager.getChannel(url)
              ?.consumeEach { updateDownloadProgress(it) } ?: Timber.d("Channel is null")
        }
      } catch (e: Exception) {
        Timber.e(e)
        downloadManager.getChannel(url)
            ?.close()
      }
    }

    private fun updateDownloadProgress(downloadProgress: DownloadProgress) {
      progressMap[downloadProgress.url] = downloadProgress
      item.downloadItemProgress.text = "${downloadProgress.percentage}%"
      item.downloadSizeStatus.text =
        "${downloadProgress.bytesDownloaded}MB / ${downloadProgress.totalBytes}MB"
      item.downloadItemState.text = downloadProgress.state.toString()
      if (downloadProgress.state == COMPLETED) {
        downloadManager.getChannel(downloadProgress.url)
            ?.close()
        item.pauseResumeButton.isEnabled = false
      }
    }

    private fun setPauseResumeListener(url: String) {
      item.pauseResumeButton.setOnClickListener {
        (it as AppCompatButton).let { button ->
          mainScope.launch {
            if (button.text.toString() == it.context.getString(string.pause)) {
              downloadManager.pause(url)
              progressMap[url]?.state = PAUSED
              button.text = it.context.getString(string.resume)
            } else {
              button.text = it.context.getString(string.pause)
              downloadManager.download(url)
              progressMap[url]?.state = DOWNLOADING
              consumeDownloadProgressChannel(url)
            }
            item.downloadItemState.text = progressMap[url]?.state.toString()
          }
        }
      }
    }
  }
}