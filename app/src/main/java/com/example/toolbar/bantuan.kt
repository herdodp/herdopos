package com.example.toolbar

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Suppress("DEPRECATION")
class bantuan : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bantuan2)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // tes cetak struk
        val tescetakstruk = findViewById<TextView>(R.id.tescetakstruk)
        val tescetakstrukhtml = getString(R.string.tescetakstruk)
        tescetakstruk.text = Html.fromHtml(tescetakstrukhtml,Html.FROM_HTML_MODE_LEGACY)


        // pengaturan
        val pengaturan1 = findViewById<TextView>(R.id.pengaturan)
        val pengaturanhtml = getString(R.string.pengaturan)
        pengaturan1.text = Html.fromHtml(pengaturanhtml,Html.FROM_HTML_MODE_LEGACY)


        // tombol manual
        val tombolmanual1 = findViewById<TextView>(R.id.tombolmanual)
        val tombolmanualhtml = getString(R.string.tombolmanual)
        tombolmanual1.text = Html.fromHtml(tombolmanualhtml,Html.FROM_HTML_MODE_LEGACY)


        // tombol clear
        val tombolclear1 = findViewById<TextView>(R.id.tombolclear)
        val tombolclearhtml = getString(R.string.tombolclear)
        tombolclear1.text = Html.fromHtml(tombolclearhtml,Html.FROM_HTML_MODE_LEGACY)


        // tombol daftar barang
        val tomboldaftarbarang1 = findViewById<TextView>(R.id.tomboldaftarbarang)
        val tomboldaftarbaranghtml = getString(R.string.tomboldaftarbarang)
        tomboldaftarbarang1.text = Html.fromHtml(tomboldaftarbaranghtml,Html.FROM_HTML_MODE_LEGACY)


        // tombol riwayat
        val tombolriwayat1 = findViewById<TextView>(R.id.tombolriwayat)
        val tombolriwayathtml = getString(R.string.tombolriwayat)
        tombolriwayat1.text = Html.fromHtml(tombolriwayathtml,Html.FROM_HTML_MODE_LEGACY)



        // tambah barang
        val tambahbarang1 = findViewById<TextView>(R.id.tambahbarang)
        val tambahbaranghtml = getString(R.string.tambahbarang)
        tambahbarang1.text = Html.fromHtml(tambahbaranghtml,Html.FROM_HTML_MODE_LEGACY)




        // download qrcode
        val downloadqrcode1 = findViewById<TextView>(R.id.downloadqrcode)
        val downloadqrcodehtml = getString(R.string.downloadqrcode)
        downloadqrcode1.text = Html.fromHtml(downloadqrcodehtml,Html.FROM_HTML_MODE_LEGACY)


        // scan qrcode
        val scanqrcode1 = findViewById<TextView>(R.id.scanqrcode)
        val scanqrcodehtml = getString(R.string.scanqrcode)
        scanqrcode1.text = Html.fromHtml(scanqrcodehtml,Html.FROM_HTML_MODE_LEGACY)







    }





    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){
        super.onBackPressed()
       finish()
    }
}