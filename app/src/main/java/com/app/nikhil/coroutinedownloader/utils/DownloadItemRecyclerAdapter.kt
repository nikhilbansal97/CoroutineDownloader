package com.app.nikhil.coroutinedownloader.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.app.nikhil.coroutinedownloader.R.layout
import com.app.nikhil.coroutinedownloader.R.string
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter.DownloadItemViewHolder
import kotlinx.android.synthetic.main.layout_download_item.view.downloadItemName
import kotlinx.android.synthetic.main.layout_download_item.view.downloadItemProgress
import kotlinx.android.synthetic.main.layout_download_item.view.downloadItemState
import kotlinx.android.synthetic.main.layout_download_item.view.downloadSizeStatus
import kotlinx.android.synthetic.main.layout_download_item.view.pauseResumeButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class DownloadItemRecyclerAdapter(
  private val downloadItems: ArrayList<DownloadItem>,
  private val downloader: Downloader
) : RecyclerView.Adapter<DownloadItemViewHolder>() {

  private val mainScope = CoroutineScope(Dispatchers.Main)

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

  fun addItem(downloadItem: DownloadItem) {
    downloadItems.add(downloadItem)
    notifyDataSetChanged()
  }

  inner class DownloadItemViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
    fun bind(downloadItem: DownloadItem) {
      item.downloadItemName.text = downloadItem.fileName
      setPauseResumeListener(downloadItem.url)
      consumeDownloadProgressChannel(downloadItem.url)
    }

    @ExperimentalCoroutinesApi
    private fun consumeDownloadProgressChannel(url: String) {
      try {
        mainScope.launch {
          downloader.getChannelForURL(url)
              ?.consumeEach { downloadInfo ->
                item.downloadItemProgress.text = "${downloadInfo.percentage}%"
                item.downloadSizeStatus.text =
                  "${downloadInfo.bytesDownloaded}MB /${downloadInfo.totalBytes}MB"
                if (downloadInfo.percentage.toDouble() != 100.0) {
                  item.downloadItemState.text = item.context.getString(string.downloading)
                } else {
                  item.downloadItemState.text = item.context.getString(string.completed)
                  downloader.getChannelForURL(url)
                      ?.close()
                  item.pauseResumeButton.isEnabled = false
                }
              }
        }
      } catch (e: Exception) {
        Timber.e(e)
        downloader.getChannelForURL(url)
            ?.close()
      }
    }

    private fun setPauseResumeListener(url: String) {
      item.pauseResumeButton.setOnClickListener {
        (it as AppCompatButton).let { button ->
          if (button.text.toString() == it.context.getString(string.pause)) {
            downloader.pauseDownload(url)
            button.text = it.context.getString(string.resume)
          } else {
            button.text = it.context.getString(string.pause)
            downloader.resumeDownload(url)
            consumeDownloadProgressChannel(url)
          }
        }
      }
    }
  }
}