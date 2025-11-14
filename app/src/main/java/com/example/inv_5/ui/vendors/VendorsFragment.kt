package com.example.inv_5.ui.vendors

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.R
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Supplier
import com.example.inv_5.databinding.FragmentVendorsBinding
import com.example.inv_5.databinding.DialogAddEditSupplierBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class VendorsFragment : Fragment() {

    private var _binding: FragmentVendorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SuppliersAdapter
    private var currentPage = 0
    private val pageSize = 10
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendorsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup RecyclerView
        adapter = SuppliersAdapter(
            items = emptyList(),
            onEditClick = { supplier -> showEditSupplierDialog(supplier) },
            onDeleteClick = { supplier -> confirmDeleteSupplier(supplier) }
        )
        binding.suppliersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.suppliersRecyclerView.adapter = adapter

        binding.backHomeButton.setOnClickListener {
            findNavController().navigate(R.id.nav_home)
        }

        // Add Supplier Button
        binding.addSupplierButton.setOnClickListener {
            showAddSupplierDialog()
        }

        // Setup search functionality
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query ?: ""
                currentPage = 0
                loadPage()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                currentPage = 0
                loadPage()
                return true
            }
        })

        binding.prevButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                loadPage()
            }
        }

        binding.nextButton.setOnClickListener {
            currentPage++
            loadPage()
        }

        // Initial load
        loadPage()

        return root
    }

    override fun onResume() {
        super.onResume()
        currentPage = 0
        loadPage()
    }

    private fun loadPage() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getInstance(requireContext())
            val offset = currentPage * pageSize
            val suppliers: List<Supplier> = withContext(Dispatchers.IO) {
                if (searchQuery.isEmpty()) {
                    db.supplierDao().searchSuppliers("", pageSize, offset)
                } else {
                    db.supplierDao().searchSuppliers(searchQuery, pageSize, offset)
                }
            }
            if (suppliers.isEmpty() && currentPage > 0) {
                currentPage--
                Toast.makeText(requireContext(), "No more records", Toast.LENGTH_SHORT).show()
                return@launch
            }
            adapter.setItems(suppliers)
        }
    }

    private fun showAddSupplierDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogAddEditSupplierBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.dialogTitleText.text = "Add Supplier"

        // Save button
        dialogBinding.saveButton.setOnClickListener {
            val name = dialogBinding.supplierNameEditText.text.toString().trim()
            val contactPerson = dialogBinding.contactPersonEditText.text.toString().trim()
            val phone = dialogBinding.phoneEditText.text.toString().trim()
            val email = dialogBinding.emailEditText.text.toString().trim()
            val address = dialogBinding.addressEditText.text.toString().trim()
            val isActive = dialogBinding.isActiveSwitch.isChecked

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Supplier name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentDate = Date()
            val newSupplier = Supplier(
                id = UUID.randomUUID().toString(),
                name = name,
                contactPerson = contactPerson,
                phone = phone,
                email = email,
                address = address,
                isActive = isActive,
                addedDt = currentDate,
                updatedDt = currentDate
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = DatabaseProvider.getInstance(requireContext())
                    db.supplierDao().insertSupplier(newSupplier)
                }
                Toast.makeText(requireContext(), "Supplier added successfully", Toast.LENGTH_SHORT).show()
                loadPage()
                dialog.dismiss()
            }
        }

        // Cancel button
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditSupplierDialog(supplier: Supplier) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogAddEditSupplierBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.dialogTitleText.text = "Edit Supplier"

        // Populate fields
        dialogBinding.supplierNameEditText.setText(supplier.name)
        dialogBinding.contactPersonEditText.setText(supplier.contactPerson)
        dialogBinding.phoneEditText.setText(supplier.phone)
        dialogBinding.emailEditText.setText(supplier.email)
        dialogBinding.addressEditText.setText(supplier.address)
        dialogBinding.isActiveSwitch.isChecked = supplier.isActive

        // Save button
        dialogBinding.saveButton.setOnClickListener {
            val name = dialogBinding.supplierNameEditText.text.toString().trim()
            val contactPerson = dialogBinding.contactPersonEditText.text.toString().trim()
            val phone = dialogBinding.phoneEditText.text.toString().trim()
            val email = dialogBinding.emailEditText.text.toString().trim()
            val address = dialogBinding.addressEditText.text.toString().trim()
            val isActive = dialogBinding.isActiveSwitch.isChecked

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Supplier name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedSupplier = supplier.copy(
                name = name,
                contactPerson = contactPerson,
                phone = phone,
                email = email,
                address = address,
                isActive = isActive,
                updatedDt = Date()
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = DatabaseProvider.getInstance(requireContext())
                    db.supplierDao().updateSupplier(updatedSupplier)
                }
                Toast.makeText(requireContext(), "Supplier updated successfully", Toast.LENGTH_SHORT).show()
                loadPage()
                dialog.dismiss()
            }
        }

        // Cancel button
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmDeleteSupplier(supplier: Supplier) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Supplier")
            .setMessage("Are you sure you want to delete ${supplier.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSupplier(supplier)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSupplier(supplier: Supplier) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = DatabaseProvider.getInstance(requireContext())
                db.supplierDao().deleteSupplier(supplier.id)
            }
            Toast.makeText(requireContext(), "Supplier deleted successfully", Toast.LENGTH_SHORT).show()
            loadPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}