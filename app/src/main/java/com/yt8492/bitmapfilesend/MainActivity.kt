package com.yt8492.bitmapfilesend

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt8492.bitmapfilesend.databinding.ActivityMainBinding
import com.yt8492.bitmapfilesend.send.SendActivity

class MainActivity : AppCompatActivity() {

    private val deviceListAdapter = DeviceListAdapter(object : OnClickDeviceListener {
        override fun onClick(device: BluetoothDevice?) {
            device ?: return
            val intent = Intent(this@MainActivity, SendActivity::class.java).apply {
                putExtra(SendActivity.KEY_DEVICE, device)
            }
            startActivity(intent)
        }
    })

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.bondState == BluetoothDevice.BOND_BONDED) {
                        deviceListAdapter.addDevices(listOf(device))
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    bluetoothAdapter.cancelDiscovery()
                    binding.refreshLayout.isRefreshing = false
                }
            }
        }
    }
    
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.deviceListRecyclerView.apply {
            adapter = deviceListAdapter
            val linearLayoutManager = LinearLayoutManager(this@MainActivity)
            val itemDecoration = DividerItemDecoration(context, linearLayoutManager.orientation)
            addItemDecoration(itemDecoration)
            layoutManager = linearLayoutManager
        }
        binding.refreshLayout.setOnRefreshListener {
            if (!bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.startDiscovery()
            }
        }
        listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            .filterNot {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
            .toTypedArray()
            .let {
                val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
                    val isAllGranted = grantedMap.all { e ->
                        e.value
                    }
                    if (!isAllGranted) {
                        AlertDialog.Builder(this)
                            .setMessage("Bluetoothを許可してください")
                            .setPositiveButton("OK") { _, _ ->
                                finish()
                            }
                            .create()
                            .show()
                    }
                }
                launcher.launch(it)
            }
        if (bluetoothAdapter == null) {
            AlertDialog.Builder(this)
                .setMessage("Bluetoothをサポートしていません")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
                .create()
                .show()
        }
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) {
                    AlertDialog.Builder(this)
                        .setMessage("BluetoothをONにしてください")
                        .setPositiveButton("OK") { _, _ ->
                            finish()
                        }
                        .create()
                        .show()
                }
            }
            launcher.launch(enableIntent)
        }

        val foundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, foundFilter)
        val discoveryFinishedFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, discoveryFinishedFilter)

        val devices = bluetoothAdapter.bondedDevices.toList()
        deviceListAdapter.addDevices(devices)
        bluetoothAdapter.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
            binding.refreshLayout.isRefreshing = false
        }
        unregisterReceiver(receiver)
    }
}
