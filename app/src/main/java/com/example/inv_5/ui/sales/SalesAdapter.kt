package com.example.inv_5.ui.sales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val number: TextView = view.findViewById(R.id.colNumber)
        val customer: TextView = view.findViewById(R.id.colCustomer)
        val phone: TextView = view.findViewById(R.id.colPhone)
        val date: TextView = view.findViewById(R.id.colDate)
        val amount: TextView = view.findViewById(R.id.colAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sale_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.number.text = s.id
        holder.customer.text = s.customerName
        holder.phone.text = s.customerPhone
        val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        holder.date.text = fmt.format(s.saleDate)
        holder.amount.text = String.format(Locale.getDefault(), "%.2f", s.totalAmount)
        holder.itemView.setOnClickListener { onClick(s) }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Sale>) {
        items = newItems
        notifyDataSetChanged()
    }
}
