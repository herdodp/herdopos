import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.toolbar.Barang
import com.example.toolbar.Riwayat

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Kasir.db"
        private const val DATABASE_VERSION = 6

        // Table barang
        const val TABLE_NAME = "Barang"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "nama"
        const val COLUMN_PRICE = "harga"
        const val COLUMN_BARCODE = "idbarcode"
        const val COLUMN_STOK = "stok"

        // Table riwayat transaksi
        const val TABLE_RIWAYAT = "riwayat"
        const val COLUMN_ID_RIWAYAT = "id_riwayat"
        const val COLUMN_NAMA_BARANG = "nama_barang"
        const val COLUMN_QTY_BARANG = "qty_barang"
        const val COLUMN_HARGA_ASLI = "harga_asli"
        const val COLUMN_TOTAL_HARGA = "total_harga"
        const val COLUMN_JAM_RIWAYAT = "jam_riwayat"
        const val COLUMN_TANGGAL_RIWAYAT = "tanggal_riwayat"
        const val COLUMN_NO_STRUK = "nomor_struk"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableBarang = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, $COLUMN_PRICE REAL, $COLUMN_BARCODE TEXT, $COLUMN_STOK INTEGER)")
        db?.execSQL(createTableBarang)

        val createTableRiwayat = ("CREATE TABLE $TABLE_RIWAYAT ($COLUMN_ID_RIWAYAT INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAMA_BARANG TEXT, $COLUMN_QTY_BARANG INTEGER, $COLUMN_HARGA_ASLI REAL, $COLUMN_TOTAL_HARGA REAL, $COLUMN_JAM_RIWAYAT TEXT, $COLUMN_TANGGAL_RIWAYAT TEXT, $COLUMN_NO_STRUK TEXT)")
        db?.execSQL(createTableRiwayat)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RIWAYAT")
        onCreate(db)
    }

    // ============================================== OPEN DATABASE BARANG ===========================================

    // Fungsi untuk menyimpan data ke dalam database barang
    fun insertData(nama: String, harga: Double, idbarcode: String, stokbarang: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, nama)
            put(COLUMN_PRICE, harga)
            put(COLUMN_BARCODE, idbarcode)
            put(COLUMN_STOK, stokbarang)
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

                val barang = Barang(id, nama, harga, idbarcode, stok)
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
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PRICE, COLUMN_BARCODE, COLUMN_STOK),
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
            cursor.close()
            return Barang(id, nama, harga, barcode, stok)
        }

        cursor.close()
        return null
    }

    // ============================================== CLOSE DATABASE BARANG =========================================














    // ============================================== OPEN DATABASE RIWAYAT TRANSAKSI ===================================

    // Fungsi untuk menyimpan data ke dalam database riwayat
    fun insertDatariwayat(nama_barang: String, qty_barang: Int, harga_asli: Double, total_harga: Double, jam_riwayat: String, tanggal_riwayat: String, nomor_struk: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAMA_BARANG, nama_barang)
            put(COLUMN_QTY_BARANG, qty_barang)
            put(COLUMN_HARGA_ASLI, harga_asli)
            put(COLUMN_TOTAL_HARGA, total_harga)
            put(COLUMN_JAM_RIWAYAT, jam_riwayat)
            put(COLUMN_TANGGAL_RIWAYAT, tanggal_riwayat)
            put(COLUMN_NO_STRUK, nomor_struk)
        }
        return db.insert(TABLE_RIWAYAT, null, contentValues)
    }

    // Fungsi untuk mengambil semua data dari database riwayat
    fun getAllDatariwayat(): List<Riwayat> {
        val riwayatList = mutableListOf<Riwayat>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RIWAYAT"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val idriwayat = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_RIWAYAT))
                val namabarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_BARANG))
                val qtybarang = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QTY_BARANG))
                val hargaasli = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HARGA_ASLI))
                val totalharga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_HARGA))
                val jamriwayat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JAM_RIWAYAT))
                val tanggalriwayat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_RIWAYAT))
                val nostruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_STRUK))

                val riwayat = Riwayat(idriwayat, namabarang, qtybarang, hargaasli, totalharga, jamriwayat, tanggalriwayat, nostruk)
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
            arrayOf(COLUMN_ID_RIWAYAT, COLUMN_NAMA_BARANG, COLUMN_QTY_BARANG, COLUMN_HARGA_ASLI, COLUMN_TOTAL_HARGA, COLUMN_JAM_RIWAYAT, COLUMN_TANGGAL_RIWAYAT, COLUMN_NO_STRUK),
            "$COLUMN_NO_STRUK = ?",
            arrayOf(nostruk),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val idriwayat = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_RIWAYAT))
            val namabarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_BARANG))
            val qtybarang = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QTY_BARANG))
            val hargaasli = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HARGA_ASLI))
            val totalharga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_HARGA))
            val jamriwayat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JAM_RIWAYAT))
            val tanggalriwayat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_RIWAYAT))
            val nostruk = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NO_STRUK))
            cursor.close()
            return Riwayat(idriwayat, namabarang, qtybarang, hargaasli, totalharga, jamriwayat, tanggalriwayat, nostruk)
        }

        cursor.close()
        return null
    }

    // ============================================== CLOSE DATABASE RIWAYAT TRANSAKSI ===================================
}
