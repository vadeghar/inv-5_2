package com.example.inv_5.ui.purchases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Purchase
import java.text.SimpleDateFormat
import java.util.Locale

class PurchasesAdapter(
    private var items: List<Purchase>,
    private val onClick: (Purchase) -> Unit
) : RecyclerView.Adapter<PurchasesAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.colNumber)
        val vendor: TextView = view.findViewById(R.id.colVendor)
        val invoice: TextView = view.findViewById(R.id.colInvoice)
        val date: TextView = view.findViewById(R.id.colDate)
        val amount: TextView = view.findViewById(R.id.colAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_purchase_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.number.text = p.id
        holder.vendor.text = p.vendor
        holder.invoice.text = p.invoiceNo
        val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        holder.date.text = fmt.format(p.invoiceDate)
        holder.amount.text = String.format(Locale.getDefault(), "%.2f", p.totalAmount)
        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Purchase>) {
        items = newItems
        notifyDataSetChanged()
    }
}
