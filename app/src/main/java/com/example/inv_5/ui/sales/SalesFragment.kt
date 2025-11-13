package com.example.inv_5.ui.sales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.R
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Sale
import com.example.inv_5.databinding.FragmentSalesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SalesAdapter
    private var currentPage = 0
    private val pageSize = 10
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val salesViewModel =
            ViewModelProvider(this).get(SalesViewModel::class.java)

        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup RecyclerView
        adapter = SalesAdapter(emptyList()) { sale ->
            // open AddSaleActivity for editing selected sale
            val intent = Intent(requireContext(), AddSaleActivity::class.java)
            intent.putExtra("saleId", sale.id)
            startActivity(intent)
        }
        binding.salesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.salesRecyclerView.adapter = adapter

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

        binding.addSaleButton.setOnClickListener {
            val intent = Intent(requireContext(), AddSaleActivity::class.java)
            startActivity(intent)
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

    // load a page of sales from DB
    private fun loadPage() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getInstance(requireContext())
            val offset = currentPage * pageSize
            val sales: List<Sale> = withContext(Dispatchers.IO) {
                if (searchQuery.isEmpty()) {
                    db.saleDao().listSales(pageSize, offset)
                } else {
                    db.saleDao().searchSales(searchQuery, pageSize, offset)
                }
            }
            if (sales.isEmpty() && currentPage > 0) {
                // if no results, step back one page
                currentPage--
                Toast.makeText(requireContext(), "No more records", Toast.LENGTH_SHORT).show()
                return@launch
            }
            adapter.setItems(sales)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}