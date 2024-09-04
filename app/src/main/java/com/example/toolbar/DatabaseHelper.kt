import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.toolbar.Barang

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Kasir.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "Barang"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "nama"
        const val COLUMN_PRICE = "harga"
        const val COLUMN_BARCODE = "idbarcode"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, $COLUMN_PRICE REAL, $COLUMN_BARCODE TEXT)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Fungsi untuk menyimpan data ke dalam database
    fun insertData(nama: String, harga: Double, idbarcode: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, nama)
        contentValues.put(COLUMN_PRICE, harga)
        contentValues.put(COLUMN_BARCODE,idbarcode)
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

                val barang = Barang(id, nama, harga, idbarcode)
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



}
