package com.yt8492.bitmapfilesend.send

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt8492.bitmapfilesend.Constants
import com.yt8492.bitmapfilesend.R
import com.yt8492.bitmapfilesend.databinding.ActivitySendBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer

class SendActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendBinding

    private val device by lazy {
        intent.getParcelableExtra<BluetoothDevice>(KEY_DEVICE)
    }

    private val sendingDialog by lazy {
        AlertDialog.Builder(this)
            .setMessage("通信中...")
            .create()
    }

    private val errorDialog by lazy {
        AlertDialog.Builder(this@SendActivity)
            .setTitle("通信に失敗しました")
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private val imageListAdapter = ImageListAdapter(object : OnClickImageListener {
        override fun onClick(fileName: String?) {
            fileName ?: return
            val device = device ?: run {
                errorDialog.show()
                return
            }
            lifecycleScope.launchWhenStarted {
                sendingDialog.show()
                withContext(Dispatchers.IO) {
                    device.createRfcommSocketToServiceRecord(Constants.APP_UUID).use { socket ->
                        try {
                            if (socket.isConnected) return@use
                            socket.connect()
                            val outputStream = socket.outputStream
                            val inputStream = assets.open(fileName)
                            val size = inputStream.available()
                            outputStream.write(ByteBuffer.allocate(4).putInt(size).array())
                            for (i in 0 until size) {
                                outputStream.write(inputStream.read())
                            }
                        } catch (e: IOException) {
                            withContext(Dispatchers.Main.immediate) {
                                AlertDialog.Builder(this@SendActivity)
                                    .setTitle("エラー")
                                    .setMessage(e.message)
                                    .setPositiveButton("OK") { d, _ ->
                                        d.dismiss()
                                    }
                                    .create()
                                    .show()
                            }
                        }
                    }
                }
                sendingDialog.dismiss()
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_send)
        binding.lifecycleOwner = this
        binding.imageRecyclerView.apply {
            adapter = imageListAdapter
            val linearLayoutManager = LinearLayoutManager(this@SendActivity)
            val itemDecoration = DividerItemDecoration(this@SendActivity, linearLayoutManager.orientation)
            addItemDecoration(itemDecoration)
        }
        val files = assets.list("")?.filter { it.endsWith(".bmp") } ?: listOf()
        imageListAdapter.addFiles(files)
    }

    companion object {
        const val KEY_DEVICE = "device"
    }
}