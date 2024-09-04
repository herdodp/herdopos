package com.example.toolbar

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevice: BluetoothDevice? = null
    private var getnamatoko: String? = null
    private var getalamat: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        val view = inflater.inflate(R.layout.home_fragment, container, false)

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: run {
            Toast.makeText(activity, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show()
            return view
        }



        // Button test print
        val testButton: Button = view.findViewById(R.id.testbutton)
        testButton.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothRequestLauncher.launch(enableIntent)
            } else {
                findBluetooth()
                bluetoothDevice?.let {
                    connectPrintDevice(it)
                } ?: run {
                    Toast.makeText(activity, "Bluetooth device not found", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Scan button
        val scanButton: Button = view.findViewById(R.id.scan)
        scanButton.setOnClickListener {
            startActivity(Intent(activity, BarcodeScannerActivity::class.java))
        }





        //get data share preference
        val prefs = activity?.getSharedPreferences("My_Prefs", Context.MODE_PRIVATE)
        val prefsalamat = activity?.getSharedPreferences("alamat", Context.MODE_PRIVATE)

        getnamatoko = prefs?.getString("keynamatoko", "default")
        getalamat = prefsalamat?.getString("keyalamat", "default")





        return view
    }

    // Find Bluetooth
    @SuppressLint("MissingPermission")
    private fun findBluetooth() {
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        bluetoothDevice = pairedDevices.find { it.name == "RPP02" }
        if (bluetoothDevice == null) {
            Toast.makeText(activity, "Bluetooth device not found", Toast.LENGTH_LONG).show()
        }
    }

    // Connect to print using Bluetooth
    @SuppressLint("MissingPermission")
    private fun connectPrintDevice(device: BluetoothDevice) {
        val uuid: UUID = device.uuids.firstOrNull()?.uuid ?: return // Ensure UUID is not null
        var bluetoothSocket: BluetoothSocket? = null

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket.connect()
            val outputStream: OutputStream = bluetoothSocket.outputStream
            printReceipt(outputStream)
            outputStream.close()
            bluetoothSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                closeException.printStackTrace()
            }
        }
    }

    private fun printReceipt(outputStream: OutputStream) {
        val receipt = """
            
            
                *** ${getnamatoko} ***
            $getalamat
            ---------------------------
            Item      Qty    Price
            ---------------------------
            cashew b   2  30.000  60.000
            kimchi     5  25.000  125.000
            coklat b   3  30.000  90.000
            ---------------------------
            Total                 275.000
            ---------------------------
      
      
        """.trimIndent()
        try {
            outputStream.write(receipt.toByteArray())
            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // ActivityResultLauncher for Bluetooth enabling
    private val bluetoothRequestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            findBluetooth()
            bluetoothDevice?.let {
                connectPrintDevice(it)
            } ?: run {
                Toast.makeText(activity, "Bluetooth device not found", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(activity, "Bluetooth must be enabled", Toast.LENGTH_LONG).show()
        }
    }
}
