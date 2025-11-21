package com.example.inv_5.ui.stockmanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Product
import kotlin.math.abs

class StockProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAdjustClick: (Product) -> Unit
) : ListAdapter<Product, StockProductAdapter.StockProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_product, parent, false)
        return StockProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product, onProductClick, onAdjustClick)
    }

    class StockProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvBarcode: TextView = itemView.findViewById(R.id.tvBarcode)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvLowStockWarning: TextView = itemView.findViewById(R.id.tvLowStockWarning)
        private val viewStockStatus: View = itemView.findViewById(R.id.viewStockStatus)
        private val btnAdjustStock: ImageButton = itemView.findViewById(R.id.btnAdjustStock)

        fun bind(
            product: Product,
            onProductClick: (Product) -> Unit,
            onAdjustClick: (Product) -> Unit
        ) {
            // Set product details
            tvProductName.text = product.name
            tvBarcode.text = "Barcode: ${product.barCode}"
            tvQuantity.text = product.quantityOnHand.toString()
            tvPrice.text = "₱${String.format("%.2f", product.salePrice)}"

            // Set stock status color indicator and low stock warning
            when {
                product.quantityOnHand == 0 -> {
                    // Out of Stock - Red
                    viewStockStatus.setBackgroundColor(
                        itemView.context.getColor(android.R.color.holo_red_dark)
                    )
                    tvLowStockWarning.visibility = View.GONE
                }
                product.quantityOnHand <= product.reorderPoint -> {
                    // Low Stock - Orange
                    viewStockStatus.setBackgroundColor(
                        itemView.context.getColor(android.R.color.holo_orange_dark)
                    )
                    tvLowStockWarning.visibility = View.VISIBLE
                    tvLowStockWarning.text = "⚠ Low Stock Alert (Reorder Point: ${product.reorderPoint})"
                }
                else -> {
                    // In Stock - Green
                    viewStockStatus.setBackgroundColor(
                        itemView.context.getColor(android.R.color.holo_green_dark)
                    )
                    tvLowStockWarning.visibility = View.GONE
                }
            }

            // Click listeners
            itemView.setOnClickListener {
                onProductClick(product)
            }

            btnAdjustStock.setOnClickListener {
                onAdjustClick(product)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
