package com.example.inv_5.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.R
import com.example.inv_5.databinding.FragmentHomeBinding
import com.example.inv_5.ui.purchases.AddPurchaseActivity
import com.example.inv_5.ui.sales.AddSaleActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
    private lateinit var recentActivityAdapter: RecentActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        setupRecyclerView()
        setupCardClicks()
        setupQuickActions()
        setupSwipeRefresh()
        observeViewModel()
        
        return binding.root
    }

    private fun setupCardClicks() {
        // Total Purchases card - Navigate to Purchases screen
        binding.cardTotalPurchases.setOnClickListener {
            findNavController().navigate(R.id.nav_purchases)
        }
        
        // Total Sales card - Navigate to Sales screen
        binding.cardTotalSales.setOnClickListener {
            findNavController().navigate(R.id.nav_sales)
        }
        
        // Out of Stock card - Navigate to Products screen
        // TODO: Add filter for out-of-stock items in future
        binding.cardOutOfStock.setOnClickListener {
            findNavController().navigate(R.id.nav_stock_management)
        }
        
        // Week Purchases card - Navigate to Purchases screen
        // TODO: Add date filter for this week in future
        binding.cardWeekPurchases.setOnClickListener {
            findNavController().navigate(R.id.nav_purchases)
        }
    }

    private fun setupRecyclerView() {
        recentActivityAdapter = RecentActivityAdapter { activity ->
            // Handle activity click
            Toast.makeText(
                requireContext(),
                "Clicked: ${activity.documentNumber}",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentActivityAdapter
        }
    }

    private fun setupQuickActions() {
        binding.btnNewPurchase.setOnClickListener {
            startActivity(Intent(requireContext(), AddPurchaseActivity::class.java))
        }
        
        binding.btnNewSale.setOnClickListener {
            startActivity(Intent(requireContext(), AddSaleActivity::class.java))
        }
        
        binding.btnViewProducts.setOnClickListener {
            // Navigate to products tab - handled by bottom navigation
            Toast.makeText(requireContext(), "Navigate to Products tab", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewModel.dashboardStats.observe(viewLifecycleOwner) { stats ->
            updateQuickStats(stats)
        }

        viewModel.recentActivities.observe(viewLifecycleOwner) { activities ->
            recentActivityAdapter.updateActivities(activities)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading && !binding.swipeRefresh.isRefreshing) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateQuickStats(stats: com.example.inv_5.data.models.DashboardStats) {
        // Update 2x2 grid stats
        binding.tvTotalPurchases.text = stats.thisMonthPurchases.toString()
        binding.tvTotalSales.text = stats.thisMonthSales.toString()
        binding.tvOutOfStock.text = stats.outOfStockProducts.toString()
        binding.tvWeekPurchases.text = stats.thisWeekPurchases.toString()
        
        // Update today's activity
        binding.tvTodayPurchases.text = "${stats.todayPurchases} items"
        binding.tvTodaySales.text = "${stats.todaySales} items"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}