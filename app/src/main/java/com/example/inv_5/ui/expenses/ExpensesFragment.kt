package com.example.inv_5.ui.expenses

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.databinding.FragmentExpensesBinding
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.model.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var expensesAdapter: ExpensesAdapter
    private var allExpenses: List<Expense> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupFab()
        loadExpenses()
    }

    private fun setupRecyclerView() {
        expensesAdapter = ExpensesAdapter(emptyList()) { expense ->
            openExpenseDetails(expense)
        }

        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expensesAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterExpenses(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterExpenses(newText ?: "")
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadExpenses() {
        val database = DatabaseProvider.getInstance(requireContext())
        
        database.expenseDao().getAllExpenses().observe(viewLifecycleOwner) { expenses ->
            allExpenses = expenses
            expensesAdapter.updateExpenses(expenses)
            updateEmptyState(expenses.isEmpty())
        }
    }

    private fun filterExpenses(query: String) {
        if (query.isEmpty()) {
            expensesAdapter.updateExpenses(allExpenses)
            updateEmptyState(allExpenses.isEmpty())
        } else {
            val filtered = allExpenses.filter {
                it.expenseCategory.contains(query, ignoreCase = true) ||
                it.expenseType.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
            expensesAdapter.updateExpenses(filtered)
            updateEmptyState(filtered.isEmpty())
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvExpenses.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openExpenseDetails(expense: Expense) {
        val intent = Intent(requireContext(), AddExpenseActivity::class.java).apply {
            putExtra("EXPENSE_ID", expense.expenseId)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
