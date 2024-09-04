package com.example.toolbar


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text

@Suppress("DEPRECATION")
class hasilscanner : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasilscanner)


        val intent = intent
        val barcodeResult = intent.getStringExtra("barcode_result")
        val hasilscan = findViewById<TextView>(R.id.hasilscan)
        hasilscan.text = barcodeResult

        Toast.makeText(applicationContext, "hasil scan : ${barcodeResult}", Toast.LENGTH_SHORT).show()




















    }



    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
       startActivity(Intent(this, BarcodeScannerActivity::class.java))
        super.onBackPressed()


    }


}
