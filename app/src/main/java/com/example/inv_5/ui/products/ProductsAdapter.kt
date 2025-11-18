package com.example.inv_5.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Product
import java.text.SimpleDateFormat
import java.util.Locale

class ProductsAdapter(
    private var items: List<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productNameTextView)
        val barcode: TextView = view.findViewById(R.id.barcodeTextView)
        val mrp: TextView = view.findViewById(R.id.mrpTextView)
        val quantity: TextView = view.findViewById(R.id.quantityTextView)
        val date: TextView = view.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val product = items[position]
        
        holder.productName.text = product.name
        holder.barcode.text = product.barCode
        holder.mrp.text = String.format(Locale.getDefault(), "₹%.2f", product.mrp)
        holder.quantity.text = product.quantityOnHand.toString()
        
        // Show updated date if available, otherwise show added date
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateToShow = product.updatedDt ?: product.addedDt
        val dateLabel = if (product.updatedDt != null) "Updated: " else "Added: "
        holder.date.text = dateLabel + fmt.format(dateToShow)
        
        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
