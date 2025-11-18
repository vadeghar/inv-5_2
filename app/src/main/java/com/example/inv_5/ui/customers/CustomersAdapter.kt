package com.example.inv_5.ui.customers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.R
import com.example.inv_5.data.entities.Customer

class CustomersAdapter(
    private val onEditClick: (Customer) -> Unit,
    private val onDeleteClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomersAdapter.CustomerViewHolder>() {

    private var customers = listOf<Customer>()

    class CustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerName: TextView = view.findViewById(R.id.customerNameTextView)
        val contactPerson: TextView = view.findViewById(R.id.contactPersonTextView)
        val phone: TextView = view.findViewById(R.id.phoneTextView)
        val email: TextView = view.findViewById(R.id.emailTextView)
        val address: TextView = view.findViewById(R.id.addressTextView)
        val status: TextView = view.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_card, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]
        
        holder.customerName.text = customer.name
        holder.contactPerson.text = customer.contactPerson ?: "N/A"
        holder.phone.text = customer.phone ?: "N/A"
        holder.email.text = customer.email ?: "N/A"
        holder.address.text = customer.address ?: "N/A"
        holder.status.text = if (customer.isActive) "Active" else "Inactive"
    }

    override fun getItemCount(): Int = customers.size

    fun setItems(newCustomers: List<Customer>) {
        customers = newCustomers
        notifyDataSetChanged()
    }
}
