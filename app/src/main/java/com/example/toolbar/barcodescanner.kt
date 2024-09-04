@file:Suppress("DEPRECATION")

package com.example.toolbar

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class BarcodeScannerActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 1

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barcode)



        // Mengunci orientasi ke potret
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Memeriksa izin kamera
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Jika izin belum diberikan, minta izin
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            // Izin kamera sudah diberikan, langsung mulai pemindaian barcode
            startBarcodeScanner()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan, mulai pemindaian barcode
                startBarcodeScanner()
            } else {
                // Izin kamera ditolak, tindakan yang sesuai dapat diambil di sini
                // Misalnya, tampilkan pesan kepada pengguna bahwa izin diperlukan
            }
        }
    }

    private fun startBarcodeScanner() {
        // Izin kamera diberikan atau sudah ada, lakukan pemindaian
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(true)
        integrator.setPrompt("") // Anda bisa menambahkan teks prompt jika diperlukan
        integrator.setCameraId(0) // Kamera belakang
        integrator.initiateScan()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val integratorResult: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (integratorResult != null) {
            if (integratorResult.contents != null) {
                // Pemindaian berhasil, result.contents berisi nilai barcode
                val scannedData = integratorResult.contents
                val countview = "1"

                // Mengirim hasil pemindaian ke HalamanLainActivity
                val intent = Intent(this, hasilscanner::class.java)
                intent.putExtra("barcode_result", scannedData)
                intent.putExtra("countview", countview)
                startActivity(intent)
            } else {
                // Pemindaian dibatalkan atau tidak ada barcode yang ditemukan
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }
}
