import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.toolbar.R

class ScanResultAdapter(private val scanResults: List<com.example.toolbar.ScanResult>) :
    RecyclerView.Adapter<ScanResultAdapter.ScanResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_result, parent, false)
        return ScanResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
        val result = scanResults[position]
        holder.bind(result)
    }

    override fun getItemCount() = scanResults.size

    class ScanResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textScanResult: TextView = itemView.findViewById(R.id.text_scan_result)
        private val qtyScanResult: TextView = itemView.findViewById(R.id.qty_scan_result)
        private val hargaScanResult: TextView = itemView.findViewById(R.id.harga_scan_result)

        fun bind(scanResult: com.example.toolbar.ScanResult) {
            // Gunakan informasi dari ScanResult yang relevan
            textScanResult.text = scanResult.text
            qtyScanResult.text = scanResult.qty
            hargaScanResult.text = scanResult.harga
        }
    }
}
