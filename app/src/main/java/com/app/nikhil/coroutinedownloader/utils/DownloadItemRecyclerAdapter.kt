package com.app.nikhil.coroutinedownloader.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.app.nikhil.coroutinedownloader.R.layout
import com.app.nikhil.coroutinedownloader.R.string
import com.app.nikhil.coroutinedownloader.downloadutils.DownloadManager
import com.app.nikhil.coroutinedownloader.models.DownloadItem
import com.app.nikhil.coroutinedownloader.models.DownloadProgress
import com.app.nikhil.coroutinedownloader.models.DownloadState.COMPLETED
import com.app.nikhil.coroutinedownloader.models.DownloadState.PAUSED
import com.app.nikhil.coroutinedownloader.utils.DownloadItemRecyclerAdapter.DownloadItemViewHolder
import kotlinx.android.synthetic.main.layout_download_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadItemRecyclerAdapter(
  private val downloadItems: ArrayList<DownloadItem>,
  private val downloadManager: DownloadManager
) : RecyclerView.Adapter<DownloadItemViewHolder>() {

  private val mainScope = CoroutineScope(Dispatchers.Main)
  private val itemMap: MutableMap<String, DownloadItem> = mutableMapOf()

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

  fun getDownloadProgressList(): List<DownloadItem> {
    return itemMap.values.toList()
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
    itemMap[downloadItem.url] = downloadItem
    notifyDataSetChanged()
  }

  fun addAll(items: List<DownloadItem>) {
    downloadItems.addAll(items)
    for (item: DownloadItem in items) {
      itemMap[item.url] = item
    }
    notifyDataSetChanged()
  }

  inner class DownloadItemViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
    fun bind(downloadItem: DownloadItem) {
      item.downloadItemName.text = downloadItem.fileName
      setData(downloadItem.downloadProgress)
      setPauseResumeListener(downloadItem.url)
      consumeDownloadProgressChannel(downloadItem.url)
    }

    @SuppressLint("SetTextI18n")
    private fun setData(downloadProgress: DownloadProgress) {
      with(downloadProgress) {
        item.downloadItemProgress.text = "$percentageDisplay%"
        item.downloadSizeStatus.text =
          "${megaBytesDownloaded}MB / ${totalMegaBytes}MB"
        item.downloadItemState.text = state.toString()
        item.pauseResumeButton.text = when (state) {
          PAUSED -> item.context.getText(string.resume)
          else -> item.context.getText(string.pause)
        }
      }
    }

    private fun consumeDownloadProgressChannel(url: String) {
      downloadManager.onProgressChanged(url) { updateDownloadProgress(url, it) }
    }

    @UiThread
    private fun updateDownloadProgress(
      url: String,
      progress: DownloadProgress
    ) {
      mainScope.launch {
        itemMap[url]?.downloadProgress = progress
        setData(progress)
        if (progress.state == COMPLETED) {
          item.pauseResumeButton.isEnabled = false
        }
      }
    }

    private fun setPauseResumeListener(url: String) {
      item.pauseResumeButton.setOnClickListener {
        (it as AppCompatButton).let { button ->
          mainScope.launch {
            if (button.text.toString() == it.context.getString(string.pause) && itemMap[url] != null) {
              downloadManager.pause(itemMap[url]!!)
              button.text = it.context.getString(string.resume)
            } else {
              button.text = it.context.getString(string.pause)
              downloadManager.download(url)
              consumeDownloadProgressChannel(url)
            }
            item.downloadItemState.text = itemMap[url]?.downloadProgress!!.state.toString()
          }
        }
      }
    }
  }
}