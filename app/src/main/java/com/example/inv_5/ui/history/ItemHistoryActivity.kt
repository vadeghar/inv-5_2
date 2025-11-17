package com.example.inv_5.ui.history

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.R
import com.example.inv_5.data.models.ProductTransaction
import com.example.inv_5.databinding.ActivityItemHistoryBinding
import com.example.inv_5.utils.ItemHistoryExporter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ItemHistoryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
    }

    private lateinit var binding: ActivityItemHistoryBinding
    private lateinit var viewModel: ItemHistoryViewModel
    private lateinit var adapter: ItemHistoryAdapter
    
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var currentFilter: ProductTransaction.TransactionType? = null
    
    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get product ID from intent
        val productId = intent.getStringExtra(EXTRA_PRODUCT_ID)
        if (productId == null) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Item History"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ItemHistoryViewModel::class.java]

        // Setup RecyclerView
        adapter = ItemHistoryAdapter(emptyList()) { transaction ->
            onTransactionClicked(transaction)
        }
        binding.transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.transactionsRecyclerView.adapter = adapter

        // Setup UI listeners
        setupFilters()
        setupExport()

        // Observe ViewModel
        observeViewModel()

        // Load data
        viewModel.loadProductHistory(productId)
    }

    private fun setupFilters() {
        // Date range filter
        binding.dateRangeButton.setOnClickListener {
            showDateRangeDialog()
        }

        // Transaction type filter
        binding.filterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chipPurchases -> ProductTransaction.TransactionType.PURCHASE
                R.id.chipSales -> ProductTransaction.TransactionType.SALE
                else -> null
            }
            applyFilters()
        }
    }

    private fun setupExport() {
        binding.exportFab.setOnClickListener {
            exportToExcel()
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(this) { transactions ->
            if (transactions.isEmpty()) {
                binding.transactionsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.transactionsRecyclerView.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
                adapter.updateTransactions(transactions)
            }
        }

        viewModel.summary.observe(this) { summary ->
            binding.productNameTextView.text = summary.productName
            binding.productBarcodeTextView.text = "Barcode: ${summary.productBarcode}"
            binding.currentStockTextView.text = "Current Stock: ${summary.currentStock} units"
            
            binding.openingBalanceTextView.text = summary.openingBalance.toString()
            binding.totalPurchasesTextView.text = "+${summary.totalPurchases}"
            binding.totalSalesTextView.text = "-${summary.totalSales}"
            binding.closingBalanceTextView.text = summary.closingBalance.toString()
        }

        viewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun showDateRangeDialog() {
        val options = arrayOf(
            "All Time",
            "Today",
            "This Week",
            "This Month",
            "This Year",
            "Custom Range"
        )

        AlertDialog.Builder(this)
            .setTitle("Select Date Range")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setDateRange(null, null, "All Time")
                    1 -> setToday()
                    2 -> setThisWeek()
                    3 -> setThisMonth()
                    4 -> setThisYear()
                    5 -> showCustomDatePicker()
                }
            }
            .show()
    }

    private fun setDateRange(start: Date?, end: Date?, label: String) {
        startDate = start
        endDate = end
        binding.dateRangeButton.text = label
        applyFilters()
    }

    private fun setToday() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.time
        
        setDateRange(start, end, "Today")
    }

    private fun setThisWeek() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time
        
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.time
        
        setDateRange(start, end, "This Week")
    }

    private fun setThisMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.time
        
        setDateRange(start, end, "This Month")
    }

    private fun setThisYear() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.time
        
        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.time
        
        setDateRange(start, end, "This Year")
    }

    private fun showCustomDatePicker() {
        val calendar = Calendar.getInstance()
        
        // Start date picker
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val startCal = Calendar.getInstance()
                startCal.set(year, month, day, 0, 0, 0)
                startDate = startCal.time
                
                // End date picker
                DatePickerDialog(
                    this,
                    { _, endYear, endMonth, endDay ->
                        val endCal = Calendar.getInstance()
                        endCal.set(endYear, endMonth, endDay, 23, 59, 59)
                        endDate = endCal.time
                        
                        val label = "${dateFormat.format(startDate!!)} - ${dateFormat.format(endDate!!)}"
                        binding.dateRangeButton.text = label
                        applyFilters()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun applyFilters() {
        val productId = intent.getStringExtra(EXTRA_PRODUCT_ID) ?: return
        viewModel.loadProductHistory(productId, startDate, endDate, currentFilter)
    }

    private fun onTransactionClicked(transaction: ProductTransaction) {
        // Show transaction details in a dialog
        val message = buildString {
            append("Document: ${transaction.documentNumber}\n")
            append("Date: ${dateFormat.format(transaction.date)}\n")
            append("Type: ${transaction.documentType}\n")
            append("Quantity: ${transaction.quantity}\n")
            append("Rate: ₹${String.format(Locale.getDefault(), "%.2f", transaction.rate)}\n")
            append("Amount: ₹${String.format(Locale.getDefault(), "%.2f", transaction.amount)}\n")
            append("Balance: ${transaction.runningBalance}\n")
            if (!transaction.customerOrSupplier.isNullOrEmpty()) {
                val label = if (transaction.documentType == ProductTransaction.TransactionType.PURCHASE) {
                    "Supplier"
                } else {
                    "Customer"
                }
                append("$label: ${transaction.customerOrSupplier}")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Transaction Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportToExcel() {
        val summary = viewModel.summary.value
        val transactions = viewModel.transactions.value

        if (summary == null || transactions == null) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val dateRangeLabel = binding.dateRangeButton.text.toString()
        val exporter = ItemHistoryExporter(this)
        
        val result = exporter.exportItemHistory(summary, transactions, dateRangeLabel)
        
        if (result) {
            Toast.makeText(this, "Exported successfully to Downloads folder", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }
}
