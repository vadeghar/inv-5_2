package com.example.inv_5.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.data.entities.SaleItem
import com.example.inv_5.databinding.ItemSaleBinding

class SaleItemsAdapter(private val items: MutableList<SaleItem>) :
    RecyclerView.Adapter<SaleItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSaleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.serialTextView.text = (position + 1).toString()
        holder.binding.productNameTextView.text = item.productName.ifEmpty { item.hsn }
        holder.binding.quantityTextView.text = item.quantity.toString()
        holder.binding.mrpTextView.text = String.format(java.util.Locale.getDefault(), "%.2f", item.mrp)
        holder.binding.salePriceTextView.text = String.format(java.util.Locale.getDefault(), "%.2f", item.salePrice)
    }

    override fun getItemCount() = items.size

    fun addItem(item: SaleItem) {
        val pos = items.size
        items.add(item)
        notifyItemInserted(pos)
    }

    fun replaceAll(newItems: List<SaleItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemSaleBinding) : RecyclerView.ViewHolder(binding.root)
}
