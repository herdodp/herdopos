package com.example.toolbar

import DatabaseHelper
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@Suppress("DEPRECATION")
class riwayattransaksi : AppCompatActivity() {

    private lateinit var riwayatAdapter: riwayatAdapter
    private lateinit var recyclerViewRiwayat: RecyclerView
    private lateinit var databaseHelper: DatabaseHelper
    private var riwayatList: List<riwayatlist> = listOf() // Ganti dengan tipe data riwayatlist

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_riwayattransaksi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerViewRiwayat = findViewById(R.id.recyclerViewriwayat)

        // Inisiasi DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        displayData()

        // Inisialisasi dan setel SearchView
        val searchView: SearchView = findViewById(R.id.searchViewriwayat)
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

    private fun displayData() {
        riwayatList = databaseHelper.getAllDatariwayat()

        // Inisialisasi adapter dengan data
        riwayatAdapter = riwayatAdapter(riwayatList) { id ->
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

        recyclerViewRiwayat.layoutManager = LinearLayoutManager(this)
        recyclerViewRiwayat.adapter = riwayatAdapter
    }

    private fun filterData(query: String) {
        val filteredList = riwayatList.filter {
            it.nostruk.contains(query, ignoreCase = true) || it.jamstruk.contains(query, ignoreCase = true)
        }

        // Update adapter dengan data yang sudah difilter
        riwayatAdapter = riwayatAdapter(filteredList) { id ->
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
        recyclerViewRiwayat.adapter = riwayatAdapter
    }


    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this@riwayattransaksi, MainActivity::class.java))
        finish()
        //super.onBackPressed()
    }




}
