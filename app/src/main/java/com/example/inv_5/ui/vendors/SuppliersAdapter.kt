package com.example.inv_5.ui.vendors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Supplier
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

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
        val status: Chip = view.findViewById(R.id.statusTextView)
        val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_supplier, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val supplier = items[position]
        
        holder.supplierName.text = supplier.name
        holder.contactPerson.text = if (!supplier.contactPerson.isNullOrEmpty()) {
            "Contact: ${supplier.contactPerson}"
        } else {
            "Contact: N/A"
        }
        holder.phone.text = if (!supplier.phone.isNullOrEmpty()) {
            "Phone: ${supplier.phone}"
        } else {
            "Phone: N/A"
        }
        holder.email.text = if (!supplier.email.isNullOrEmpty()) {
            "Email: ${supplier.email}"
        } else {
            "Email: N/A"
        }
        holder.address.text = if (!supplier.address.isNullOrEmpty()) {
            "Address: ${supplier.address}"
        } else {
            "Address: N/A"
        }
        
        holder.status.text = if (supplier.isActive) "Active" else "Inactive"
        holder.status.setChipBackgroundColorResource(
            if (supplier.isActive) R.color.purple_200 else android.R.color.darker_gray
        )
        
        holder.btnEdit.setOnClickListener { onEditClick(supplier) }
        holder.btnDelete.setOnClickListener { onDeleteClick(supplier) }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Supplier>) {
        items = newItems
        notifyDataSetChanged()
    }
}
