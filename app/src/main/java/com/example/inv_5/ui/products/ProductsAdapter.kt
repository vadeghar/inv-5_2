package com.example.inv_5.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Product
import java.util.Locale

class ProductsAdapter(
    private var items: List<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val id: TextView = view.findViewById(R.id.idTextView)
        val productName: TextView = view.findViewById(R.id.productNameTextView)
        val barcode: TextView = view.findViewById(R.id.barcodeTextView)
        val mrp: TextView = view.findViewById(R.id.mrpTextView)
        val quantity: TextView = view.findViewById(R.id.quantityTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val product = items[position]
        
        // Show truncated ID (first 6 chars)
        holder.id.text = if (product.id.length > 6) {
            product.id.substring(0, 6) + "..."
        } else {
            product.id
        }
        
        holder.productName.text = product.name
        holder.barcode.text = product.barCode
        holder.mrp.text = String.format(Locale.getDefault(), "₹%.2f", product.mrp)
        holder.quantity.text = product.quantityOnHand.toString()
        
        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
