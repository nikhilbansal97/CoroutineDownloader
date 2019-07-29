package com.app.nikhil.coroutinedownloader.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.nikhil.coroutinedownloader.R.layout
import com.app.nikhil.coroutinedownloader.R.string
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter.DownloadItemViewHolder
import kotlinx.android.synthetic.main.layout_download_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import timber.log.Timber

class DownloadItemRecyclerAdapter(
  private val downloadItems: ArrayList<DownloadItem>
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
      try {
        mainScope.launch {
          downloadItem.channel.consumeEach { downloadInfo ->
            Timber.d("Percentage Downloaded: ${downloadInfo.percentage}")
            item.downloadItemProgress.text = "${downloadInfo.percentage}%"
            item.downloadSizeStatus.text = "${downloadInfo.bytesDownloaded}MB /${downloadInfo.totalBytes}MB"
            if (downloadInfo.percentage.toDouble() != 100.0) {
              item.downloadItemState.text = item.context.getString(string.downloading)
            } else {
              item.downloadItemState.text = item.context.getString(string.completed)
              downloadItem.channel.close()
            }
          }
        }
      } catch (e: Exception) {
        Timber.e(e)
        downloadItem.channel.close(e)
      }
    }
  }
}