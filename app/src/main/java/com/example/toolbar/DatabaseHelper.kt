import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.toolbar.Barang

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Kasir.db"
        private const val DATABASE_VERSION = 3
        const val TABLE_NAME = "Barang"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "nama"
        const val COLUMN_PRICE = "harga"
        const val COLUMN_BARCODE = "idbarcode"
        const val COLUMN_STOK = "stok"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, $COLUMN_PRICE REAL, $COLUMN_BARCODE TEXT, $COLUMN_STOK INTEGER)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Fungsi untuk menyimpan data ke dalam database
    fun insertData(nama: String, harga: Double, idbarcode: String, stokbarang: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, nama)
        contentValues.put(COLUMN_PRICE, harga)
        contentValues.put(COLUMN_BARCODE,idbarcode)
        contentValues.put(COLUMN_STOK, stokbarang)
        return db.insert(TABLE_NAME, null, contentValues)
    }

    // Fungsi untuk mengambil semua data dari database
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

    // untuk menghapus data
    fun deleteData(id: Int): Int {
        val db = this.writableDatabase
        return db.delete("barang", "id = ?", arrayOf(id.toString()))
    }


    //menghitung total jumlah data dalam database
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



}
