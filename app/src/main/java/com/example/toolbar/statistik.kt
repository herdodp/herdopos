package com.example.toolbar

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.NumberFormat
import java.util.Locale

@Suppress("DEPRECATION")
class statistik : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistik)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val getuangkotor = intent.getStringExtra("uangkotor")

        val viewuangkotor = findViewById<TextView>(R.id.uangkotor)
        viewuangkotor.text = formatRupiahnonrp(getuangkotor?.toInt()!!.toDouble())

















    }

    fun formatRupiahnonrp(amount: Double): String {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val formattedAmount = formatRupiah.format(amount)

        // Menghapus "Rp" dan menghilangkan ",00" di akhir jika ada
        return formattedAmount.replace("Rp", "").replace(",00", "").trim()
    }









    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed()
        finish()
    }

}