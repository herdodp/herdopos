package com.example.toolbar

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.toolbar.ScanResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
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
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {


    @SuppressLint("NewApi")
    val currentDateTime = LocalDateTime.now()


    // Mendapatkan tanggal
    @SuppressLint("NewApi")
    val date = currentDateTime.toLocalDate()
    @SuppressLint("NewApi")
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    @SuppressLint("NewApi")
    val Datenow = date.format(dateFormatter)

    // Mendapatkan waktu
    @SuppressLint("NewApi")
    val time = currentDateTime.toLocalTime()
    @SuppressLint("NewApi")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    @SuppressLint("NewApi")
    val Timenow = time.format(timeFormatter)

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
    private lateinit var buttonclearinput : ImageButton

    //private lateinit var totalharga : TextView

    private var totalHarga = 0.0
    private lateinit var totalHargaTextView: TextView

    private lateinit var databasehelper : DatabaseHelper

    //list barang
    private var listbarang: MutableList<Int> = mutableListOf()


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


        // button riwayat
        val buttonriwayat = findViewById<Button>(R.id.btnriwayat)
        buttonriwayat.setOnClickListener {
            startActivity(Intent(this@MainActivity, riwayattransaksi::class.java))
            finish()
        }



        //total harga
        totalHargaTextView = findViewById(R.id.totalharga) // Ganti dengan ID TextView Anda















        //button selesaikan transaksi
        val btntransaksi = findViewById<Button>(R.id.btndonetransaction)
        btntransaksi.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Complete Transaction")
            builder.setMessage("Selesaikan Transaksi ini ?")
            builder.setPositiveButton("Selesaikan"){_,_ ->
                Toast.makeText(applicationContext, "Transaksi selesai", Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Print struk transaksi ini ?")
                builder.setPositiveButton("Print"){_,_->

                        lifecycleScope.launch {
                            Toast.makeText(applicationContext, "Process print struk.", Toast.LENGTH_SHORT).show()
                            delay(1000) // Delay selama 2 detik
                            Toast.makeText(applicationContext, "Process print struk. .", Toast.LENGTH_SHORT).show()
                            delay(1000)
                            Toast.makeText(applicationContext, "Process print struk. . .", Toast.LENGTH_SHORT).show()
                            delay(2000)

                            selesaikanTransaksi()

                        }

                    //val scannedData =
                    //Toast.makeText(this@MainActivity, "Scanned QR Code: $scannedData", Toast.LENGTH_LONG).show()

                    // Add scan result to list and update RecyclerView
                    //scanResults.add(ScanResult(scannedData))
                    //adapter.notifyDataSetChanged()

                    //val databaseHelper = DatabaseHelper(this@MainActivity)
                    //val barang = databaseHelper.getBarangByBarcode(scannedData)

                    //menyimpan data di table riwayat
                   // val result = databasehelper.insertDatariwayat(barang.nama, qtybarang, hargaasli, totalharga, jamriwayat, tanggalriwayat, nomorstruk)
                    //if (result != -1L) {
                     //Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        //displayData() // Refresh data setelah penyimpanan
                        //startActivity(Intent(this, daftarbarang::class.java))
                        //finish()
                    //} else {
                       // Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_SHORT).show()
                   // }


                }
                builder.setNegativeButton("Cancel"){dialog,_->
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                }
                builder.create()
                builder.show()

            }
            builder.setNegativeButton("Batal"){dialog, _->
                dialog.dismiss()
            }
            builder.create()
            builder.show()

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



        //button clear input item
        buttonclearinput = findViewById(R.id.cleariteminputbtn)
        buttonclearinput.setOnClickListener {
            cleariteminput()
        }






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
                finish()
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

            R.id.about -> {
                startActivity(Intent(this@MainActivity, about::class.java))
                finish()
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
               // sendPrintData(outputStream, "Hello Printer!")

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
            Item    Qty    Price
            ---------------------------
            item 1   2  30.000  60.000
            item 2   5  25.000  125.000
            item 3   3  30.000  90.000
            item 4   4  30.000  90.000
            item 5   3  30.000  90.000
            ---------------------------
            Total               275.000
            ---------------------------
                   Terima Kasih
      
      
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
                //Toast.makeText(this, "Camera permission is required to use QR code scanner", Toast.LENGTH_LONG).show()
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
                //Log.d("BarcodeResult", "Scanned QR Code: ${it.text}")
                val scannedData = it.text
                //Toast.makeText(this@MainActivity, "Scanned QR Code: $scannedData", Toast.LENGTH_LONG).show()

                // Add scan result to list and update RecyclerView
                //scanResults.add(ScanResult(scannedData))
                //adapter.notifyDataSetChanged()

                val databaseHelper = DatabaseHelper(this@MainActivity)
                val barang = databaseHelper.getBarangByBarcode(scannedData)

                if (barang != null) {
                    // Update UI with the data, e.g., show in a TextView or RecyclerView
                    //Toast.makeText(this@MainActivity, "Nama: ${barang.nama}, Harga: ${barang.harga}", Toast.LENGTH_LONG).show()


                    //muncul alertdialog input jumlah barang yang dibeli

                    //val count = 0

                    val inflateitemcount = LayoutInflater.from(this@MainActivity).inflate(R.layout.popup_countitem, null)


                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setView(inflateitemcount)
                    builder.setTitle("Jumlah Item")
                    builder.setPositiveButton("Ok"){_,_ ->

                        val itemcount = inflateitemcount.findViewById<EditText>(R.id.inputjumlah)
                        val jumlahitemcount = itemcount.text.toString().toInt()
                        val totalharga = jumlahitemcount*barang.harga
                        val count = jumlahitemcount

                        listbarang.add(count)


                        totalHarga += totalharga
                        // Perbarui tampilan total harga
                        totalHargaTextView.text = "${formatRupiahnonrp(totalHarga)}"

                        scanResults.add(ScanResult(barang.nama, count.toString(),formatRupiah(totalharga)))

                        adapter.notifyDataSetChanged()




                    }
                    builder.setNegativeButton("Cancel"){dialog,_->
                        dialog.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.setOnShowListener {
                        inflateitemcount.requestFocus()

                        // Gunakan InputMethodManager untuk menampilkan keyboard
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(inflateitemcount, InputMethodManager.SHOW_IMPLICIT)
                    }
                    //builder.create()
                    builder.show()





                    // Example: Add to RecyclerView

                } else {
                    Toast.makeText(this@MainActivity, "Barang tidak ditemukan", Toast.LENGTH_LONG).show()

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












    @SuppressLint("MissingPermission")
    private fun selesaikanTransaksi() {
        // Buat format struk kasir dengan item dari RecyclerView
        val strukBuilder = StringBuilder()

        val totalHargaText = formatRupiahnonrp(totalHarga)
        val totalHargaDouble = parseToDouble(totalHargaText)

        // Format struk kasir
        strukBuilder.append("\n\n*** ${getnamatoko} ***\n")
        strukBuilder.append("${getalamat}\n")
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("Item    Qty    Price\n")
        strukBuilder.append("---------------------------\n")

        val databasehelper = DatabaseHelper(this@MainActivity)
        val no_struk = randomnostruk()

        // Tambahkan item ke struk
        for (scanResult in scanResults) {
            val harga1 = scanResult.harga
            val cleanedHargaString = harga1.replace("Rp", "")
                .replace(".", "")
                .replace(",", ".")
                .trim()

            strukBuilder.append("${scanResult.text}   23   ${harga1}\n")
        }

        // Tambahkan informasi total dan waktu ke struk
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("Total               ${formatRupiahnonrp(totalHarga)}\n")
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("${Timenow}\n")
        strukBuilder.append("${Datenow}\n")
        strukBuilder.append("       Terima Kasih       \n\n")

        // Dapatkan struk utuh setelah selesai menambah semua elemen
        val strukutuh = strukBuilder.toString()

        // Menyimpan data struk ke database
        val result = databasehelper.insertDatariwayat(
            stringstruk = strukutuh,
            nostruk = no_struk,
            jamstruk = Timenow,
            tanggaltruk = Datenow
        )

        if (result == -1L) {
            Toast.makeText(this, "Gagal menyimpan data struk!", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Berhasil menyimpan data struk!", Toast.LENGTH_SHORT).show()

        // Lanjutkan dengan Intent ke halaman riwayat
        val intent = Intent(this@MainActivity, riwayattransaksi::class.java)
        intent.putExtra("nostruk", no_struk)
        startActivity(intent)

        // Tampilkan struk kasir di Logcat atau cetak ke printer
        Log.d("StrukKasir", strukutuh)

        // Cetak struk jika diperlukan
        bluetoothDevice?.let { device ->
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val socket = device.createRfcommSocketToServiceRecord(uuid)
                    socket.connect()
                    val outputStream: OutputStream = socket.outputStream

                    // Kirim data struk ke printer
                    sendPrintData(outputStream, strukutuh)

                    outputStream.close()
                    socket.close()

                    withContext(Dispatchers.Main) {
                        lifecycleScope.launch {
                            Toast.makeText(applicationContext, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                            delay(2000) // Delay selama 2 detik
                            startActivity(Intent(this@MainActivity, MainActivity::class.java))
                            Toast.makeText(applicationContext, "Pilih perangkat bluetooth dulu sebelum menyelesaikan transaksi", Toast.LENGTH_SHORT).show()
                            delay(1000)
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Gagal mencetak struk", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "Pilih perangkat Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }



    private fun randomnostruk(): String {
        val alfanumerik = "0123456789abcdefghijklmnopqrstuvwxyz"
        return (1..10).map { alfanumerik.random() }.joinToString("")
    }













    // Convert dp to px
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun cleariteminput(){
        val hapusitem = AlertDialog.Builder(this)
        hapusitem.setTitle("Hapus Item")
        hapusitem.setMessage("Bersihkan semua item input ?")
        hapusitem.setPositiveButton("Bersihkan"){_,_->
            scanResults.clear()
            adapter.notifyDataSetChanged()

            totalHarga = 0.0
            totalHargaTextView.text = "$totalHarga"

            startActivity(Intent(this@MainActivity, MainActivity::class.java))
            finish()
        }
        hapusitem.setNegativeButton("Batal"){dialog ,_->
            dialog.dismiss()
        }
        hapusitem.create()
        hapusitem.show()

    }



    //mengganti format rupiah
    fun formatRupiah(amount: Double): String {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val rupiah = formatRupiah.format(amount)
        // Menambahkan spasi setelah "Rp"
        return rupiah.replace("Rp", "Rp ")
    }


    //mengganti format rupiah tanpa Rp
    fun formatRupiahnonrp(amount: Double): String {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val formattedAmount = formatRupiah.format(amount)
        // Menghapus "Rp" dari hasil format
        return formattedAmount.replace("Rp", "").trim()
    }


    private fun removeRpPrefix(amount: String): String {
        return amount.replace("Rp ", "").replace(",", "").trim()
    }

    private fun parseToDouble(amount: String): Double {
        return try {
            removeRpPrefix(amount).toDouble()
        } catch (e: NumberFormatException) {
            0.0 // Mengembalikan nilai default jika parsing gagal
        }
    }


    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("keluar dari aplikasi ?")
        builder.setPositiveButton("Keluar"){_,_->
            finishAffinity()
        }
        builder.setNegativeButton("Batal"){dialog,_->
            dialog.dismiss()
        }
        builder.create()
        builder.show()
        //super.onBackPressed()
    }


}
