package com.yt8492.bitmapfilesend.send

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ImageListAdapter(
    private val listener: OnClickImageListener
) : ListAdapter<String, ImageItemViewHolder>(ItemCallback) {

    private val filePaths = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {
        return ImageItemViewHolder.create(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    fun addFiles(filePaths: List<String>) {
        this.filePaths.addAll(filePaths)
        submitList(this.filePaths)
    }

    private object ItemCallback : DiffUtil.ItemCallback<String>() {
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}