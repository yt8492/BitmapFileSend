package com.yt8492.bitmapfilesend

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
                putExtra(SendActivity.KEY_ADDRESS, device.address)
            }
            startActivity(intent)
        }
    })

    private val bluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

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
            findDevices()
            binding.refreshLayout.isRefreshing = false
        }

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "ble not supported", Toast.LENGTH_SHORT).show()
            finish()
        }

        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
            .filterNot {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
            .toTypedArray()
            .let {
                if (it.isEmpty()) {
                    return@let
                }
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
        if (bluetoothAdapter?.isEnabled == false) {
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
        findDevices()
    }

    private fun findDevices() {
        val devices = bluetoothAdapter.bondedDevices.toList()
        Log.d("hogehgoe", "devices:\n${devices.joinToString("\n") { "name: ${it.name}, state: ${it.bondState}" }}")
        deviceListAdapter.clear()
        deviceListAdapter.addDevices(devices)
    }
}

private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
