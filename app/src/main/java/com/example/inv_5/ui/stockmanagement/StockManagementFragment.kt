package com.example.inv_5.ui.stockmanagement

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.R
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.StockAdjustment
import com.example.inv_5.databinding.FragmentStockManagementBinding
import com.example.inv_5.ui.history.ItemHistoryActivity
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText

class StockManagementFragment : Fragment() {

    private var _binding: FragmentStockManagementBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: StockManagementViewModel
    private lateinit var adapter: StockProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockManagementBinding.inflate(inflater, container, false)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[StockManagementViewModel::class.java]
        
        setupRecyclerView()
        setupSearchBar()
        setupFilters()
        setupObservers()
        setupFAB()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = StockProductAdapter(
            onProductClick = { product ->
                // Navigate to ItemHistoryActivity to show stock movement history
                val intent = Intent(requireContext(), ItemHistoryActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                intent.putExtra("PRODUCT_NAME", product.name)
                startActivity(intent)
            },
            onAdjustClick = { product ->
                showAdjustmentDialog(product)
            }
        )
        
        binding.rvStockProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StockManagementFragment.adapter
        }
    }

    private fun setupSearchBar() {
        // Search functionality
        binding.etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchProducts(s?.toString() ?: "")
            }
        })

        // Barcode scanner button - TODO: Implement with ML Kit
        binding.btnScanBarcode.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Barcode scanner - Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            
            when (checkedIds[0]) {
                R.id.chipAll -> viewModel.applyFilter(StockManagementViewModel.StockFilter.ALL)
                R.id.chipInStock -> viewModel.applyFilter(StockManagementViewModel.StockFilter.IN_STOCK)
                R.id.chipOutOfStock -> viewModel.applyFilter(StockManagementViewModel.StockFilter.OUT_OF_STOCK)
                R.id.chipLowStock -> viewModel.applyFilter(StockManagementViewModel.StockFilter.LOW_STOCK)
            }
        }
    }

    private fun setupObservers() {
        // Observe filtered products
        viewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            
            // Show/hide empty state
            if (products.isEmpty()) {
                binding.rvStockProducts.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvStockProducts.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }
        }

        // Observe statistics
        viewModel.totalProducts.observe(viewLifecycleOwner) { count ->
            binding.tvTotalProducts.text = count.toString()
        }

        viewModel.outOfStockCount.observe(viewLifecycleOwner) { count ->
            binding.tvOutOfStock.text = count.toString()
        }

        viewModel.lowStockCount.observe(viewLifecycleOwner) { count ->
            binding.tvLowStock.text = count.toString()
        }

        viewModel.inventoryValue.observe(viewLifecycleOwner) { value ->
            binding.tvInventoryValue.text = "₱${String.format("%.2f", value)}"
        }
    }

    private fun setupFAB() {
        binding.fabStockValuation.setOnClickListener {
            // TODO: Show stock valuation report dialog
            Toast.makeText(
                requireContext(),
                "Stock Valuation Report - Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showAdjustmentDialog(product: Product) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_stock_adjustment, null, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Find views
        val tvProductInfo = dialogView.findViewById<TextView>(R.id.tvProductInfo)
        val tvCurrentQuantity = dialogView.findViewById<TextView>(R.id.tvCurrentQuantity)
        val etNewQuantity = dialogView.findViewById<TextInputEditText>(R.id.etNewQuantity)
        val actvAdjustmentReason = dialogView.findViewById<AutoCompleteTextView>(R.id.actvAdjustmentReason)
        val etNotes = dialogView.findViewById<TextInputEditText>(R.id.etNotes)
        val tvDifference = dialogView.findViewById<TextView>(R.id.tvDifference)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // Set product info
        tvProductInfo.text = "Product: ${product.name} (${product.barCode})"
        tvCurrentQuantity.text = product.quantityOnHand.toString()

        // Setup adjustment reason dropdown
        val reasons = StockAdjustment.AdjustmentReason.values().map { it.name.replace("_", " ") }
        val reasonAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, reasons)
        actvAdjustmentReason.setAdapter(reasonAdapter)

        // Calculate difference on quantity change
        etNewQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newQty = s?.toString()?.toIntOrNull() ?: 0
                val difference = newQty - product.quantityOnHand
                tvDifference.text = if (difference >= 0) "+$difference" else difference.toString()
                tvDifference.setTextColor(
                    requireContext().getColor(
                        if (difference >= 0) android.R.color.holo_green_dark
                        else android.R.color.holo_red_dark
                    )
                )
            }
        })

        // Cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Save button
        btnSave.setOnClickListener {
            val newQuantityText = etNewQuantity.text?.toString()
            val reasonText = actvAdjustmentReason.text?.toString()
            val notes = etNotes.text?.toString()

            // Validation
            if (newQuantityText.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please enter new quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (reasonText.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select adjustment reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newQuantity = newQuantityText.toIntOrNull()
            if (newQuantity == null || newQuantity < 0) {
                Toast.makeText(requireContext(), "Invalid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Find matching enum reason
            val reason = StockAdjustment.AdjustmentReason.values()
                .find { it.name.replace("_", " ") == reasonText }
                ?: StockAdjustment.AdjustmentReason.OTHER

            // Save adjustment
            viewModel.adjustStock(
                product = product,
                newQuantity = newQuantity,
                reason = reason,
                notes = notes?.ifEmpty { null },
                adjustedBy = "System User" // TODO: Get from user session
            )

            Toast.makeText(
                requireContext(),
                "Stock adjusted successfully",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning from ItemHistoryActivity
        viewModel.loadStockData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
