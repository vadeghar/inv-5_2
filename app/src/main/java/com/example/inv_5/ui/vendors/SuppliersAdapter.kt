package com.example.inv_5.ui.vendors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Supplier


class SuppliersAdapter(
    private var items: List<Supplier>,
    private val onEditClick: (Supplier) -> Unit,
    private val onDeleteClick: (Supplier) -> Unit
) : RecyclerView.Adapter<SuppliersAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val supplierName: TextView = view.findViewById(R.id.supplierNameTextView)
        val contactPerson: TextView = view.findViewById(R.id.contactPersonTextView)
        val phone: TextView = view.findViewById(R.id.phoneTextView)
        val email: TextView = view.findViewById(R.id.emailTextView)
        val address: TextView = view.findViewById(R.id.addressTextView)
        val status: TextView = view.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_supplier_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val supplier = items[position]
        
        holder.supplierName.text = supplier.name
        holder.contactPerson.text = supplier.contactPerson ?: "N/A"
        holder.phone.text = supplier.phone ?: "N/A"
        holder.email.text = supplier.email ?: "N/A"
        holder.address.text = supplier.address ?: "N/A"
        holder.status.text = if (supplier.isActive) "Active" else "Inactive"
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Supplier>) {
        items = newItems
        notifyDataSetChanged()
    }
}
