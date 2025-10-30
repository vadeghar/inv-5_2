package com.example.inv_5.ui.purchases

import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.databinding.FragmentPurchasesBinding
import com.example.inv_5.ui.purchases.PurchasesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.inv_5.ui.purchases.AddPurchaseActivity
import com.example.inv_5.data.entities.Purchase
import android.widget.Toast
import androidx.navigation.fragment.findNavController

class PurchasesFragment : Fragment() {

    private var _binding: FragmentPurchasesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: PurchasesAdapter
    private var currentPage = 0
    private val pageSize = 10
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val purchasesViewModel =
            ViewModelProvider(this).get(PurchasesViewModel::class.java)

        _binding = FragmentPurchasesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup RecyclerView
        adapter = PurchasesAdapter(emptyList()) { purchase ->
            // open AddPurchaseActivity for editing selected purchase
            val intent = Intent(requireContext(), AddPurchaseActivity::class.java)
            intent.putExtra("purchaseId", purchase.id)
            startActivity(intent)
        }
        binding.purchasesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.purchasesRecyclerView.adapter = adapter

        binding.backHomeButton.setOnClickListener {
            // navigate back to home fragment using NavController
            findNavController().navigate(com.example.inv_5.R.id.nav_home)
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

        binding.addPurchaseButton.setOnClickListener {
            val intent = Intent(requireContext(), AddPurchaseActivity::class.java)
            startActivity(intent)
        }

        // initial load
        loadPage()

        return root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data whenever fragment becomes visible (e.g., returning from AddPurchaseActivity)
        // Reset to first page to see most recent purchases
        currentPage = 0
        loadPage()
    }

    // load a page of purchases from DB
    private fun loadPage() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getInstance(requireContext())
            val offset = currentPage * pageSize
            val purchases: List<Purchase> = withContext(Dispatchers.IO) {
                if (searchQuery.isEmpty()) {
                    db.purchaseDao().listPurchases(pageSize, offset)
                } else {
                    db.purchaseDao().searchPurchases(searchQuery, pageSize, offset)
                }
            }
            if (purchases.isEmpty() && currentPage > 0) {
                // if no results, step back one page
                currentPage--
                Toast.makeText(requireContext(), "No more records", Toast.LENGTH_SHORT).show()
                return@launch
            }
            adapter.setItems(purchases)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}