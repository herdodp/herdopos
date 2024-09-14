package com.example.toolbar

import DatabaseHelper
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.text.NumberFormat
import java.util.Locale

class BarangAdapter(
    private val barangList: List<Barang>,
    private val onDelete: (Int) -> Unit,
    private val activity: daftarbarang // Tambahkan parameter aktivitas
) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    inner class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaTextView: TextView = itemView.findViewById(R.id.textNama)
        val hargaTextView: TextView = itemView.findViewById(R.id.textHarga)
        val idbarcodetextview: TextView = itemView.findViewById(R.id.idbarcode)
        val jumlahstoktextview: TextView = itemView.findViewById(R.id.stokbarang)
        val btnHapus: Button = itemView.findViewById(R.id.btn_hapus)
        val btnbarcode: Button = itemView.findViewById(R.id.btn_barocde)
        val btntmbahstok: Button = itemView.findViewById(R.id.btntambahstok)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_barang, parent, false)
        return BarangViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = barangList[position]
        holder.namaTextView.text = barang.nama
        holder.hargaTextView.text = formatRupiah(barang.harga)
        holder.jumlahstoktextview.text = barang.stokbarang.toString()

        holder.btnHapus.setOnClickListener {
            onDelete(barang.id)
        }

        holder.btnbarcode.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, view_barcode::class.java)
            intent.putExtra("idbarcode", barang.idbarcode)
            context.startActivity(intent)
        }

        holder.btntmbahstok.setOnClickListener {
            val context = holder.itemView.context
            val inflatestok = LayoutInflater.from(context).inflate(R.layout.view_tambah_stok, null)

            val stokEditText: EditText = inflatestok.findViewById(R.id.jumlahstok)

            val builder = AlertDialog.Builder(context)
            builder.setView(inflatestok)
            builder.setCancelable(false)
            builder.setTitle("Tambah Stok")
            builder.setPositiveButton("Tambah") { _, _ ->
                val tambahanStok = stokEditText.text.toString().toIntOrNull()
                if (tambahanStok != null && tambahanStok > 0) {
                    val databaseHelper = DatabaseHelper(context)
                    val currentStok = barang.stokbarang
                    val updatedStok = currentStok + tambahanStok
                    val result = databaseHelper.updateStok(barang.id, updatedStok)
                    if (result) {
                        Toast.makeText(context, "Stok berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        // Refresh data setelah update
                        activity.displayData() // Memanggil displayData() dari aktivitas
                    } else {
                        Toast.makeText(context, "Gagal memperbarui stok!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Jumlah stok tidak valid!", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        }
    }

    override fun getItemCount(): Int {
        return barangList.size
    }

    private fun formatRupiah(amount: Double): String {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatRupiah.format(amount).replace("Rp", "Rp ")
    }
}
