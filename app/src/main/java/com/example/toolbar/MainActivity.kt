package com.example.toolbar

import android.bluetooth.BluetoothSocket
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var framela: FrameLayout
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevice: BluetoothDevice? = null
    private var namatoko: String? = null
    private var alamat: String? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //intent daftar barang
        val daftarbarang1 = findViewById<Button>(R.id.daftarbarang)
        daftarbarang1.setOnClickListener{
            startActivity(Intent(this, daftarbarang::class.java))
        }



        // Mengambil nilai dari SharedPreferences
        val sharedPrefs = getSharedPreferences("My_Prefs", Context.MODE_PRIVATE)
        val alamatget = getSharedPreferences("alamat", Context.MODE_PRIVATE)
        namatoko = sharedPrefs.getString("keynamatoko", "Default")
        alamat = alamatget.getString("keyalamat", "Default")

        // Mengatur Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = namatoko

        // Inisialisasi Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: run {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show()
            return
        }

        // Button untuk test print
        val testButton: Button = findViewById(R.id.testbutton)
        testButton.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothRequestLauncher.launch(enableIntent)
            } else {
                findBluetooth()
                bluetoothDevice?.let {
                    connectPrintDevice(it)
                } ?: run {
                    Toast.makeText(this, "Bluetooth device not found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Menginflate menu ke activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    // Penanganan click pada menu
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Penanganan back press
    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Apakah anda ingin menutup aplikasi?")
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    // Mencari perangkat Bluetooth yang telah terhubung
    @SuppressLint("MissingPermission")
    private fun findBluetooth() {
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        bluetoothDevice = pairedDevices.find { it.name == "RPP02" }
        if (bluetoothDevice == null) {
            Toast.makeText(this, "Bluetooth device not found", Toast.LENGTH_LONG).show()
        }
    }

    // Menghubungkan ke perangkat Bluetooth untuk mencetak
    @SuppressLint("MissingPermission")
    private fun connectPrintDevice(device: BluetoothDevice) {
        val uuid: UUID = device.uuids.firstOrNull()?.uuid ?: return
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

    // Mencetak struk
    private fun printReceipt(outputStream: OutputStream) {
        val receipt = """
            *** $namatoko ***
            $alamat
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

    // ActivityResultLauncher untuk mengaktifkan Bluetooth
    private val bluetoothRequestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            findBluetooth()
            bluetoothDevice?.let {
                connectPrintDevice(it)
            } ?: run {
                Toast.makeText(this, "Bluetooth device not found", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_LONG).show()
        }
    }
}
