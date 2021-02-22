package com.yt8492.bitmapfilesend

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class DeviceListAdapter(
    private val listener: OnClickDeviceListener
) : ListAdapter<BluetoothDevice, DeviceViewHolder>(ItemCallBack) {

    private val devices = mutableListOf<BluetoothDevice>()

    private object ItemCallBack : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder.create(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    fun addDevices(devices: List<BluetoothDevice>) {
        this.devices.addAll(devices)
        submitList(this.devices)
    }
}
