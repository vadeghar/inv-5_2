package com.example.inv_5.ui.expenses

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.inv_5.R
import com.example.inv_5.databinding.ActivityAddExpenseBinding
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.model.Expense
import com.example.inv_5.data.model.ExpenseCategory
import com.example.inv_5.data.repository.ActivityLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var selectedDate: Date = Date()
    private var selectedPaymentDate: Date? = null
    private var expenseId: Long = 0
    private var isEditMode = false
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    private var categories: List<ExpenseCategory> = emptyList()
    private var selectedCategoryType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Add Expense"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Check if edit mode
        expenseId = intent.getLongExtra("EXPENSE_ID", 0)
        isEditMode = expenseId > 0

        if (isEditMode) {
            supportActionBar?.title = "Edit Expense"
            binding.toolbar.title = "Edit Expense"
            binding.btnDelete.visibility = android.view.View.VISIBLE
            loadExpenseData()
        }

        setupDatePicker()
        setupCategoryAutocomplete()
        setupExpenseTypeDropdown()
        setupPaymentModeDropdown()
        setupPaymentStatusDropdown()
        setupPaymentDatePicker()
        setupTotalAmountListener()
        setupInfoIcon()
        setupButtons()

        // Set default dates to today
        binding.etExpenseDate.setText(dateFormat.format(selectedDate))
        selectedPaymentDate = Date()
        binding.etPaymentDate.setText(dateFormat.format(selectedPaymentDate!!))
    }

    private fun setupDatePicker() {
        binding.etExpenseDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    binding.etExpenseDate.setText(dateFormat.format(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupCategoryAutocomplete() {
        lifecycleScope.launch {
            categories = withContext(Dispatchers.IO) {
                DatabaseProvider.getInstance(this@AddExpenseActivity)
                    .expenseCategoryDao()
                    .getAllCategoriesList()
            }

            val categoryNames = categories.map { it.categoryName }
            val adapter = ArrayAdapter(
                this@AddExpenseActivity,
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
            )

            binding.acExpenseCategory.setAdapter(adapter)
            binding.acExpenseCategory.threshold = 1

            // Handle item selection from dropdown
            binding.acExpenseCategory.setOnItemClickListener { _, _, position, _ ->
                val selectedCategory = categories.find { it.categoryName == categoryNames[position] }
                selectedCategory?.let {
                    handleCategorySelection(it)
                }
            }

            // Handle manual text entry - match category when user types exact name
            binding.acExpenseCategory.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val enteredText = binding.acExpenseCategory.text.toString().trim()
                    val matchedCategory = categories.find { 
                        it.categoryName.equals(enteredText, ignoreCase = true) 
                    }
                    matchedCategory?.let {
                        handleCategorySelection(it)
                    }
                }
            }
        }
    }

    private fun handleCategorySelection(category: ExpenseCategory) {
        selectedCategoryType = category.categoryType
        
        // If category is "Others", enable manual type selection
        if (category.categoryName == "Others") {
            binding.acExpenseType.isEnabled = true
            binding.acExpenseType.setText("")
            binding.tilExpenseType.hint = "Select Expense Type (Manual)"
        } else {
            // Auto-populate type based on category
            binding.acExpenseType.isEnabled = false
            binding.acExpenseType.setText(category.categoryType)
            binding.tilExpenseType.hint = "Expense Type (Auto)"
        }
    }

    private fun setupExpenseTypeDropdown() {
        val expenseTypes = arrayOf("CAPEX", "OPEX", "MIXED")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            expenseTypes
        )
        binding.acExpenseType.setAdapter(adapter)
    }

    private fun setupPaymentModeDropdown() {
        val paymentModes = arrayOf("Cash", "Card", "UPI", "Bank Transfer", "Cheque", "Net Banking")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            paymentModes
        )
        binding.acPaymentMode.setAdapter(adapter)
    }

    private fun setupPaymentStatusDropdown() {
        val paymentStatuses = arrayOf("Paid", "Pending", "Partial")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            paymentStatuses
        )
        binding.acPaymentStatus.setAdapter(adapter)
    }

    private fun setupPaymentDatePicker() {
        binding.etPaymentDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedPaymentDate?.let { calendar.time = it }

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedPaymentDate = calendar.time
                    binding.etPaymentDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTotalAmountListener() {
        binding.etTotalAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val totalAmountStr = binding.etTotalAmount.text.toString().trim()
                if (totalAmountStr.isNotEmpty() && binding.etPaidAmount.text.toString().isEmpty()) {
                    val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
                    if (totalAmount > 0) {
                        binding.etPaidAmount.setText(totalAmountStr)
                    }
                }
            }
        }
    }

    private fun setupInfoIcon() {
        binding.tilExpenseType.setEndIconOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Expense Types")
                .setMessage(
                    "CAPEX (Capital Expenditure):\n" +
                    "Long-term investment spending\n" +
                    "Example: Machinery, Furniture, Property\n\n" +
                    "OPEX (Operational Expenditure):\n" +
                    "Everyday running costs of the business\n" +
                    "Example: Rent, Utilities, Salaries"
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnDelete.setOnClickListener {
            deleteExpense()
        }
    }

    private fun loadExpenseData() {
        lifecycleScope.launch {
            val expense = withContext(Dispatchers.IO) {
                DatabaseProvider.getInstance(this@AddExpenseActivity)
                    .expenseDao()
                    .getExpenseById(expenseId)
            }

            expense?.let {
                selectedDate = it.expenseDate
                binding.etExpenseDate.setText(dateFormat.format(it.expenseDate))
                binding.acExpenseCategory.setText(it.expenseCategory, false)
                binding.acExpenseType.setText(it.expenseType, false)
                binding.etDescription.setText(it.description)
                binding.etTotalAmount.setText(it.totalAmount.toString())
                
                // Taxation
                if (it.gstRate > 0) binding.etGstRate.setText(it.gstRate.toString())
                if (it.cgstAmount > 0) binding.etCgstAmount.setText(it.cgstAmount.toString())
                if (it.sgstAmount > 0) binding.etSgstAmount.setText(it.sgstAmount.toString())
                if (it.igstAmount > 0) binding.etIgstAmount.setText(it.igstAmount.toString())
                
                // Payment Info
                if (it.paymentMode.isNotEmpty()) binding.acPaymentMode.setText(it.paymentMode, false)
                if (it.paymentStatus.isNotEmpty()) binding.acPaymentStatus.setText(it.paymentStatus, false)
                if (it.paidAmount > 0) binding.etPaidAmount.setText(it.paidAmount.toString())
                it.paymentDate?.let { date ->
                    selectedPaymentDate = date
                    binding.etPaymentDate.setText(dateFormat.format(date))
                }

                // Check if category is "Others" to enable manual type selection
                if (it.expenseCategory == "Others") {
                    binding.acExpenseType.isEnabled = true
                    binding.tilExpenseType.hint = "Select Expense Type (Manual)"
                }
            }
        }
    }

    private fun saveExpense() {
        val category = binding.acExpenseCategory.text.toString().trim()
        val type = binding.acExpenseType.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val totalAmountStr = binding.etTotalAmount.text.toString().trim()

        // Validation
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select expense category", Toast.LENGTH_SHORT).show()
            return
        }

        if (type.isEmpty()) {
            Toast.makeText(this, "Please select expense type", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }

        if (totalAmountStr.isEmpty()) {
            Toast.makeText(this, "Please enter total amount", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
        if (totalAmount <= 0) {
            Toast.makeText(this, "Please enter valid total amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse optional fields
        val paidAmount = binding.etPaidAmount.text.toString().toDoubleOrNull() ?: 0.0
        
        // Validate paid amount does not exceed total amount
        if (paidAmount > totalAmount) {
            Toast.makeText(this, "Paid amount cannot be greater than total amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        val gstRate = binding.etGstRate.text.toString().toDoubleOrNull() ?: 0.0
        val cgstAmount = binding.etCgstAmount.text.toString().toDoubleOrNull() ?: 0.0
        val sgstAmount = binding.etSgstAmount.text.toString().toDoubleOrNull() ?: 0.0
        val igstAmount = binding.etIgstAmount.text.toString().toDoubleOrNull() ?: 0.0
        val paymentMode = binding.acPaymentMode.text.toString().trim()
        val paymentStatus = binding.acPaymentStatus.text.toString().trim()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val database = DatabaseProvider.getInstance(this@AddExpenseActivity)
                val activityLogRepo = ActivityLogRepository(this@AddExpenseActivity)
                
                if (isEditMode) {
                    // Update existing expense
                    val expense = Expense(
                        expenseId = expenseId,
                        expenseDate = selectedDate,
                        expenseCategory = category,
                        expenseType = type,
                        description = description,
                        totalAmount = totalAmount,
                        cgstAmount = cgstAmount,
                        sgstAmount = sgstAmount,
                        igstAmount = igstAmount,
                        gstRate = gstRate,
                        paymentMode = paymentMode,
                        paymentStatus = paymentStatus,
                        paymentDate = selectedPaymentDate,
                        paidAmount = paidAmount
                    )
                    database.expenseDao().update(expense)
                    activityLogRepo.logExpenseUpdated(expense)
                } else {
                    // Insert new expense
                    val expense = Expense(
                        expenseDate = selectedDate,
                        expenseCategory = category,
                        expenseType = type,
                        description = description,
                        totalAmount = totalAmount,
                        cgstAmount = cgstAmount,
                        sgstAmount = sgstAmount,
                        igstAmount = igstAmount,
                        gstRate = gstRate,
                        paymentMode = paymentMode,
                        paymentStatus = paymentStatus,
                        paymentDate = selectedPaymentDate,
                        paidAmount = paidAmount
                    )
                    val insertedId = database.expenseDao().insert(expense)
                    val insertedExpense = expense.copy(expenseId = insertedId)
                    activityLogRepo.logExpenseAdded(insertedExpense)
                }
            }

            Toast.makeText(
                this@AddExpenseActivity,
                if (isEditMode) "Expense updated successfully" else "Expense added successfully",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun deleteExpense() {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val database = DatabaseProvider.getInstance(this@AddExpenseActivity)
                        val activityLogRepo = ActivityLogRepository(this@AddExpenseActivity)
                        val expense = database.expenseDao().getExpenseById(expenseId)
                        expense?.let {
                            database.expenseDao().delete(it)
                            activityLogRepo.logExpenseDeleted(it.expenseId, it.expenseCategory, it.totalAmount)
                        }
                    }

                    Toast.makeText(
                        this@AddExpenseActivity,
                        "Expense deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
