import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.toolbar.Barang

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Kasir.db"
        private const val DATABASE_VERSION = 5

        //table barang
        const val TABLE_NAME = "Barang"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "nama"
        const val COLUMN_PRICE = "harga"
        const val COLUMN_BARCODE = "idbarcode"
        const val COLUMN_STOK = "stok"

        //table riwayat transaksi
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
        val createTable = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, $COLUMN_PRICE REAL, $COLUMN_BARCODE TEXT, $COLUMN_STOK INTEGER)")
        db?.execSQL(createTable)

        val createTableriwayat = ("CREATE TABLE $TABLE_RIWAYAT ($COLUMN_ID_RIWAYAT INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAMA_BARANG TEXT, $COLUMN_QTY_BARANG INTEGER, $COLUMN_HARGA_ASLI REAL, $COLUMN_TOTAL_HARGA REAL, $COLUMN_JAM_RIWAYAT TEXT, $COLUMN_TANGGAL_RIWAYAT TEXT, $COLUMN_NO_STRUK TEXT)")
        db?.execSQL(createTableriwayat)

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
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, nama)
        contentValues.put(COLUMN_PRICE, harga)
        contentValues.put(COLUMN_BARCODE,idbarcode)
        contentValues.put(COLUMN_STOK, stokbarang)
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

    // untuk menghapus data di database barang
    fun deleteData(id: Int): Int {
        val db = this.writableDatabase
        return db.delete("barang", "id = ?", arrayOf(id.toString()))
    }


    //menghitung total jumlah data dalam database barang
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
            "idbarcode = ?",
            arrayOf(barcode),
            null, null, null,
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            val nama = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            val harga = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE))
            val barcode = cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE))
            val stok = cursor.getInt(cursor.getColumnIndex(COLUMN_STOK))
            cursor.close()
            db.close()
            return Barang(id, nama, harga,barcode, stok )
        }

        cursor.close()
        db.close()
        return null
    }

    //==============================================   CLOSE DATABASE BARANG =========================================













    //============================================= OPEN DATABASE RIWAYAT TRANSAKSI ===================================

    //nama_barang, qty_barang, harga_asli, total_harga, jam_riwayat, tanggal_riwayat, nomor_struk

    //insert data riwayat
    fun insertDatariwayat(nama: String, harga: Double, idbarcode: String, stokbarang: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, nama)
        contentValues.put(COLUMN_PRICE, harga)
        contentValues.put(COLUMN_BARCODE,idbarcode)
        contentValues.put(COLUMN_STOK, stokbarang)
        return db.insert(TABLE_RIWAYAT, null, contentValues)
    }




    // Fungsi untuk mengambil semua data dari database riwayat
    fun getAllDatariwayat(): List<Barang> {
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




    // Method to get Barang by barcode di database struk
    @SuppressLint("Range")
    fun getBarangBykodestruk(barcode: String): Barang? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PRICE, COLUMN_BARCODE, COLUMN_STOK),
            "idbarcode = ?",
            arrayOf(barcode),
            null, null, null,
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            val nama = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            val harga = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE))
            val barcode = cursor.getString(cursor.getColumnIndex(COLUMN_BARCODE))
            val stok = cursor.getInt(cursor.getColumnIndex(COLUMN_STOK))
            cursor.close()
            db.close()
            return Barang(id, nama, harga,barcode, stok )
        }

        cursor.close()
        db.close()
        return null
    }



    //============================================= CLOSE DATABASE RIWAYAT TRANSAKSI ===================================







}
