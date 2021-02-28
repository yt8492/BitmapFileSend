package com.yt8492.bitmapfilesend.send

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt8492.bitmapfilesend.Constants
import com.yt8492.bitmapfilesend.R
import com.yt8492.bitmapfilesend.databinding.ActivitySendBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer

class SendActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendBinding

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val device by lazy {
        bluetoothAdapter.getRemoteDevice(address)
    }

    private val address by lazy {
        intent.getStringExtra(KEY_ADDRESS)
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
                    val socket = device.createRfcommSocketToServiceRecord(Constants.APP_UUID)
                    try {
                        if (socket.isConnected) return@withContext
                        socket.connect()
                        val outputStream = socket.outputStream
                        val logStream = socket.inputStream
                        val inputStream = assets.open(fileName)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val dataBuf = ByteArray(bitmap.width * bitmap.height * 3)
                        var i = 0
                        for (y in 0 until bitmap.height) {
                            for (x in 0 until bitmap.width) {
                                val pixel = bitmap.getPixel(x, y)
                                val r = Color.red(pixel)
                                val g = Color.green(pixel)
                                val b = Color.blue(pixel)
                                dataBuf[i] = r.toByte()
                                i++
                                dataBuf[i] = g.toByte()
                                i++
                                dataBuf[i] = b.toByte()
                                i++
                            }
                        }
                        var offset = 0
                        outputStream.write(dataBuf, offset, 32)
                        launch {
                            while (socket.isConnected) {
                                val tmp = logStream.read()
                                if (tmp != 'k'.toInt()) {
                                    continue
                                }
                                Log.d("hogehoge", "tmp: $tmp")
                                offset += 32
                                if (offset < dataBuf.size) {
                                    val len = if (offset + 32 < dataBuf.size) {
                                        32
                                    } else {
                                        dataBuf.size - offset
                                    }
                                    outputStream.write(dataBuf, offset, len)
                                    Log.d("hogehoge", "send: ${offset + len}")
                                    if (offset + len >= dataBuf.size) {
                                        break
                                    }
                                }
                            }
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
        val files = assets.list("")?.filter { it.endsWith(".bmp") || it.endsWith(".png") } ?: listOf()
        imageListAdapter.addFiles(files)
    }

    companion object {
        const val KEY_ADDRESS = "address"
    }
}