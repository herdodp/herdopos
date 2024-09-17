package com.example.toolbar


import java.util.Calendar
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
import com.example.toolbar.ScanResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
import DatabaseHelper
import ScanResultAdapter
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
//import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
    //val currentDateTime = LocalDateTime.now()
    // Membuat instance dari Calendar
    val calendar = Calendar.getInstance()

    // Mendapatkan komponen waktu (jam, menit, detik)
    val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)

    // Mendapatkan komponen tanggal (tahun, bulan, hari)
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // Januari adalah bulan ke-0, sehingga perlu +1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val Timenow = "${hour}:${minute}:${second}"
    val Datenow = "${day}/${month}/${year}"



    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevice: BluetoothDevice? = null
    private var namatoko: String? = null
    private lateinit var barcodeView: DecoratedBarcodeView

    private var getnamatoko: String? = null
    private var getalamat: String? = null

    private val PERMISSION_REQUEST_CODE = 1

    private lateinit var adapter: ScanResultAdapter

    //jika scan barang menggunakan scanner
    private val scanResults = mutableListOf<ScanResult>()

    //jika input manual
    private val inputmanual = mutableListOf<ScanResult>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonclearinput : ImageButton

    //private lateinit var totalharga : TextView

    private var totalHarga = 0.0
    private var modal : Int = 0
    private var hargapokok : Int = 0

    private lateinit var totalHargaTextView: TextView

    private lateinit var databasehelper : DatabaseHelper

    //list barang
    private var listbarang: MutableList<Int> = mutableListOf()

    private var totaluangkotor : Int = 0



    private lateinit var sharepref : SharedPreferences
    private lateinit var kembalianpref : SharedPreferences
    private lateinit var lababersihpref : SharedPreferences



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














        // ================= open Fungsi untuk menampilkan dialog input kembalian ================

        fun showInputKembalianDialog() {
            // LayoutInflater untuk membuat view baru
            val inflatekembalian = LayoutInflater.from(this).inflate(R.layout.input_kembalian, null)
            val initkembalian = inflatekembalian.findViewById<EditText>(R.id.uangkembalian)

            val builder = AlertDialog.Builder(this)
            builder.setView(inflatekembalian)
            builder.setCancelable(false)
            builder.setPositiveButton("Okay") { _, _ ->

                val getuangkembalian = initkembalian.text.toString()
                val payback = (getuangkembalian.toIntOrNull() ?: 0) - totalHarga

                // Format pesan untuk uang kembalian
                val pesan = """
            
            Total pembelian  : Rp ${formatRupiah(totalHarga)}
            
            uang kostumer    : Rp ${formatRupiah(getuangkembalian.toIntOrNull()?.toDouble() ?: 0.0)}
            
            Uang kembalian   : Rp ${formatRupiah(payback.toDouble())}
            
        """.trimIndent()

                // Tampilkan dialog uang kembalian
                showUangKembalianDialog(pesan)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        //================= close Fungsi untuk menampilkan dialog uang kembalian ==================














        //================================ open button kembalian =================================

        val buttonkembalian = findViewById<ImageButton>(R.id.btnkembalian)
        buttonkembalian.setOnClickListener {
            if (totalHarga == null || totalHarga.toInt() == 0) {
                val buildkosong = AlertDialog.Builder(this)
                buildkosong.setMessage("Tidak ada transaksi")
                buildkosong.setCancelable(true)
                buildkosong.show()
            } else {
                showInputKembalianDialog()
            }
        }

        //================================== close button kembalian ================================















        // ========================== open button riwayat =======================================

        val buttonriwayat = findViewById<Button>(R.id.btnriwayat)
        buttonriwayat.setOnClickListener {
            startActivity(Intent(this@MainActivity, riwayattransaksi::class.java))
        }

        // ============================ close button riwayat ====================================













        //================= open sharedpreference uang kembalian ===============================

        sharepref = getSharedPreferences("uangkotor", Context.MODE_PRIVATE)
        kembalianpref = getSharedPreferences("uangkembalian", Context.MODE_PRIVATE)

        val shareprefvalue = sharepref.getString("uangkotor", "0")

        if(shareprefvalue?.isEmpty() == true || shareprefvalue == null){
            val editoruangkotor = sharepref.edit()
            editoruangkotor.putString("uangkotor", totaluangkotor.toString())
        }

        //laba bersih
        lababersihpref = getSharedPreferences("lababersih", Context.MODE_PRIVATE)

        // ======================== close sharedpreferance uang kembalian =========================


















        // =========================== open button statistik =====================================

        val buttonstatistik = findViewById<Button>(R.id.btnstatistik)
        buttonstatistik.setOnClickListener {

            // Inflate the view each time the button is clicked to avoid reusing the same inflated view
            val inflateviewreport = LayoutInflater.from(this).inflate(R.layout.view_report, null)
            val pendapatankotorid = inflateviewreport.findViewById<TextView>(R.id.pendapatankotor)
            val lababersihid = inflateviewreport.findViewById<TextView>(R.id.lababersih)

            val buttonclearreport = inflateviewreport.findViewById<Button>(R.id.clearreport)
            buttonclearreport.setOnClickListener {

                //clear button
                val clearbtn = AlertDialog.Builder(this)
                clearbtn.setMessage("Bersihkan Pendapatan Kotor dan Laba Bersih ? ")
                clearbtn.setPositiveButton("bersihkan"){_,_->

                    val editorincome = sharepref.edit()
                    editorincome.putString("uangkotor", "0")
                    editorincome.apply()

                    val editorlaba = lababersihpref.edit()
                    editorlaba.putString("lababersih", "0")
                    editorlaba.apply()


                    Toast.makeText(applicationContext, "Berhasil membersihkan pendapatan kotor dan laba bersih", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    finish()

                }
                clearbtn.setNegativeButton("Cancel"){dialog,_->
                    dialog.dismiss()
                }
                clearbtn.show()

            }

            // button produk terlaris
            val btnchart = inflateviewreport.findViewById<Button>(R.id.produkterlaris)
            btnchart.setOnClickListener {
                val build = AlertDialog.Builder(this@MainActivity)

                // Pesan yang ingin ditampilkan di atas link
                val pesan = "Fitur ini untuk versi PRO. Membelian versi pro silahkan klik link dibawah \n\n"

                // Teks yang akan menjadi link
                val spannableString = SpannableString("Beli versi PRO")
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // Ganti URL dengan link yang ingin Anda tambahkan
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/1eUgUBugqk3Sv2BC9"))
                        startActivity(intent)
                    }
                }
                spannableString.setSpan(clickableSpan, 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // Membuat TextView untuk menampilkan pesan dan link
                val messageTextView = TextView(this@MainActivity).apply {
                    text = pesan + spannableString // Gabungkan pesan dengan link
                    movementMethod = LinkMovementMethod.getInstance() // Mengaktifkan klik pada link
                    textSize = 16f
                    setPadding(50, 20, 50, 20) // Mengatur padding untuk teks
                }

                build.setCancelable(false)
                build.setView(messageTextView) // Mengatur tampilan pesan dengan TextView yang sudah dibuat
                build.setNeutralButton("Tutup") { dialog, _ ->
                    dialog.dismiss() // Menutup dialog saat tombol "Tutup" ditekan
                }
                build.show()
            }




            val getuangkotor = sharepref.getString("uangkotor", "0")
            pendapatankotorid.text = getuangkotor.toString()

            val labarbersihview = lababersihpref.getString("lababersih", "0")
            lababersihid.text = labarbersihview.toString()

            val buildincome = AlertDialog.Builder(this@MainActivity)
            buildincome.setTitle("Laporan Semua Transaksi")
            buildincome.setView(inflateviewreport)
            buildincome.setCancelable(true)
            buildincome.show()
        }


    // ================================== close button statistik ==================================



















        //=========================== open button manual =======================================

        val buttonmanual = findViewById<ImageButton>(R.id.addmanualitembtn)
        buttonmanual.setOnClickListener {
            val inflatebuild = LayoutInflater.from(this@MainActivity).inflate(R.layout.input_manual, null)

            val namabarang = inflatebuild.findViewById<EditText>(R.id.namabarang)
            val jumlahbarang = inflatebuild.findViewById<EditText>(R.id.jumlahbarang)
            val hargabarang = inflatebuild.findViewById<EditText>(R.id.hargabarang)

            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setView(inflatebuild)
            builder.setTitle("Masukkan Data Pembelian")
            builder.setCancelable(false)
            builder.setPositiveButton("Tambah ke chart") { _, _ ->

                val getnamabarang = namabarang.text.toString()
                val getjumlahbarangString = jumlahbarang.text.toString()
                val gethargabarangString = hargabarang.text.toString()

                // Cek apakah semua input tidak kosong
                if (getnamabarang.isNotEmpty() && getjumlahbarangString.isNotEmpty() && gethargabarangString.isNotEmpty()) {
                    val getjumlahbarang = getjumlahbarangString.toIntOrNull() ?: 0
                    val gethargabarang = gethargabarangString.toIntOrNull() ?: 0

                    val databaseHelper = DatabaseHelper(this@MainActivity)
                    val barang = databaseHelper.getBarangBynamabarang(getnamabarang)

                    if (barang != null) {
                        val stok = barang.stokbarang
                        val hargapokok =  barang.hargapokok
                        if (stok >= getjumlahbarang) {
                            val itemTotalHarga = getjumlahbarang * gethargabarang
                            val count = getjumlahbarang
                            val hargaasli = barang.harga

                            listbarang.add(getjumlahbarang)
                            totalHarga += itemTotalHarga
                            modal += hargapokok

                            // Perbarui tampilan total harga
                            totalHargaTextView.text = "${formatRupiahnonrp(totalHarga)}"

                            scanResults.add(
                                ScanResult(
                                    getnamabarang,
                                    count.toString(),
                                    hargaasli.toString(),
                                    formatRupiah(itemTotalHarga.toDouble()),
                                    modal
                                )
                            )

                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Stok tidak mencukupi untuk barang: $getnamabarang",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Barang tidak ditemukan: $getnamabarang", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create()
            builder.show()
        }

        //================================== close button manual ==================================














        //total harga
        totalHargaTextView = findViewById(R.id.totalharga) // Ganti dengan ID TextView Anda


















        //=========================== open button selesaikan transaksi ===========================

        val btntransaksi = findViewById<Button>(R.id.btndonetransaction)
        btntransaksi.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Complete Transaction")
            builder.setCancelable(false)
            val pesan = """
                    Selesaikan transaksi ini ?
                    
                    Note:
                    - hubungkan ulang perangkat bluetooth setelah proses transaksi selesai
                    
                   
                """.trimIndent()
            builder.setMessage(pesan)
            builder.setPositiveButton("Selesaikan"){_,_ ->
                //Toast.makeText(applicationContext, "Transaksi selesai", Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setMessage("Print struk transaksi ini ?")
                builder.setPositiveButton("Print"){_,_->

                        lifecycleScope.launch {
                            Toast.makeText(applicationContext, "Process print struk.", Toast.LENGTH_SHORT).show()
                            delay(500)
                            Toast.makeText(applicationContext, "Process print struk. .", Toast.LENGTH_SHORT).show()
                            delay(500)
                            Toast.makeText(applicationContext, "Process print struk. . .", Toast.LENGTH_SHORT).show()
                            delay(500)

                            lakukanTransaksiDanCetakStruk()

                        }


                }
                builder.setNegativeButton("Tidak"){dialog,_->
                    //startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    //dialog.dismiss()
                    selesaikanTransaksiTanpaCetakStruk()
                }
                builder.setNeutralButton("Batalkan proses"){ dialog,_->
                    dialog.dismiss()
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

        //=========================== close button selesaikan transaksi ========================



















        //======================== open sharedpreference nama dan alamat toko ====================
        val prefs = getSharedPreferences("My_Prefs", Context.MODE_PRIVATE)
        val prefsalamat = getSharedPreferences("alamat", Context.MODE_PRIVATE)

        getnamatoko = prefs?.getString("keynamatoko", "default")
        getalamat = prefsalamat?.getString("keyalamat", "default")
        //======================= close sharedpreferance nama dan alamat toko ==================









        //============================ open init recyclerview ===================================
        recyclerView = findViewById(R.id.recyclerviewmain) // Pastikan ID ini sesuai dengan ID di layout XML Anda
        adapter = ScanResultAdapter(scanResults)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        //=========================== close init recyclerview ===================================












        //=========================== open button clear input item ===============================
        buttonclearinput = findViewById(R.id.cleariteminputbtn)
        buttonclearinput.setOnClickListener {
            cleariteminput()
        }
        //============================ close button clear input item ============================












        //========================== open init Barcode Scanner ==================================
        barcodeView = findViewById(R.id.barcode_scanner)
        barcodeView.decodeContinuous(callback)
        barcodeView.setStatusText("")

        val layoutParams = barcodeView.layoutParams
        layoutParams.width = dpToPx(180) // Convert dp to px
        layoutParams.height = dpToPx(100) // Convert dp to px
        barcodeView.layoutParams = layoutParams
        //=========================== close init barcode scanner ===============================













        //=============================== open tombol daftar barang ===========================
        val daftarbarang1 = findViewById<Button>(R.id.daftarbarang)
        daftarbarang1.setOnClickListener {
            startActivity(Intent(this, daftarbarang::class.java))
        }
        //============================== close tombol daftar barang ===========================













        //============================== open Toolbar ==========================================
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = namatoko
        //============================== close toolbar =========================================












        //====================== open Initialize Bluetooth Adapter =============================
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
        //========================= close Initialize Bluetooth Adapter =============================
















    //===========================================================================================
    //============================= BATAS ONCREATE DAN FUNCTION =================================
    } //<<=======================================================================================
    //============================= BATAS ONCREATE DAN FUNCTION =================================
    //===========================================================================================













    //================================== open check permission bluetooth =========================
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
    //=================================== close check permission bluetooth ========================















    //========================= open func uang kembalian =========================================
    fun showUangKembalianDialog(pesan: String) {
        val buildkembalian = AlertDialog.Builder(this)
        buildkembalian.setTitle("Uang Kembalian")
        buildkembalian.setCancelable(false)
        buildkembalian.setMessage(pesan)
        buildkembalian.setPositiveButton("Lanjutkan ?") { dialog, _ ->

            val buildlanjut = AlertDialog.Builder(this)
            buildlanjut.setCancelable(false)
            buildlanjut.setMessage("Selesaikan Transaksi ?")
            buildlanjut.setPositiveButton("Ya"){_,_->
                val buildprint = AlertDialog.Builder(this)
                buildprint.setMessage("Cetak struk transaksi ini ?")
                buildprint.setCancelable(false)
                buildprint.setPositiveButton("Ya"){_,_->
                    lakukanTransaksiDanCetakStruk()
                }
                buildprint.setNegativeButton("Tidak"){dialog,_->
                    selesaikanTransaksiTanpaCetakStruk()
                }
                buildprint.setNeutralButton("Batalkan"){dialog,_->
                    dialog.dismiss()
                }
                buildprint.create()
                buildprint.show()
            }
            buildlanjut.setNegativeButton("Tutup"){dialog,_->
                dialog.dismiss()
            }
            buildlanjut.show()

        }
        buildkembalian.show()
    }
    //============================ close func uang kembalian =====================================
















    //=============================== open menu titik tiga toolbar ==============================
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
                true
            }

            R.id.versipro -> {


                    val build = AlertDialog.Builder(this@MainActivity)

                    // Pesan yang ingin ditampilkan di atas link
                    val pesan = "Untuk pembelian versi PRO. Silahkan klik link dibawah\n\n"

                    // Teks yang akan menjadi link
                    val spannableString = SpannableString("Beli versi PRO")
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            // Ganti URL dengan link yang ingin Anda tambahkan
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/1eUgUBugqk3Sv2BC9"))
                            startActivity(intent)
                        }
                    }
                    spannableString.setSpan(clickableSpan, 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // Membuat TextView untuk menampilkan pesan dan link
                    val messageTextView = TextView(this@MainActivity).apply {
                        text = pesan // Tampilkan pesan
                        append(spannableString) // Tambahkan link di bawah pesan
                        movementMethod = LinkMovementMethod.getInstance() // Mengaktifkan klik pada link
                        textSize = 16f
                        setPadding(50, 20, 50, 20) // Mengatur padding untuk teks
                    }

                    build.setCancelable(false)
                    build.setView(messageTextView) // Mengatur tampilan pesan dengan TextView yang sudah dibuat
                    build.setNeutralButton("Tutup") { dialog, _ ->
                        dialog.dismiss() // Menutup dialog saat tombol "Tutup" ditekan
                    }
                    build.show()



                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //========================= close menu titik tiga toolbar ====================================


















    //===================== open Menampilkan dialog daftar perangkat ==============================
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

    //========================== close menampilkan dialog daftar perangkat ====================














    //================================ open send data untuk print ================================
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
    //=============================== close send data untuk print ==============================




















    //============================= open func tes cetak struk ====================================

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
            Id struk : wffvdsdef
            ---------------------------
            Item   Qty  Price  Total
            ---------------------------
            item 1  2   30.000  60.000
            item 2  5   25.000  125.000
            item 3  3   30.000  90.000
            item 4  4   30.000  90.000
            item 5  3   30.000  90.000
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Camera permission is required to use QR code scanner", Toast.LENGTH_LONG).show()
            }
        }
    }

    //================================== close tes cetak struk ====================================


















    //=================================== open onResume dan onPause ===============================
    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
    //================================ close onResume dan onPause =================================


















    //===============================  open BarcodeResult callback  ==============================

    private val callback = object : BarcodeCallback {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun barcodeResult(result: BarcodeResult?) {
            result?.let {
                val scannedData = it.text

                val databaseHelper = DatabaseHelper(this@MainActivity)
                val barang = databaseHelper.getBarangByBarcode(scannedData)

                if (barang != null) {
                    // Membuka dialog input jumlah item
                    val inflateitemcount = LayoutInflater.from(this@MainActivity).inflate(R.layout.popup_countitem, null)

                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setView(inflateitemcount)
                    builder.setTitle("Jumlah Item")
                    builder.setCancelable(false)
                    builder.setPositiveButton("Ok") { _, _ ->

                        val itemcount = inflateitemcount.findViewById<EditText>(R.id.inputjumlah)
                        val jumlahItemText = itemcount.text.toString()

                        // Validasi apakah input kosong atau tidak valid
                        if (jumlahItemText.isEmpty() || jumlahItemText.toIntOrNull() == null || jumlahItemText.toInt() <= 0) {
                            val buildempty = AlertDialog.Builder(this@MainActivity)
                            buildempty.setCancelable(true)
                            buildempty.setMessage("Harap masukkan jumlah item yang valid")
                            buildempty.show()
                        } else {
                            val jumlahitemcount = jumlahItemText.toInt()

                            val totalharga = jumlahitemcount * barang.harga
                            val hargaasli = barang.harga
                            val hargapokok = barang.hargapokok
                            val count = jumlahitemcount

                            listbarang.add(count)
                            totalHarga += totalharga
                            modal += hargapokok

                            // Perbarui tampilan total harga
                            totalHargaTextView.text = "${formatRupiahnonrp(totalHarga)}"

                            // Tambahkan hasil scan ke dalam scanResults dan update adapter
                            scanResults.add(ScanResult(barang.nama, count.toString(), hargaasli.toString(), formatRupiahnonrp(totalharga), modal))
                            adapter.notifyDataSetChanged()
                        }
                    }
                    builder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val dialog = builder.create()
                    dialog.setOnShowListener {
                        inflateitemcount.requestFocus()

                        // Menampilkan keyboard secara otomatis
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(inflateitemcount, InputMethodManager.SHOW_IMPLICIT)
                    }
                    dialog.show()

                } else {
                    Toast.makeText(this@MainActivity, "Barang tidak ditemukan", Toast.LENGTH_LONG).show()
                }

                // Stop scanning
                barcodeView.pause()

                // Restart scanning after a delay (opsional)
                barcodeView.postDelayed({
                    barcodeView.resume()
                }, 1000) // Mulai kembali scanning setelah 1 detik
            }
        }

        //=========================== close barcodeResult callback ================================













    override fun possibleResultPoints(resultPoints: List<ResultPoint>?) {
            // Optionally handle result points
        }
    }



















    //============================== open format 000 ke K ======================================
    fun formatAngkaToK(amount: Double): String {
        return if (amount >= 1000.00) {
            val formatted = (amount / 1000.00).toString() + "K"
            formatted
        } else {
            amount.toString()
        }
    }
    //============================ close format 000 ke K  =====================================








    //============================= open format string to Int =================================
    fun parseStringToInt(value: String): Int {
        return when {
            value.contains("K") -> {
                (value.replace("K", "").toDouble() * 1000).toInt()
            }
            value.contains("M") -> {
                (value.replace("M", "").toDouble() * 1000000).toInt()
            }
            else -> {
                value.toIntOrNull() ?: 0  // Menghindari NumberFormatException jika ada format yang salah
            }
        }
    }
    //============================== close format string to Int ===============================












    //============================ open format string to double ===============================
    fun parseStringToDouble(value: String): Double {
        return value.toDoubleOrNull() ?: 0.0
    }
    //=========================== close format string to double ===============================



















    // ================================ open selesaikan transaksi ==============================

    @SuppressLint("MissingPermission")
    private fun lakukanTransaksiDanCetakStruk() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak tersedia pada perangkat ini", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth tidak aktif, minta pengguna untuk mengaktifkannya
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }

        // Cek apakah perangkat Bluetooth sudah dipilih
        val device = bluetoothDevice
        if (device == null) {
            // Jika perangkat Bluetooth belum dipilih, buka aktivitas untuk memilih perangkat
            val pairedDevices = bluetoothAdapter.bondedDevices
            if (pairedDevices.isNotEmpty()) {
                // Tampilkan perangkat yang sudah dipairing
                val devices = pairedDevices.map { it.name to it.address }.toMap()
                // Tampilkan dialog atau aktivitas untuk memilih perangkat
                showDeviceSelectionDialog(devices)
            } else {
                Toast.makeText(this, "Tidak ada perangkat Bluetooth yang dipairing", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Jika perangkat Bluetooth sudah dipilih, lanjutkan ke proses pencetakan struk
        cetakStruk()
    }

    private fun showDeviceSelectionDialog(devices: Map<String, String>) {
        val deviceNames = devices.keys.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Pilih Perangkat Bluetooth")
            .setItems(deviceNames) { dialog, which ->
                val selectedDeviceAddress = devices.values.toTypedArray()[which]
                bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(selectedDeviceAddress)
                Toast.makeText(this, "Perangkat Bluetooth dipilih: ${deviceNames[which]}", Toast.LENGTH_SHORT).show()

                // Setelah memilih perangkat, lanjutkan proses pencetakan struk
                lakukanTransaksiDanCetakStruk()
            }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun cetakStruk() {
        // Buat format struk kasir dengan item dari RecyclerView
        val strukBuilder = StringBuilder()

        val totalHargaText = formatRupiahnonrp(totalHarga)
        val totalHargaDouble = parseToDouble(totalHargaText)

        val databasehelper = DatabaseHelper(this@MainActivity)
        val no_struk = randomnostruk()

        // Format struk kasir
        strukBuilder.append("\n\n*** ${getnamatoko} ***\n")
        strukBuilder.append("${getalamat}\n")
        strukBuilder.append("Id struk : ${no_struk}\n")
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("Item    Qty   Price   Total\n")
        strukBuilder.append("---------------------------\n")

        var totalModal = 0.0

        // Tambahkan item ke struk
        for ((index, scanResult) in scanResults.withIndex()) {

            val hargaPokokPerItem = databasehelper.getHargaPokokById(scanResult.text)


            //val harga1 = formatAngkaToK(scanResult.harga.toDouble()) // Menggunakan fungsi formatAngkaToK
            val harga1 = formatAngkaToK(scanResult.harga.toDouble())
            val hargaasli = formatAngkaToK(scanResult.hargaasli.toDouble()) // Menggunakan fungsi formatAngkaToK
            val hargaaslipokok = parseStringToInt(hargaasli)

            val qty = if (index < listbarang.size) listbarang[index] else 0

            // Harga pokok per item
            //val qtyitem = scanResult.qty
            val hargapokokPerItem = hargaPokokPerItem * qty

            //Toast.makeText(applicationContext, "harga pokok : ${hargaaslipokok}, qty : ${qty}", Toast.LENGTH_SHORT).show()
            val totalModalItem = hargapokokPerItem // Harga pokok per item bukan per qty
            totalModal += totalModalItem

            val itemNama = scanResult.text.take(6).padEnd(8, ' ') // Panjang maksimal 6 karakter
            val qtyText = qty.toString().padEnd(5, ' ') // Panjang kolom Qty menjadi 5 karakter
            val hargaText = harga1.padStart(6, ' ') // Panjang kolom Price menjadi 6 karakter
            val hargaaslitext = hargaasli.padStart(6, ' ')

            strukBuilder.append("${itemNama}${qtyText}${hargaaslitext}${hargaText}\n")

            val isUpdated = databasehelper.updateStokBarang(scanResult.text, qty)
            if (!isUpdated) {
                Toast.makeText(this, "Gagal mengurangi stok barang: ${scanResult.text}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Tambahkan informasi total dan waktu ke struk
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("Total               ${formatAngkaToK(totalHarga)}\n") // Menggunakan fungsi formatAngkaToK
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("${Timenow}\n")
        strukBuilder.append("${Datenow}\n")
        strukBuilder.append("       Terima Kasih       \n\n")

        val strukutuh = strukBuilder.toString()

        // Totalkan pendapatan
        val gettotaluangkotor = sharepref.getString("uangkotor", "0")
        val totalpendapatan = gettotaluangkotor?.toDouble()?.plus(totalHarga)

        val editoruangkotor = sharepref.edit()
        editoruangkotor.putString("uangkotor", totalpendapatan.toString())
        editoruangkotor.apply()

        // Hitung laba bersih
        val getlababersih = lababersihpref.getString("lababersih", "0")
        val lababersih = totalHarga - totalModal
        val labafinal = getlababersih?.toDouble()?.plus(lababersih)

        val editortotalpokok = lababersihpref.edit()
        editortotalpokok.putString("lababersih", labafinal.toString())
        editortotalpokok.apply()
        // Simpan data struk ke database
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

        // Cetak struk jika perangkat Bluetooth tersedia
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val socket = bluetoothDevice?.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()
                val outputStream: OutputStream = socket!!.outputStream

                sendPrintData(outputStream, strukutuh)

                outputStream.close()
                socket.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                    delay(2000)
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Gagal mencetak struk", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth telah diaktifkan, lanjutkan proses
                    lakukanTransaksiDanCetakStruk()
                } else {
                    Toast.makeText(this, "Bluetooth tidak diaktifkan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }

    // ================================ close selesaikan transaksi =================================























//=============================== open  transaksi tanpa cetak struk  ==============================

    private fun selesaikanTransaksiTanpaCetakStruk() {
        // Buat format struk kasir dengan item dari RecyclerView
        val strukBuilder = StringBuilder()

        //val totalHargaText = formatRupiahnonrp(totalHarga)
        //val totalHargaDouble = parseToDouble(totalHargaText)

        val databasehelper = DatabaseHelper(this@MainActivity)
        val no_struk = randomnostruk()

        // Format struk kasir
        strukBuilder.append("\n\n*** ${getnamatoko} ***\n")
        strukBuilder.append("${getalamat}\n")
        strukBuilder.append("Id struk : ${no_struk}\n")
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("Item    Qty   Price   Total\n")
        strukBuilder.append("---------------------------\n")

        var totalModal = 0.0

        // Tambahkan item ke struk
        for ((index, scanResult) in scanResults.withIndex()) {


            val hargaPokokPerItem = databasehelper.getHargaPokokById(scanResult.text)


            val harga1 = formatAngkaToK(scanResult.harga.toDouble()) // Menggunakan fungsi formatAngkaToK
            val hargaasli = formatAngkaToK(scanResult.hargaasli.toDouble()) // Menggunakan fungsi formatAngkaToK
            val qty = if (index < listbarang.size) listbarang[index] else 0
            //val hargaaslipokok =  parseStringToDouble(hargaasli)

            // Harga pokok per item
            //val hargapokokPerItem = scanResult.modal
            val totalModalItem = hargaPokokPerItem * qty
            totalModal += totalModalItem

            val itemNama = scanResult.text.take(6).padEnd(8, ' ') // Panjang maksimal 6 karakter
            val qtyText = qty.toString().padEnd(5, ' ') // Panjang kolom Qty menjadi 5 karakter
            val hargaText = harga1.padStart(6, ' ') // Panjang kolom Price menjadi 6 karakter
            val hargaaslitext = hargaasli.padStart(6, ' ')

            strukBuilder.append("${itemNama}${qtyText}${hargaaslitext}${hargaText}\n")

            val isUpdated = databasehelper.updateStokBarang(scanResult.text, qty)
            if (!isUpdated) {
                Toast.makeText(this, "Gagal mengurangi stok barang: ${scanResult.text}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Tambahkan informasi total dan waktu ke struk
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("Total               ${formatAngkaToK(totalHarga)}\n") // Menggunakan fungsi formatAngkaToK
        strukBuilder.append("---------------------------\n")
        strukBuilder.append("${Timenow}\n")
        strukBuilder.append("${Datenow}\n")
        strukBuilder.append("       Terima Kasih       \n\n")

        val strukutuh = strukBuilder.toString()

        // Totalkan pendapatan
        val gettotaluangkotor = sharepref.getString("uangkotor", "0")
        val totalpendapatan = gettotaluangkotor?.toDouble()?.plus(totalHarga)

        val editoruangkotor = sharepref.edit()
        editoruangkotor.putString("uangkotor", totalpendapatan.toString())
        editoruangkotor.apply()

        // Hitung laba bersih
        val getlababersih = lababersihpref.getString("lababersih", "0")
        val lababersih = totalHarga - totalModal
        val labafinal = getlababersih?.toDouble()?.plus(lababersih)

        val editortotalpokok = lababersihpref.edit()
        editortotalpokok.putString("lababersih", labafinal.toString())
        editortotalpokok.apply()

        // Simpan data struk ke database
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

        startActivity(Intent(this@MainActivity, MainActivity::class.java))

        Log.d("StrukKasir", strukutuh)
    }


//================================ close transaksi tanpa cetak struk  =============================






















    //============================= open random string struk =====================================
    private fun randomnostruk(): String {
        val alfanumerik = "0123456789abcdefghijklmnopqrstuvwxyz"
        return (1..10).map { alfanumerik.random() }.joinToString("")
    }
    //============================ close random string struk ====================================
















    //===================================== open Convert dp to px ================================
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
    //==================================== close convert dp to px ===============================














//============================== open func clear input Mainactivity =============================

    private fun cleariteminput() {
        val hapusitem = AlertDialog.Builder(this)
        hapusitem.setTitle("Hapus Item")

        // Gabungkan semua pesan menjadi satu string
        val pesan = """
        Bersihkan semua item input?
        
        Note:
        - Hubungkan ulang perangkat bluetooth
     
    """.trimIndent()

        hapusitem.setMessage(pesan)

        hapusitem.setPositiveButton("Bersihkan") { _, _ ->
            // Bersihkan item yang ada
            scanResults.clear()
            adapter.notifyDataSetChanged()

            // Setel ulang total harga
            totalHarga = 0.0
            totalHargaTextView.text = "${formatRupiahnonrp(totalHarga)}"

            // Muat ulang aktivitas
            startActivity(Intent(this@MainActivity, MainActivity::class.java))
            finish()

            Toast.makeText(applicationContext, "Hubungkan ulang perangkat bluetooth", Toast.LENGTH_SHORT).show()
        }

        hapusitem.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        hapusitem.create()
        hapusitem.show()
    }

    //========================= close func clear input mainactivity ==============================


















    //================================== open format =============================================

    fun formatRupiah(amount: Double): String {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val rupiah = formatRupiah.format(amount)

        // Menghapus "Rp" dan menghilangkan ",00" di akhir jika ada
        return rupiah.replace("Rp", "").replace(",00", "").trim()
    }


    //mengganti format rupiah tanpa Rp
    fun formatRupiahnonrp(amount: Double): String {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val formattedAmount = formatRupiah.format(amount)

        // Menghapus "Rp" dan menghilangkan ",00" di akhir jika ada
        return formattedAmount.replace("Rp", "").replace(",00", "").trim()
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

    //=================================== close format ===========================================












    //============================== open tombol back ============================================
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
    //============================== close tombol back ===========================================





}
