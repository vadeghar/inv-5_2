package com.example.inv_5.ui.customers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Customer
import com.example.inv_5.data.repository.ActivityLogRepository
import com.example.inv_5.databinding.DialogAddEditCustomerBinding
import com.example.inv_5.databinding.FragmentCustomersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var customersAdapter: CustomersAdapter
    private val customerDao by lazy { DatabaseProvider.getInstance(requireContext()).customerDao() }
    private val activityLogRepo by lazy { ActivityLogRepository(requireContext()) }
    
    private var currentPage = 0
    private val pageSize = 20
    private var totalPages = 1
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchBar()
        setupPagination()
        setupFab()
        
        loadPage()
    }

    private fun setupRecyclerView() {
        customersAdapter = CustomersAdapter(
            onEditClick = { customer -> showEditCustomerDialog(customer) },
            onDeleteClick = { customer -> confirmDeleteCustomer(customer) }
        )
        binding.customersRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.customersRecyclerView.adapter = customersAdapter
    }

    private fun setupSearchBar() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                currentPage = 0
                loadPage()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                currentPage = 0
                loadPage()
                return true
            }
        })
    }

    private fun setupPagination() {
        binding.prevButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                loadPage()
            }
        }

        binding.nextButton.setOnClickListener {
            if (currentPage < totalPages - 1) {
                currentPage++
                loadPage()
            }
        }
    }

    private fun setupFab() {
        binding.fabAddCustomer.setOnClickListener {
            showAddCustomerDialog()
        }
    }

    private fun loadPage() {
        lifecycleScope.launch {
            val customers = withContext(Dispatchers.IO) {
                customerDao.searchCustomers(
                    searchQuery = currentSearchQuery,
                    limit = pageSize,
                    offset = currentPage * pageSize
                )
            }

            customersAdapter.setItems(customers)
            
            // Update pagination
            totalPages = if (customers.size < pageSize) currentPage + 1 else currentPage + 2
            
            // Disable/enable pagination buttons
            if (customers.isEmpty() && currentPage > 0) {
                currentPage--
                Toast.makeText(requireContext(), "No more records", Toast.LENGTH_SHORT).show()
                return@launch
            }
        }
    }

    private fun showAddCustomerDialog() {
        val dialogBinding = DialogAddEditCustomerBinding.inflate(layoutInflater)
        
        dialogBinding.dialogTitle.text = "Add Customer"
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etCustomerName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Customer name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val customer = Customer(
                id = UUID.randomUUID().toString(),
                name = name,
                contactPerson = dialogBinding.etContactPerson.text.toString().trim().ifEmpty { null },
                phone = dialogBinding.etPhone.text.toString().trim().ifEmpty { null },
                email = dialogBinding.etEmail.text.toString().trim().ifEmpty { null },
                address = dialogBinding.etAddress.text.toString().trim().ifEmpty { null },
                isActive = dialogBinding.switchActive.isChecked,
                addedDt = Date(),
                updatedDt = Date()
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    customerDao.insertCustomer(customer)
                    activityLogRepo.logCustomerAdded(customer)
                }
                Toast.makeText(requireContext(), "Customer added successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadPage()
            }
        }

        dialog.show()
    }

    private fun showEditCustomerDialog(customer: Customer) {
        val dialogBinding = DialogAddEditCustomerBinding.inflate(layoutInflater)
        
        dialogBinding.dialogTitle.text = "Edit Customer"
        dialogBinding.etCustomerName.setText(customer.name)
        dialogBinding.etContactPerson.setText(customer.contactPerson)
        dialogBinding.etPhone.setText(customer.phone)
        dialogBinding.etEmail.setText(customer.email)
        dialogBinding.etAddress.setText(customer.address)
        dialogBinding.switchActive.isChecked = customer.isActive
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etCustomerName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Customer name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedCustomer = customer.copy(
                name = name,
                contactPerson = dialogBinding.etContactPerson.text.toString().trim().ifEmpty { null },
                phone = dialogBinding.etPhone.text.toString().trim().ifEmpty { null },
                email = dialogBinding.etEmail.text.toString().trim().ifEmpty { null },
                address = dialogBinding.etAddress.text.toString().trim().ifEmpty { null },
                isActive = dialogBinding.switchActive.isChecked,
                updatedDt = Date()
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    customerDao.updateCustomer(updatedCustomer)
                    activityLogRepo.logCustomerUpdated(updatedCustomer)
                }
                Toast.makeText(requireContext(), "Customer updated successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadPage()
            }
        }

        dialog.show()
    }

    private fun confirmDeleteCustomer(customer: Customer) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Customer")
            .setMessage("Are you sure you want to delete ${customer.name}? This customer will be removed from existing sales records.")
            .setPositiveButton("Delete") { _, _ ->
                deleteCustomer(customer)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCustomer(customer: Customer) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                customerDao.deleteCustomer(customer.id)
                activityLogRepo.logCustomerDeleted(customer.id, customer.name, customer.phone ?: "")
            }
            Toast.makeText(requireContext(), "Customer deleted", Toast.LENGTH_SHORT).show()
            loadPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}