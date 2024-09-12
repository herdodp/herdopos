package com.example.toolbar

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class riwayatAdapter(
    private val riwayatList: List<riwayatlist>,
    private val onDelete: (String) -> Unit // Callback untuk penghapusan item menggunakan tipe data String
) : RecyclerView.Adapter<riwayatAdapter.riwayatViewHolder>() {

    inner class riwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nostruk: TextView = itemView.findViewById(R.id.nostruk)
        val jamstruk: TextView = itemView.findViewById(R.id.jamstruk)
        val tanggalstruk: TextView = itemView.findViewById(R.id.tanggalstruk)
        val btnview: ImageButton = itemView.findViewById(R.id.btnview)
        val btndelete: ImageButton = itemView.findViewById(R.id.btndelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): riwayatViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return riwayatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: riwayatViewHolder, position: Int) {
        val riwayatdb = riwayatList[position]
        holder.nostruk.text = riwayatdb.nostruk
        holder.jamstruk.text = riwayatdb.jamstruk
        holder.tanggalstruk.text = riwayatdb.tanggalstruk

        // Fungsi untuk menghapus item saat tombol delete diklik
        holder.btndelete.setOnClickListener {
            onDelete(riwayatdb.nostruk) // Memanggil fungsi penghapusan dengan kode struk
        }

        holder.btnview.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, view_struk::class.java)
            intent.putExtra("viewstruk", riwayatdb.nostruk)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = riwayatList.size
}
