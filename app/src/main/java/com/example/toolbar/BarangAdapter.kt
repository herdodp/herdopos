package com.example.toolbar

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BarangAdapter(
    private val barangList: List<Barang>,
    private val onDelete: (Int) -> Unit // Tambahkan parameter onDelete
) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    inner class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaTextView: TextView = itemView.findViewById(R.id.textNama)
        val hargaTextView: TextView = itemView.findViewById(R.id.textHarga)
        val idbarcodetextview : TextView = itemView.findViewById(R.id.idbarcode)
        val btnHapus: Button = itemView.findViewById(R.id.btn_hapus)
        val btnbarcode : Button = itemView.findViewById(R.id.btn_barocde)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_barang, parent, false)
        return BarangViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = barangList[position]
        holder.namaTextView.text = barang.nama
        holder.hargaTextView.text = barang.harga.toString()
        holder.idbarcodetextview.text =  barang.idbarcode

        // Callback onDelete untuk menghapus item
        holder.btnHapus.setOnClickListener {
            onDelete(barang.id) // Memanggil fungsi penghapusan
        }

        holder.btnbarcode.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, view_barcode::class.java)
            intent.putExtra("idbarcode", barang.idbarcode)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return barangList.size
    }
}
