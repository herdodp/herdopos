package com.example.toolbar

import DatabaseHelper
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class view_struk : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_struk)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mengambil kode struk dari Intent
        val getviewstruk = intent.getStringExtra("viewstruk")

        // Mendapatkan data struk dari database
        val databasehelper = DatabaseHelper(this@view_struk)
        val riwayatStruk = databasehelper.gettransaksiBykodestruk(getviewstruk.toString())

        // Jika data struk ditemukan, tampilkan di TextView
        if (riwayatStruk != null) {
            // Menampilkan hanya stringstrukdb di TextView
            val viewstruk1 = findViewById<TextView>(R.id.viewstruk)
            viewstruk1.text = riwayatStruk.stringstrukdb
        } else {
            Toast.makeText(this, "Struk tidak ditemukan", Toast.LENGTH_SHORT).show()
        }




    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        //super.onBackPressed()
        finish()
    }

}