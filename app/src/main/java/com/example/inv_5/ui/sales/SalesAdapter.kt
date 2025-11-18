package com.example.inv_5.ui.sales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Sale
import java.text.SimpleDateFormat
import java.util.Locale

class SalesAdapter(
    private var items: List<Sale>,
    private val onClick: (Sale) -> Unit
) : RecyclerView.Adapter<SalesAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val customerName: TextView = view.findViewById(R.id.customerNameTextView)
        val invoiceLayout: LinearLayout = view.findViewById(R.id.invoiceLayout)
        val invoiceNumber: TextView = view.findViewById(R.id.invoiceNumberTextView)
        val date: TextView = view.findViewById(R.id.dateTextView)
        val quantity: TextView = view.findViewById(R.id.quantityTextView)
        val totalAmount: TextView = view.findViewById(R.id.totalAmountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sale_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.customerName.text = s.customerName
        
        // Handle invoice number - hide layout if empty
        // Since Sale entity doesn't have invoiceNo field, we'll hide it for now
        holder.invoiceLayout.visibility = View.GONE
        
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.date.text = fmt.format(s.saleDate)
        holder.quantity.text = s.totalQty.toString()
        holder.totalAmount.text = String.format(Locale.getDefault(), "₹%.2f", s.totalAmount)
        holder.itemView.setOnClickListener { onClick(s) }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Sale>) {
        items = newItems
        notifyDataSetChanged()
    }
}
