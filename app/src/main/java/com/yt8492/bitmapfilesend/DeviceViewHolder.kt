package com.yt8492.bitmapfilesend

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yt8492.bitmapfilesend.databinding.ItemDeviceBinding

class DeviceViewHolder(
    private val binding: ItemDeviceBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        device: BluetoothDevice,
        onClickListener: OnClickDeviceListener
    ) {
        binding.device = device
        binding.listener = onClickListener
        binding.executePendingBindings()
    }

    companion object {
        fun create(
            inflater: LayoutInflater,
            container: ViewGroup,
            attachToRoot: Boolean
        ): DeviceViewHolder {
            return DeviceViewHolder(
                ItemDeviceBinding.inflate(
                    inflater,
                    container,
                    attachToRoot
                )
            )
        }
    }
}

interface OnClickDeviceListener {
    fun onClick(device: BluetoothDevice?)
}
