package com.example.toolbar

import DatabaseHelper
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class daftarbarang : AppCompatActivity() {

    private lateinit var barangAdapter: BarangAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_daftarbarang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar2: Toolbar = findViewById(R.id.toolbar1)
        setSupportActionBar(toolbar2)

        supportActionBar?.title = "Daftar barang"







        // Inisiasi RecyclerView
        recyclerView = findViewById(R.id.recyclerView)

        // Inisiasi DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Tampilkan data
        displayData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_daftarbarang, menu)
        return true
    }








    //tambah barang
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.tambahbarang1 -> {
                val inflatedaftar = LayoutInflater.from(this)
                val viewdaftar = inflatedaftar.inflate(R.layout.addbarang, null)


                AlertDialog.Builder(this)
                    .setTitle("Tambah Barang")
                    .setView(viewdaftar)
                    .setCancelable(false)
                    .setPositiveButton("Simpan") { _, _ ->
                        // Inisiasi ID nama dan harga barang
                        val editTextNama: EditText = viewdaftar.findViewById(R.id.namabarang)
                        val editTextHarga: EditText = viewdaftar.findViewById(R.id.hargabarang)
                        val idbarcode = idalfanumerik()

                        // Ambil nilai input
                        val nama = editTextNama.text.toString()
                        val harga = editTextHarga.text.toString().toDoubleOrNull()

                        // Proses menginput barang
                        if (nama.isNotEmpty() && harga != null) {
                            val result = databaseHelper.insertData(nama, harga, idbarcode)
                            if (result != -1L) {
                                Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, daftarbarang::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Isi semua kolom dengan benar!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    //random string id alfanumerik
    private fun idalfanumerik(): String {
        val alfanumerik = "0123456789abcdefghijklmnopqrstuvwxyz"
        return (1..10).map{alfanumerik.random()}.joinToString("")
    }





    //hapus barang
    private fun displayData() {
        val barangList = databaseHelper.getAllData()
        barangAdapter = BarangAdapter(barangList) { id ->
            // Konfirmasi penghapusan
            AlertDialog.Builder(this)
                .setTitle("Hapus Barang")
                .setMessage("Apakah Anda yakin ingin menghapus barang ini?")
                .setPositiveButton("Ya") { _, _ ->
                    val result = databaseHelper.deleteData(id)
                    if (result > 0) {
                        Toast.makeText(this, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        displayData() // Refresh data setelah penghapusan
                    } else {
                        Toast.makeText(this, "Gagal menghapus data!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = barangAdapter
    }
}
