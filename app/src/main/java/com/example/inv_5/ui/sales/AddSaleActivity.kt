package com.example.inv_5.ui.sales

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.R
import com.example.inv_5.data.entities.Customer
import com.example.inv_5.data.entities.Sale
import com.example.inv_5.data.entities.SaleItem
import com.example.inv_5.databinding.ActivityAddSaleBinding
import com.example.inv_5.databinding.DialogAddEditCustomerBinding
import com.example.inv_5.databinding.DialogAddSaleItemBinding
import com.example.inv_5.ui.purchases.InlineBarcodeScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.example.inv_5.bluetooth.BluetoothScannerManager

class AddSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSaleBinding
    private val saleItems = mutableListOf<SaleItem>()
    private var editingSaleId: String? = null
    private var originalSale: Sale? = null
    private lateinit var adapter: SaleItemsAdapter
    private lateinit var viewModel: AddSaleViewModel
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private var onCameraGrantedCallback: (() -> Unit)? = null
    private var selectedCustomerId: String? = null
    
    // Bluetooth scanner
    private lateinit var bluetoothScannerManager: BluetoothScannerManager
    private var bluetoothScanListener: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(AddSaleViewModel::class.java)
        
        // Initialize Bluetooth scanner manager
        bluetoothScannerManager = BluetoothScannerManager.getInstance(this)

        // Pre-register camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onCameraGrantedCallback?.invoke()
                onCameraGrantedCallback = null
            } else {
                Toast.makeText(this, "Camera permission needed for barcode scanning", Toast.LENGTH_SHORT).show()
            }
        }

        adapter = SaleItemsAdapter(saleItems)
        binding.saleItemsRecyclerView.adapter = adapter
        binding.saleItemsRecyclerView.layoutManager = LinearLayoutManager(this)

        // sale date EditText with calendar icon
        binding.saleDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        // initialize sale date to today if empty
        val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        if (binding.saleDateEditText.text.isNullOrEmpty()) {
            binding.saleDateEditText.setText(fmt.format(Calendar.getInstance().time))
        }

        // Add item FAB
        binding.addItemButton.setOnClickListener {
            showAddSaleItemDialog()
        }

        // Back button
        binding.backToSalesButton.setOnClickListener {
            finish()
        }

        // If started with a saleId, preload the sale and items for edit
        val saleId = intent.getStringExtra("saleId")
        if (!saleId.isNullOrEmpty()) {
            editingSaleId = saleId
            lifecycleScope.launch {
                try {
                    val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
                    val sale = withContext(Dispatchers.IO) { db.saleDao().getById(saleId) }
                    val items = withContext(Dispatchers.IO) { db.saleItemDao().listBySaleId(saleId) }
                    sale?.let { s ->
                        originalSale = s
                        // Set activity title for edit mode
                        title = "Edit Sale"
                        binding.customerNameEditText.setText(s.customerName)
                        binding.customerAddressEditText.setText(s.customerAddress)
                        binding.customerPhoneEditText.setText(s.customerPhone)
                        val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                        binding.saleDateEditText.setText(fmt.format(s.saleDate))
                        if (s.status == "Active") binding.activeRadioButton.isChecked = true 
                        else binding.inactiveRadioButton.isChecked = true
                    }
                    saleItems.clear()
                    saleItems.addAll(items)
                    adapter.notifyDataSetChanged()
                    updateSummary()
                } catch (e: Exception) {
                    Log.e("AddSaleActivity", "Failed to load sale for edit", e)
                }
            }
        }

        binding.saveButton.setOnClickListener {
            saveSale()
        }

        // observe save status from ViewModel
        viewModel.saveStatus.observe(this) { status ->
            when {
                status == null -> { }
                status == "saving" -> {
                    binding.loadingOverlay.visibility = View.VISIBLE
                }
                status == "success" -> {
                    Toast.makeText(this, "Sale saved", Toast.LENGTH_SHORT).show()
                    binding.loadingOverlay.visibility = View.GONE
                    setResult(RESULT_OK)
                    finish()
                }
                status.startsWith("error") -> {
                    Toast.makeText(this, "Error saving sale: $status", Toast.LENGTH_LONG).show()
                    binding.loadingOverlay.visibility = View.GONE
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.btnAddCustomer.setOnClickListener {
            showAddCustomerDialog()
        }

        binding.btnSelectCustomer.setOnClickListener {
            showCustomerSelectionDialog()
        }
    }

    private fun showAddCustomerDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogAddEditCustomerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.dialogTitle.text = "Add Customer"

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etCustomerName.text.toString().trim()
            val contactPerson = dialogBinding.etContactPerson.text.toString().trim()
            val phone = dialogBinding.etPhone.text.toString().trim()
            val email = dialogBinding.etEmail.text.toString().trim()
            val address = dialogBinding.etAddress.text.toString().trim()
            val isActive = dialogBinding.switchActive.isChecked

            if (name.isEmpty()) {
                Toast.makeText(this, "Customer name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentDate = Date()
            val newCustomer = Customer(
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
                val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
                withContext(Dispatchers.IO) {
                    db.customerDao().insertCustomer(newCustomer)
                }
                Toast.makeText(this@AddSaleActivity, "Customer added successfully", Toast.LENGTH_SHORT).show()
                
                // Auto-fill the customer fields
                selectedCustomerId = newCustomer.id
                binding.customerNameEditText.setText(newCustomer.name)
                binding.customerAddressEditText.setText(newCustomer.address ?: "")
                binding.customerPhoneEditText.setText(newCustomer.phone ?: "")
                
                dialog.dismiss()
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCustomerSelectionDialog() {
        lifecycleScope.launch {
            val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
            val customers = withContext(Dispatchers.IO) {
                db.customerDao().getActiveCustomers()
            }

            if (customers.isEmpty()) {
                Toast.makeText(this@AddSaleActivity, "No active customers found. Please add customers first.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val customerNames = customers.map { "${it.name} - ${it.phone ?: "No phone"}" }.toTypedArray()

            AlertDialog.Builder(this@AddSaleActivity)
                .setTitle("Select Customer")
                .setItems(customerNames) { dialog, which ->
                    val selectedCustomer = customers[which]
                    selectedCustomerId = selectedCustomer.id
                    binding.customerNameEditText.setText(selectedCustomer.name)
                    binding.customerAddressEditText.setText(selectedCustomer.address ?: "")
                    binding.customerPhoneEditText.setText(selectedCustomer.phone ?: "")
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.saleDateEditText.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showAddSaleItemDialog() {
        val dialogBinding = DialogAddSaleItemBinding.inflate(layoutInflater)

        var isUpdating = false

        // Input filter to restrict decimals to 2 places
        class DecimalDigitsInputFilter(private val digitsBeforeZero: Int, private val digitsAfterZero: Int) : InputFilter {
            private val pattern = Regex("""^\d{0,${digitsBeforeZero}}(\.\d{0,${digitsAfterZero}})?$""")
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: android.text.Spanned?, dstart: Int, dend: Int): CharSequence? {
                try {
                    val newVal = (dest?.substring(0, dstart) ?: "") + (source?.subSequence(start, end) ?: "") + (dest?.substring(dend) ?: "")
                    if (newVal.isEmpty()) return null
                    if (pattern.matches(newVal)) return null
                } catch (e: Exception) { }
                return ""
            }
        }

        val decimalFilter = DecimalDigitsInputFilter(10, 2)
        dialogBinding.salePriceEditText.filters = arrayOf<InputFilter>(decimalFilter)
        dialogBinding.taxPercentageEditText.filters = arrayOf<InputFilter>(decimalFilter)

        // Helper to recalculate dependent fields
        fun recalc() {
            if (isUpdating) return
            val mrp = dialogBinding.mrpEditText.text.toString().toDoubleOrNull() ?: 0.0
            val salePrice = dialogBinding.salePriceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val quantity = dialogBinding.quantityEditText.text.toString().toIntOrNull() ?: 1
            val taxPercentage = dialogBinding.taxPercentageEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Calculate discount percentage based on MRP and Sale Price
            val discountPct = if (mrp > 0) ((mrp - salePrice) / mrp) * 100.0 else 0.0

            // Total = Sale Price × Quantity
            val total = salePrice * quantity
            
            // Tax = (Sale Price × Quantity) × (Tax% / 100)
            val tax = total * (taxPercentage / 100.0)
            
            // Taxable = Total - Tax
            val taxable = total - tax

            try {
                val discountStr = String.format(Locale.getDefault(), "%.2f", discountPct)
                val taxableStr = String.format(Locale.getDefault(), "%.2f", taxable)
                val taxStr = String.format(Locale.getDefault(), "%.2f", tax)
                val totalStr = String.format(Locale.getDefault(), "%.2f", total)

                isUpdating = true
                if (dialogBinding.discountPercentageEditText.text.toString() != discountStr) 
                    dialogBinding.discountPercentageEditText.setText(discountStr)
                if (dialogBinding.taxableEditText.text.toString() != taxableStr) 
                    dialogBinding.taxableEditText.setText(taxableStr)
                if (dialogBinding.taxEditText.text.toString() != taxStr) 
                    dialogBinding.taxEditText.setText(taxStr)
                if (dialogBinding.totalEditText.text.toString() != totalStr) 
                    dialogBinding.totalEditText.setText(totalStr)
                isUpdating = false
            } catch (e: Exception) {
                Log.w("AddSaleActivity", "recalc failed", e)
            }
        }

        // Add TextWatchers for live updates
        val generalWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { recalc() }
        }

        dialogBinding.mrpEditText.addTextChangedListener(generalWatcher)
        dialogBinding.salePriceEditText.addTextChangedListener(generalWatcher)
        dialogBinding.quantityEditText.addTextChangedListener(generalWatcher)
        dialogBinding.taxPercentageEditText.addTextChangedListener(generalWatcher)

        // Create dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        // Set window layout params to ensure proper centering
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(android.view.Gravity.CENTER)
        
        dialog.show()
        
        // Register Bluetooth scanner listener for this dialog
        bluetoothScanListener = { barcode ->
            // Auto-fill barcode field when scanned via Bluetooth
            dialogBinding.barcodeEditText.setText(barcode)
            
            // Auto-lookup product by barcode
            loadProductByBarcode(barcode, dialogBinding)
        }
        bluetoothScanListener?.let { bluetoothScannerManager.addScanListener(it) }
        
        // Remove listener when dialog is dismissed
        dialog.setOnDismissListener {
            bluetoothScanListener?.let { bluetoothScannerManager.removeScanListener(it) }
            bluetoothScanListener = null
        }

        // Set dialog width
        try {
            val window = dialog.window
            window?.let {
                val params = it.attributes
                it.setGravity(android.view.Gravity.CENTER)
                val maxWidth = resources.getDimensionPixelSize(R.dimen.dialog_max_width)
                val defaultWidth = resources.getDimensionPixelSize(R.dimen.dialog_default_width)
                val screenWidth = resources.displayMetrics.widthPixels
                val desired = Math.min(screenWidth - (32 * resources.displayMetrics.density).toInt(), maxWidth)
                params.width = if (screenWidth >= maxWidth) desired else defaultWidth
                it.attributes = params
            }
        } catch (e: Exception) {
            Log.w("AddSaleActivity", "Failed to set dialog width", e)
        }

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.totalEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                dialogBinding.addButton.performClick()
                true
            } else {
                false
            }
        }

        dialogBinding.addButton.setOnClickListener {
            val barcode = dialogBinding.barcodeEditText.text.toString()
            val hsn = dialogBinding.hsnEditText.text.toString()
            val productName = dialogBinding.productNameEditText.text.toString().trim()
            val mrp = dialogBinding.mrpEditText.text.toString().toDoubleOrNull() ?: 0.0
            val salePrice = dialogBinding.salePriceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val discountPercentage = dialogBinding.discountPercentageEditText.text.toString().toDoubleOrNull() ?: 0.0
            val quantity = dialogBinding.quantityEditText.text.toString().toIntOrNull() ?: 1
            val taxPercentage = dialogBinding.taxPercentageEditText.text.toString().toDoubleOrNull() ?: 0.0
            val taxable = dialogBinding.taxableEditText.text.toString().toDoubleOrNull() ?: 0.0
            val tax = dialogBinding.taxEditText.text.toString().toDoubleOrNull() ?: 0.0
            val total = dialogBinding.totalEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Clear previous errors
            dialogBinding.productNameLayout.error = null
            dialogBinding.salePriceLayout.error = null
            dialogBinding.quantityLayout.error = null
            dialogBinding.totalLayout.error = null

            // Validation
            if (productName.isEmpty()) {
                dialogBinding.productNameLayout.error = "Required"
                dialogBinding.productNameEditText.requestFocus()
                return@setOnClickListener
            }
            if (salePrice <= 0.0) {
                dialogBinding.salePriceLayout.error = "Enter valid sale price"
                dialogBinding.salePriceEditText.requestFocus()
                return@setOnClickListener
            }
            if (quantity <= 0) {
                dialogBinding.quantityLayout.error = "Enter valid quantity"
                dialogBinding.quantityEditText.requestFocus()
                return@setOnClickListener
            }
            if (total <= 0.0) {
                dialogBinding.totalLayout.error = "Total must be greater than 0"
                dialogBinding.totalEditText.requestFocus()
                return@setOnClickListener
            }

            val saleItem = SaleItem(
                id = System.currentTimeMillis().toString(),
                saleId = "",
                productId = "",
                productBarcode = barcode,
                productName = productName,
                hsn = hsn,
                mrp = mrp,
                salePrice = salePrice,
                discountPercentage = discountPercentage,
                quantity = quantity,
                taxPercentage = taxPercentage,
                taxable = taxable,
                tax = tax,
                total = total
            )

            adapter.addItem(saleItem)
            binding.saleItemsRecyclerView.scrollToPosition(adapter.itemCount - 1)
            updateSummary()
            dialog.dismiss()
        }

        // Wire the refresh button to load product details
        dialogBinding.refreshButton.setOnClickListener {
            val barcode = dialogBinding.barcodeEditText.text.toString().trim()
            if (barcode.isEmpty()) {
                Toast.makeText(this, "Please enter a barcode", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadProductByBarcode(barcode, dialogBinding)
        }

        // Wire the barcode scan button
        dialogBinding.barcodeLayout.setEndIconOnClickListener {
            val scannerHost = dialogBinding.root.findViewById<android.widget.FrameLayout>(R.id.scannerHost)
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestCameraPermissionAndStart {
                    showScannerInHost(scannerHost, dialogBinding)
                }
            } else {
                showScannerInHost(scannerHost, dialogBinding)
            }
        }
    }

    private fun requestCameraPermissionAndStart(onGranted: () -> Unit) {
        onCameraGrantedCallback = onGranted
        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    private fun showScannerInHost(scannerHost: android.widget.FrameLayout, dialogBinding: DialogAddSaleItemBinding) {
        try {
            val scannerView = InlineBarcodeScanner(this)
            scannerHost.removeAllViews()
            scannerHost.addView(scannerView)
            scannerHost.visibility = View.VISIBLE

            val closeBtn = scannerView.findViewById<android.widget.ImageButton>(R.id.closeScannerButton)
            closeBtn.setOnClickListener {
                try { scannerView.stopScanning() } catch (e: Exception) {}
                scannerHost.visibility = View.GONE
            }

            var lastScanned: String? = null
            var lastScannedAt = 0L
            scannerView.startScanning { code ->
                val now = System.currentTimeMillis()
                if (code == lastScanned && now - lastScannedAt < 1500L) {
                    return@startScanning
                }
                lastScanned = code
                lastScannedAt = now

                dialogBinding.barcodeEditText.setText(code)
                scannerView.stopScanning()
                scannerHost.visibility = View.GONE

                // Auto-load product details after scanning
                loadProductByBarcode(code, dialogBinding)
            }
        } catch (e: Exception) {
            Log.e("AddSaleActivity", "Failed to start scanner", e)
            Toast.makeText(this, "Scanner failed to start", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProductByBarcode(barcode: String, dialogBinding: DialogAddSaleItemBinding) {
        lifecycleScope.launch {
            val products = withContext(Dispatchers.IO) { viewModel.findProductsByBarcode(barcode) }
            when {
                products.isEmpty() -> {
                    Toast.makeText(this@AddSaleActivity, "No product found with barcode", Toast.LENGTH_SHORT).show()
                }
                products.size == 1 -> {
                    val p = products[0]
                    dialogBinding.productNameEditText.setText(p.name)
                    dialogBinding.hsnEditText.setText(p.category)
                    dialogBinding.mrpEditText.setText(String.format(Locale.getDefault(), "%.2f", p.mrp))
                    dialogBinding.salePriceEditText.setText(String.format(Locale.getDefault(), "%.2f", p.salePrice))
                }
                else -> {
                    // Multiple products found - show selection dialog with Product Name and MRP
                    val items = products.mapIndexed { index, product -> 
                        "${index + 1}. ${product.name} - MRP: ₹${String.format(Locale.getDefault(), "%.2f", product.mrp)}"
                    }.toTypedArray()
                    
                    AlertDialog.Builder(this@AddSaleActivity)
                        .setTitle("Multiple products found - Select one")
                        .setItems(items) { _: DialogInterface?, which: Int ->
                            val p = products[which]
                            dialogBinding.productNameEditText.setText(p.name)
                            dialogBinding.hsnEditText.setText(p.category)
                            dialogBinding.mrpEditText.setText(String.format(Locale.getDefault(), "%.2f", p.mrp))
                            dialogBinding.salePriceEditText.setText(String.format(Locale.getDefault(), "%.2f", p.salePrice))
                        }
                        .setCancelable(true)
                        .show()
                }
            }
        }
    }

    private fun updateSummary() {
        val totalQty = saleItems.sumOf { it.quantity }
        val totalTaxable = saleItems.sumOf { it.taxable }
        val totalTax = saleItems.sumOf { it.tax }
        val totalAmount = saleItems.sumOf { it.total }

        binding.totalQtyValue.text = totalQty.toString()
        binding.totalTaxableValue.text = String.format(Locale.getDefault(), "%.2f", totalTaxable)
        binding.totalTaxValue.text = String.format(Locale.getDefault(), "%.2f", totalTax)
        binding.totalAmountValue.text = String.format(Locale.getDefault(), "%.2f", totalAmount)
    }

    private fun saveSale() {
        val customerName = binding.customerNameEditText.text.toString()
        val customerAddress = binding.customerAddressEditText.text.toString()
        val customerPhone = binding.customerPhoneEditText.text.toString()
        val saleDateStr = binding.saleDateEditText.text.toString()
        val saleDate: Date = try {
            val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            fmt.parse(saleDateStr) ?: Calendar.getInstance().time
        } catch (e: Exception) {
            Calendar.getInstance().time
        }
        val status = if (binding.activeRadioButton.isChecked) "Active" else "Inactive"

        val totalQty = saleItems.sumOf { it.quantity }
        val totalTaxable = saleItems.sumOf { it.taxable }
        val totalTax = saleItems.sumOf { it.tax }
        val totalAmount = saleItems.sumOf { it.total }

        lifecycleScope.launch {
            try {
                val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
                val idToUse = editingSaleId ?: generateNextSaleId()
                
                if (!editingSaleId.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) { db.saleItemDao().deleteBySaleId(idToUse) }
                }
                
                val currentTime = Calendar.getInstance().time
                val sale = Sale(
                    id = idToUse,
                    customerName = customerName,
                    customerAddress = customerAddress,
                    customerPhone = customerPhone,
                    saleDate = saleDate,
                    addedDate = originalSale?.addedDate ?: currentTime,
                    updatedDate = if (originalSale != null) currentTime else null,
                    totalQty = totalQty,
                    totalTaxable = totalTaxable,
                    totalTax = totalTax,
                    totalAmount = totalAmount,
                    status = status,
                    customerId = selectedCustomerId
                )
                
                viewModel.saveSale(sale, saleItems)
            } catch (e: Exception) {
                Log.w("AddSaleActivity", "Failed to prepare sale for save", e)
            }
        }
    }

    private suspend fun generateNextSaleId(): String {
        return withContext(Dispatchers.IO) {
            val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
            var nextId = 1
            while (true) {
                val existingSale = db.saleDao().getById(nextId.toString())
                if (existingSale == null) {
                    break
                }
                nextId++
            }
            nextId.toString()
        }
    }
}
