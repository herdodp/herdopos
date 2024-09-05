package com.example.toolbar

import DatabaseHelper
import ScanResultAdapter
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
//import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevice: BluetoothDevice? = null
    private var namatoko: String? = null
    private lateinit var barcodeView: DecoratedBarcodeView

    private var getnamatoko: String? = null
    private var getalamat: String? = null

    private val PERMISSION_REQUEST_CODE = 1

    private lateinit var adapter: ScanResultAdapter
    private val scanResults = mutableListOf<ScanResult>()

    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handle edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //get data share preference
        val prefs = getSharedPreferences("My_Prefs", Context.MODE_PRIVATE)
        val prefsalamat = getSharedPreferences("alamat", Context.MODE_PRIVATE)

        getnamatoko = prefs?.getString("keynamatoko", "default")
        getalamat = prefsalamat?.getString("keyalamat", "default")


        recyclerView = findViewById(R.id.recyclerviewmain) // Pastikan ID ini sesuai dengan ID di layout XML Anda
        adapter = ScanResultAdapter(scanResults)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)



        // Barcode Scanner
        barcodeView = findViewById(R.id.barcode_scanner)
        barcodeView.decodeContinuous(callback)
        barcodeView.setStatusText("")

        val layoutParams = barcodeView.layoutParams
        layoutParams.width = dpToPx(180) // Convert dp to px
        layoutParams.height = dpToPx(100) // Convert dp to px
        barcodeView.layoutParams = layoutParams

        // Button to open Daftar Barang activity
        val daftarbarang1 = findViewById<Button>(R.id.daftarbarang)
        daftarbarang1.setOnClickListener {
            startActivity(Intent(this, daftarbarang::class.java))
        }

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = namatoko

        // Initialize Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: run {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show()
            return
        }

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
        }

        // Request Bluetooth permissions if needed
        checkAndRequestBluetoothPermissions()
    }

    private fun checkAndRequestBluetoothPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    // Inflate menu to activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                startActivity(Intent(this, setting2::class.java))
                true
            }
            R.id.bantuan -> {
                startActivity(Intent(this, bantuan::class.java))
                true
            }
            R.id.pilihdevice -> {
                if (!bluetoothAdapter.isEnabled) {
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    bluetoothRequestLauncher.launch(enableIntent)
                } else {
                    showPairedDevices()
                }
                true
            }
            R.id.testbutton -> {
                testPrintStruk()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Menampilkan dialog daftar perangkat Bluetooth yang terpasang
    @SuppressLint("MissingPermission")
    private fun showPairedDevices() {
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        if (pairedDevices.isNotEmpty()) {
            val devicesList = pairedDevices.map { it.name }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Pilih Perangkat Bluetooth")
                .setItems(devicesList) { _, which ->
                    val selectedDevice = pairedDevices.elementAt(which)
                    bluetoothDevice = selectedDevice
                    connectPrintDevice(selectedDevice)
                }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            Toast.makeText(this, "Tidak ada perangkat Bluetooth yang terhubung", Toast.LENGTH_LONG).show()
        }
    }

    // Menghubungkan ke perangkat printer Bluetooth menggunakan Coroutine
    @SuppressLint("MissingPermission")
    private fun connectPrintDevice(device: BluetoothDevice) {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                socket.connect()
                val outputStream: OutputStream = socket.outputStream

                // Mengirim data ke printer
                sendPrintData(outputStream, "Hello Printer!")

                outputStream.close()
                socket.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Terhubung dengan ${device.name}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Gagal terhubung ke perangkat: ${device.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Method to send data to the printer
    private fun sendPrintData(outputStream: OutputStream, data: String) {
        try {
            outputStream.write(data.toByteArray())
            outputStream.flush()
            Log.d("Printer", "Data sent to printer: $data")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Printer", "Error while sending data to printer: ${e.message}")
        }
    }

    // Fungsi untuk mencetak struk kasir
    @SuppressLint("MissingPermission")
    private fun testPrintStruk() {
        bluetoothDevice?.let { device ->
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
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
        } ?: run {
            Toast.makeText(this, "Pilih perangkat Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun printReceipt(outputStream: OutputStream) {
        val receipt = """
            
            
                *** ${getnamatoko} ***
            ${getalamat}
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
        sendPrintData(outputStream, receipt)
    }

    private val bluetoothRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                showPairedDevices()
            }
        }

    // Handle runtime permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission is required to use QR code scanner", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    // Barcode callback
    private val callback = object : BarcodeCallback {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun barcodeResult(result: BarcodeResult?) {
            result?.let {
                Log.d("BarcodeResult", "Scanned QR Code: ${it.text}")
                val scannedData = it.text
                Toast.makeText(this@MainActivity, "Scanned QR Code: $scannedData", Toast.LENGTH_LONG).show()

                // Add scan result to list and update RecyclerView
                //scanResults.add(ScanResult(scannedData))
                //adapter.notifyDataSetChanged()

                val databaseHelper = DatabaseHelper(this@MainActivity)
                val barang = databaseHelper.getBarangByBarcode(scannedData)

                if (barang != null) {
                    // Update UI with the data, e.g., show in a TextView or RecyclerView
                    Toast.makeText(this@MainActivity, "Nama: ${barang.nama}, Harga: ${barang.harga}", Toast.LENGTH_LONG).show()

                    // Example: Add to RecyclerView
                    scanResults.add(ScanResult(barang.nama))
                    adapter.notifyDataSetChanged()

                } else {
                    Toast.makeText(this@MainActivity, "Barang dengan barcode $scannedData tidak ditemukan", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@MainActivity, MainActivity::class.java ))
                    finish()
                }

                // Gunakan kelas ScanResult yang telah Anda buat
                //val scanResult = com.example.toolbar.ScanResult(scannedData)
                //scanResults.add(scanResult)
                //adapter.notifyDataSetChanged()

                // Stop scanning
                barcodeView.pause()
                // Optionally restart scanning after a delay
                barcodeView.postDelayed({
                    barcodeView.resume()
                }, 1000) // Resume scanning after 1 second
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>?) {
            // Optionally handle result points
        }
    }

    // Convert dp to px
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
