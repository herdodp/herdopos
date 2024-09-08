package com.example.toolbar

import DatabaseHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Suppress("DEPRECATION")
class riwayattransaksi : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_riwayattransaksi)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val getnostruk = intent.getStringExtra("nostruk")

        val databasehelper = DatabaseHelper(this@riwayattransaksi)

        val nomorStruk = "${getnostruk}"  // Ganti dengan nomor struk yang sesuai
        val riwayat = databasehelper.gettransaksiBykodestruk(nomorStruk)


        Toast.makeText(applicationContext,"no struk : ${getnostruk}",Toast.LENGTH_SHORT).show()
        Toast.makeText(applicationContext,"riwayat nama : ${riwayat?.namabarang}",Toast.LENGTH_SHORT).show()

      






    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this@riwayattransaksi, MainActivity::class.java))
        finish()
        super.onBackPressed()
    }
}