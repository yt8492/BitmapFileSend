package com.yt8492.bitmapfilesend.send

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yt8492.bitmapfilesend.databinding.ItemImageBinding

class ImageItemViewHolder(
    private val binding: ItemImageBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        fileName: String,
        onClickImageListener: OnClickImageListener
    ) {
        binding.fileName = fileName
        binding.listener = onClickImageListener
        val inputStream = binding.root.context.assets.open(fileName)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        binding.imageView.setImageBitmap(bitmap)
        binding.executePendingBindings()
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): ImageItemViewHolder {
            return ImageItemViewHolder(
                ItemImageBinding.inflate(
                    inflater,
                    container,
                    attachToRoot
                )
            )
        }
    }
}

interface OnClickImageListener {
    fun onClick(fileName: String?)
}
