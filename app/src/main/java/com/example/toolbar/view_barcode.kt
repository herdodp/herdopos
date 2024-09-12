package com.example.toolbar

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class view_barcode : AppCompatActivity() {

    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_barcode)

        // Inisialisasi ImageView dan Button
        val imageView = findViewById<ImageView>(R.id.imageView)
        val downloadButton = findViewById<Button>(R.id.btnDownload)

        // Ambil ID barcode dari Intent
        val getidbarcode = intent.getStringExtra("idbarcode") ?: "default"

        try {
            bitmap = generateQRCode(getidbarcode)
            imageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal membuat QR code", Toast.LENGTH_SHORT).show()
        }

        downloadButton.setOnClickListener {
            // Cek izin penyimpanan
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Cek koneksi internet sebelum menyimpan
                if (isInternetAvailable()) {
                    saveQRCodeToGallery()
                } else {
                    Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Minta izin penyimpanan
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    private fun generateQRCode(data: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 600, 600)
        return BarcodeEncoder().createBitmap(bitMatrix)
    }

    private fun saveQRCodeToGallery() {
        val filename = "QR_Code_${System.currentTimeMillis()}.jpg"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Kondisi untuk Android 10 (API 29) ke atas
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/QR_Code")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    val fos = resolver.openOutputStream(imageUri)
                    fos?.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                        Toast.makeText(this, "QR code berhasil disimpan ke galeri", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    throw Exception("Gagal membuat URI gambar")
                }
            } else {
                // Kondisi untuk Android di bawah 10
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val qrCodeDir = File(picturesDir, "QR_Code")
                if (!qrCodeDir.exists()) qrCodeDir.mkdirs()

                val file = File(qrCodeDir, filename)
                val fos = FileOutputStream(file)

                fos.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    Toast.makeText(this, "QR code berhasil disimpan ke galeri", Toast.LENGTH_SHORT).show()
                }

                // Scan file agar muncul di galeri
                MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, file.name)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Cek izin yang diberikan oleh pengguna
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("QRCode", "Izin diberikan oleh pengguna")
                if (isInternetAvailable()) {
                    saveQRCodeToGallery()
                } else {
                    Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("QRCode", "Izin ditolak oleh pengguna")
                Toast.makeText(this, "Izin diperlukan untuk menyimpan gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk cek koneksi internet
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}
