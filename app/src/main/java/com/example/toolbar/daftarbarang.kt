package com.example.toolbar

import DatabaseHelper
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private var barangList: List<Barang> = listOf()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_daftarbarang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btntambahbarang = findViewById<Button>(R.id.buttonaddbarang)
        btntambahbarang.setOnClickListener {
            val inflatedaftar = LayoutInflater.from(this)
            val viewdaftar = inflatedaftar.inflate(R.layout.addbarang, null)

            AlertDialog.Builder(this)
                .setTitle("Tambah Barang")
                .setView(viewdaftar)
                .setCancelable(false)
                .setPositiveButton("Simpan") { _, _ ->
                    val editTextNama: EditText = viewdaftar.findViewById(R.id.namabarang)
                    val editTextHarga: EditText = viewdaftar.findViewById(R.id.hargabarang)
                    val editTextHargapokok: EditText = viewdaftar.findViewById(R.id.hargabarangpokok)
                    val jumlahstok: EditText = viewdaftar.findViewById(R.id.stokbarang)
                    val idbarcode = idalfanumerik()

                    val nama = editTextNama.text.toString()
                    val hargaText = editTextHarga.text.toString()
                    val hargaTextpokok = editTextHargapokok.text.toString()
                    val stokText = jumlahstok.text.toString()

                    if (nama.isNotEmpty() && hargaText.isNotEmpty()  && hargaTextpokok.isNotEmpty() && stokText.isNotEmpty()) {
                        val harga = hargaText.toDoubleOrNull()
                        val barangstok = stokText.toIntOrNull()
                        val hargapokok = hargaTextpokok.toInt()

                        if (harga != null && barangstok != null) {
                            val result = databaseHelper.insertData(nama, harga, idbarcode, barangstok, hargapokok)
                            if (result != -1L) {
                                Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                // Refresh data setelah penyimpanan
                                displayData()
                            } else {
                                Toast.makeText(this, "Gagal menyimpan data!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Harga atau stok tidak valid!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Isi semua kolom dengan benar!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }

        recyclerView = findViewById(R.id.recyclerView)
        databaseHelper = DatabaseHelper(this)

        val totalBarang = databaseHelper.getTotalJumlahBarang()
        val jumlahbarang = findViewById<TextView>(R.id.jumlahbarang)
        jumlahbarang.text = totalBarang.toString()

        displayData()

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterData(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterData(it) }
                return true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_daftarbarang, menu)
        return true
    }

    private fun idalfanumerik(): String {
        val alfanumerik = "0123456789abcdefghijklmnopqrstuvwxyz"
        return (1..10).map { alfanumerik.random() }.joinToString("")
    }

    fun displayData() {
        barangList = databaseHelper.getAllData()
        barangAdapter = BarangAdapter(barangList, { id ->
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
        }, this) // Pass activity as argument
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = barangAdapter
    }

    private fun filterData(query: String) {
        val filteredList = barangList.filter {
            it.nama.contains(query, ignoreCase = true) || it.idbarcode.contains(query, ignoreCase = true)
        }
        barangAdapter = BarangAdapter(filteredList, { id ->
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
        }, this) // Pass activity as argument
        recyclerView.adapter = barangAdapter
    }
}
