package com.example.inv_5.ui.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.inv_5.data.entities.Customer
import com.example.inv_5.databinding.ItemCustomerBinding

class CustomersAdapter(
    private val onEditClick: (Customer) -> Unit,
    private val onDeleteClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomersAdapter.CustomerViewHolder>() {

    private var customers = listOf<Customer>()

    inner class CustomerViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: Customer) {
            binding.apply {
                tvCustomerName.text = customer.name
                tvContactPerson.text = "Contact: ${customer.contactPerson ?: "N/A"}"
                tvPhone.text = "Phone: ${customer.phone ?: "N/A"}"
                tvEmail.text = "Email: ${customer.email ?: "N/A"}"
                tvAddress.text = "Address: ${customer.address ?: "N/A"}"
                
                chipStatus.text = if (customer.isActive) "Active" else "Inactive"
                chipStatus.setChipBackgroundColorResource(
                    if (customer.isActive) android.R.color.holo_green_dark
                    else android.R.color.darker_gray
                )

                btnEdit.setOnClickListener {
                    onEditClick(customer)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(customer)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(customers[position])
    }

    override fun getItemCount(): Int = customers.size

    fun setItems(newCustomers: List<Customer>) {
        customers = newCustomers
        notifyDataSetChanged()
    }
}
