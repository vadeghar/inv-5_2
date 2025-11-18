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
        val vendorName: TextView = view.findViewById(R.id.vendorNameTextView)
        val invoiceNumber: TextView = view.findViewById(R.id.invoiceNumberTextView)
        val date: TextView = view.findViewById(R.id.dateTextView)
        val quantity: TextView = view.findViewById(R.id.quantityTextView)
        val totalAmount: TextView = view.findViewById(R.id.totalAmountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_purchase_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.vendorName.text = p.vendor
        holder.invoiceNumber.text = p.invoiceNo
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.date.text = fmt.format(p.invoiceDate)
        holder.quantity.text = p.totalQty.toString()
        holder.totalAmount.text = String.format(Locale.getDefault(), "₹%.2f", p.totalAmount)
        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Purchase>) {
        items = newItems
        notifyDataSetChanged()
    }
}
