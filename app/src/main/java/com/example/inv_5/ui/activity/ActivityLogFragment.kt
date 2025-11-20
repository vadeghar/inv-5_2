package com.example.inv_5.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.R
import com.example.inv_5.databinding.FragmentActivityLogBinding
import com.example.inv_5.data.repository.ActivityLogRepository
import com.example.inv_5.data.entities.ActivityLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityLogFragment : Fragment() {

    private var _binding: FragmentActivityLogBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var activityLogRepository: ActivityLogRepository
    private lateinit var adapter: ActivityLogAdapter
    private var allActivities: List<ActivityLog> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        activityLogRepository = ActivityLogRepository(requireContext())
        
        setupRecyclerView()
        setupSearchView()
        loadActivities()
    }

    private fun setupRecyclerView() {
        adapter = ActivityLogAdapter { activity ->
            Toast.makeText(
                requireContext(),
                activity.description,
                Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.rvActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ActivityLogFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterActivities(newText ?: "")
                return true
            }
        })
    }

    private fun loadActivities() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
                
                allActivities = withContext(Dispatchers.IO) {
                    activityLogRepository.getAllActivities()
                }
                
                updateUI()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error loading activities: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterActivities(query: String) {
        val filtered = if (query.isEmpty()) {
            allActivities
        } else {
            allActivities.filter { activity ->
                activity.description.contains(query, ignoreCase = true) ||
                activity.documentNumber?.contains(query, ignoreCase = true) == true ||
                activity.additionalInfo?.contains(query, ignoreCase = true) == true ||
                activity.entityType.contains(query, ignoreCase = true) ||
                activity.activityType.contains(query, ignoreCase = true)
            }
        }
        
        adapter.updateActivities(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateUI() {
        adapter.updateActivities(allActivities)
        updateEmptyState(allActivities.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvActivities.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvActivities.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadActivities()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
