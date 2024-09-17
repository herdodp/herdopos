import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.toolbar.Barang
import com.example.toolbar.Riwayat
import com.example.toolbar.riwayatlist

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Kasir.db"
        private const val DATABASE_VERSION = 16

        // Table barang
        const val TABLE_NAME = "Barang"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "nama"
        const val COLUMN_PRICE = "harga"
        const val COLUMN_BARCODE = "idbarcode"
        const val COLUMN_STOK = "stok"
        const val COLUMN_POKOK = "harga_pokok"
        const val COLUMN_TERJUAL = "terjual"

        // Table riwayat transaksi
        const val TABLE_RIWAYAT = "riwayat"
        const val COLUMN_ID_STRUK = "id_struk"
        const val COLUMN_STRING_STRUK = "stringstruk"
        const val COLUMN_NO_STRUK = "nostruk"
        const val COLUMN_JAM_STRUK= "jamstruk"
        const val COLUMN_TANGGAL_STRUK= "tanggakstruk"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableBarang = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, $COLUMN_PRICE REAL, $COLUMN_BARCODE TEXT, $COLUMN_STOK INTEGER DEFAULT 0, $COLUMN_POKOK INTEGER DEFAULT 0, $COLUMN_TERJUAL INTEGER DEFAULT 0)")
        db?.execSQL(createTableBarang)

        val createTableRiwayat = ("CREATE TABLE $TABLE_RIWAYAT ($COLUMN_ID_STRUK INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_STRING_STRUK TEXT, $COLUMN_NO_STRUK TEXT, $COLUMN_JAM_STRUK TEXT, $COLUMN_TANGGAL_STRUK TEXT)")
        db?.execSQL(createTableRiwayat)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RIWAYAT")
        onCreate(db)
    }

    // ============================================== OPEN DATABASE BARANG ===========================================

    // Fungsi untuk menyimpan data ke dalam database barang
    fun insertData(nama: String, harga: Double, idbarcode: String, stokbarang: Int, harga_pokok: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, nama)
            put(COLUMN_PRICE, harga)
            put(COLUMN_BARCODE, idbarcode)
            put(COLUMN_STOK, stokbarang)
            put(COLUMN_POKOK, harga_pokok)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    // Fungsi untuk mengambil semua data dari database barang
    fun getAllData(): List<Barang> {
        val barangList = mutableListOf<Barang>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val idbarcode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE))
                val stok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOK))
                val hargapokok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POKOK))

                val barang = Barang(id, nama, harga, idbarcode, stok, hargapokok)
                barangList.add(barang)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return barangList
    }



    // Untuk menghapus data di database barang
    fun deleteData(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }



    // Menghitung total jumlah data dalam database barang
    fun getTotalJumlahBarang(): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }



    // Method to get Barang by barcode di database barang
    @SuppressLint("Range")
    fun getBarangByBarcode(barcode: String): Barang? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PRICE, COLUMN_BARCODE, COLUMN_STOK, COLUMN_POKOK),
            "$COLUMN_BARCODE = ?",
            arrayOf(barcode),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
            val barcode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE))
            val stok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOK))
            val hargapokok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POKOK))

            cursor.close()
            return Barang(id, nama, harga, barcode, stok, hargapokok)
        }

        cursor.close()
        return null
    }





    //get data by nama barang
    @SuppressLint("Range")
    fun getBarangBynamabarang(namabarang: String): Barang? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PRICE, COLUMN_BARCODE, COLUMN_STOK, COLUMN_POKOK),
            "$COLUMN_NAME = ?",
            arrayOf(namabarang),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
            val barcode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BARCODE))
            val stok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOK))
            val hargapokok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POKOK))

            cursor.close()
            return Barang(id, nama, harga, barcode, stok, hargapokok)
        }

        cursor.close()
        return null
    }






    fun updateStokBarang(barcode: String, qty: Int): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT stok FROM $TABLE_NAME WHERE $COLUMN_NAME = ?", arrayOf(barcode))

        return if (cursor.moveToFirst()) {
            val currentStok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOK))
            val newStok = currentStok - qty

            if (newStok >= 0) {
                val contentValues = ContentValues().apply {
                    put(COLUMN_STOK, newStok)
                }
                val rowsAffected = db.update(TABLE_NAME, contentValues, "$COLUMN_NAME = ?", arrayOf(barcode))
                cursor.close()
                rowsAffected > 0
            } else {
                cursor.close()
                false // Stok tidak mencukupi
            }
        } else {
            cursor.close()
            false // Barang tidak ditemukan
        }
    }






    // Method untuk memperbarui stok
    fun updateStok(id: Int, tambahanStok: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_STOK, tambahanStok)
        }
        val result = db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
        return result > 0
    }


    




    // ============================================== CLOSE DATABASE BARANG =========================================




















    // ============================================== OPEN DATABASE RIWAYAT TRANSAKSI ===================================

    // Fungsi untuk menyimpan data ke dalam database riwayat
    fun insertDatariwayat(stringstruk: String, nostruk: String, jamstruk: String, tanggaltruk: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_STRING_STRUK, stringstruk)
            put(COLUMN_NO_STRUK, nostruk)
            put(COLUMN_JAM_STRUK, jamstruk)
            put(COLUMN_TANGGAL_STRUK, tanggaltruk)
        }
        val result = db.insert(TABLE_RIWAYAT, null, contentValues)
        db.close()
        return result
    }





    // Fungsi untuk mengambil semua data dari database riwayat
    fun getAllDatariwayat(): List<riwayatlist> {
        val riwayatList = mutableListOf<riwayatlist>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RIWAYAT"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val nostruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_STRUK))
                val jamstruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JAM_STRUK))
                val tanggalstruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_STRUK))

                // Gunakan riwayatlist sebagai tipe data yang benar
                val riwayat = riwayatlist(nostruk, jamstruk, tanggalstruk)
                riwayatList.add(riwayat)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return riwayatList
    }





    // Method to get Riwayat by nomor struk di database riwayat
    @SuppressLint("Range")
    fun gettransaksiBykodestruk(nostruk: String): Riwayat? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RIWAYAT,
            arrayOf(COLUMN_ID_STRUK, COLUMN_STRING_STRUK, COLUMN_NO_STRUK, COLUMN_JAM_STRUK, COLUMN_TANGGAL_STRUK),
            "$COLUMN_NO_STRUK = ?",
            arrayOf(nostruk),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val idstruk = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_STRUK))
            val stringstruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STRING_STRUK))
            val jamstruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JAM_STRUK))
            val tanggalstruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_STRUK))

            cursor.close()
            return Riwayat(idstruk, stringstruk, nostruk, jamstruk, tanggalstruk)
        }

        cursor.close()
        return null
    }

    fun deleteDatariwayat(nostruk: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_RIWAYAT, "$COLUMN_NO_STRUK = ?", arrayOf(nostruk))
    }


    // ============================================== CLOSE DATABASE RIWAYAT TRANSAKSI ===================================
}
