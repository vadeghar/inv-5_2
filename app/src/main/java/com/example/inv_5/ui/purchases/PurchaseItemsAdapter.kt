package com.example.inv_5.ui.purchases

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.data.entities.PurchaseItem
import com.example.inv_5.databinding.ItemPurchaseBinding

class PurchaseItemsAdapter(private val items: MutableList<PurchaseItem>) :
    RecyclerView.Adapter<PurchaseItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPurchaseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.serialTextView.text = (position + 1).toString()
    // use snapshot fields populated at create-time
    holder.binding.productNameTextView.text = item.productName.ifEmpty { item.hsn }
    holder.binding.barcodeTextView.text = item.productBarcode
    holder.binding.quantityTextView.text = item.quantity.toString()
    holder.binding.mrpTextView.text = String.format(java.util.Locale.getDefault(), "%.2f", item.mrp)
    holder.binding.salePriceTextView.text = String.format(java.util.Locale.getDefault(), "%.2f", item.rate)
    }

    override fun getItemCount() = items.size

    fun addItem(item: PurchaseItem) {
        val pos = items.size
        items.add(item)
        notifyItemInserted(pos)
    }

    fun replaceAll(newItems: List<PurchaseItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemPurchaseBinding) : RecyclerView.ViewHolder(binding.root)
}