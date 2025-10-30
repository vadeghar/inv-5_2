package com.example.inv_5.ui.products

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
import com.example.inv_5.data.entities.Product
import com.example.inv_5.databinding.FragmentProductsBinding
import com.example.inv_5.databinding.DialogEditProductBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductsAdapter
    private var currentPage = 0
    private val pageSize = 10
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup RecyclerView
        adapter = ProductsAdapter(emptyList()) { product ->
            // Show edit dialog when product is clicked
            showEditProductDialog(product)
        }
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.productsRecyclerView.adapter = adapter

        binding.backHomeButton.setOnClickListener {
            // navigate back to home fragment using NavController
            findNavController().navigate(R.id.nav_home)
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

        // initial load
        loadPage()

        return root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data whenever fragment becomes visible
        currentPage = 0
        loadPage()
    }

    // load a page of products from DB
    private fun loadPage() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getInstance(requireContext())
            val offset = currentPage * pageSize
            val products: List<Product> = withContext(Dispatchers.IO) {
                if (searchQuery.isEmpty()) {
                    db.productDao().searchProducts("", pageSize, offset)
                } else {
                    db.productDao().searchProducts(searchQuery, pageSize, offset)
                }
            }
            if (products.isEmpty() && currentPage > 0) {
                // if no results, step back one page
                currentPage--
                Toast.makeText(requireContext(), "No more records", Toast.LENGTH_SHORT).show()
                return@launch
            }
            adapter.setItems(products)
        }
    }

    private fun showEditProductDialog(product: Product) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogEditProductBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Set window size
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Format date for display
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Populate fields with product data
        dialogBinding.idEditText.setText(product.id)
        dialogBinding.nameEditText.setText(product.name)
        dialogBinding.barcodeEditText.setText(product.barCode)
        dialogBinding.categoryEditText.setText(product.category)
        dialogBinding.mrpEditText.setText(String.format("%.2f", product.mrp))
        dialogBinding.salePriceEditText.setText(String.format("%.2f", product.salePrice))
        dialogBinding.quantityOnHandEditText.setText(product.quantityOnHand.toString())
        dialogBinding.reorderPointEditText.setText(product.reorderPoint.toString())
        dialogBinding.maximumStockLevelEditText.setText(product.maximumStockLevel.toString())
        dialogBinding.isActiveSwitch.isChecked = product.isActive
        
        // Set dates if available
        product.addedDt?.let {
            dialogBinding.addedDtEditText.setText(dateFormat.format(it))
        }
        product.updatedDt?.let {
            dialogBinding.updatedDtEditText.setText(dateFormat.format(it))
        }

        // Save button
        dialogBinding.saveButton.setOnClickListener {
            // Validate editable fields
            val name = dialogBinding.nameEditText.text.toString().trim()
            val category = dialogBinding.categoryEditText.text.toString().trim()
            val reorderPointStr = dialogBinding.reorderPointEditText.text.toString().trim()
            val maxStockLevelStr = dialogBinding.maximumStockLevelEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Product name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category.isEmpty()) {
                Toast.makeText(requireContext(), "Category is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reorderPoint = reorderPointStr.toIntOrNull() ?: 1
            val maxStockLevel = maxStockLevelStr.toIntOrNull() ?: 5
            val isActive = dialogBinding.isActiveSwitch.isChecked

            // Create updated product
            val updatedProduct = product.copy(
                name = name,
                category = category,
                reorderPoint = reorderPoint,
                maximumStockLevel = maxStockLevel,
                isActive = isActive,
                updatedDt = Date()
            )

            // Update in database
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = DatabaseProvider.getInstance(requireContext())
                    db.productDao().updateProduct(updatedProduct)
                }
                Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT).show()
                loadPage() // Refresh the list
                dialog.dismiss()
            }
        }

        // Cancel button
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
