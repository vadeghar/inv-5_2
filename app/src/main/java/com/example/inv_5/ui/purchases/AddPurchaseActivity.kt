package com.example.inv_5.ui.purchases

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.inv_5.data.entities.Purchase
import com.example.inv_5.databinding.ActivityAddPurchaseBinding
import java.util.Calendar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.data.entities.PurchaseItem
import com.example.inv_5.databinding.DialogAddPurchaseItemBinding
import android.text.Editable
import android.text.TextWatcher
import android.text.InputFilter
import android.view.View
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.inv_5.R
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.DialogInterface

class AddPurchaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPurchaseBinding
    private val purchaseItems = mutableListOf<PurchaseItem>()
    private var editingPurchaseId: String? = null
    private var originalPurchase: Purchase? = null
    private lateinit var adapter: PurchaseItemsAdapter
    private lateinit var viewModel: AddPurchaseViewModel
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private var onCameraGrantedCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    Log.i("AddPurchaseActivity", "onCreate: view binding inflated, saveButton id=${binding.saveButton.id}")
        viewModel = ViewModelProvider(this).get(AddPurchaseViewModel::class.java)

        // Pre-register camera permission launcher. Must be done before the Lifecycle is STARTED.
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onCameraGrantedCallback?.invoke()
                onCameraGrantedCallback = null
            } else {
                Toast.makeText(this, "Camera permission needed for barcode scanning", Toast.LENGTH_SHORT).show()
            }
        }

        adapter = PurchaseItemsAdapter(purchaseItems)
        binding.purchaseItemsRecyclerView.adapter = adapter
        binding.purchaseItemsRecyclerView.layoutManager = LinearLayoutManager(this)

        // invoice date EditText with calendar icon
        binding.invoiceDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        // initialize invoice date to today if empty
        val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        if (binding.invoiceDateEditText.text.isNullOrEmpty()) {
            binding.invoiceDateEditText.setText(fmt.format(Calendar.getInstance().time))
        }

        // Add item button in header
        binding.addItemButton.setOnClickListener {
            showAddPurchaseItemDialog()
        }

        // Back button to navigate to Purchases screen
        binding.backToPurchasesButton.setOnClickListener {
            finish() // This will return to the previous activity (Purchases)
        }

        // If started with a purchaseId, preload the purchase and items for edit
        val purchaseId = intent.getStringExtra("purchaseId")
        if (!purchaseId.isNullOrEmpty()) {
            editingPurchaseId = purchaseId
            lifecycleScope.launch {
                try {
                    val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
                    val purchase = withContext(Dispatchers.IO) { db.purchaseDao().getById(purchaseId) }
                    val items = withContext(Dispatchers.IO) { db.purchaseItemDao().listByPurchaseId(purchaseId) }
                    purchase?.let { p ->
                        originalPurchase = p
                        // Set title to indicate edit mode
                        binding.headerTitle.text = "Edit Purchase"
                        // populate header fields
                        binding.vendorEditText.setText(p.vendor)
                        binding.invoiceNoEditText.setText(p.invoiceNo)
                        val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                        binding.invoiceDateEditText.setText(fmt.format(p.invoiceDate))
                        if (p.status == "Active") binding.activeRadioButton.isChecked = true else binding.inactiveRadioButton.isChecked = true
                    }
                    // populate items into adapter and internal list
                    purchaseItems.clear()
                    purchaseItems.addAll(items)
                    adapter.notifyDataSetChanged()
                    updateSummary()
                } catch (e: Exception) {
                    Log.e("AddPurchaseActivity", "Failed to load purchase for edit", e)
                }
            }
        }

        binding.saveButton.setOnClickListener {
            // save purchase header + items
            Log.i("AddPurchaseActivity", "Save button clicked; items=${adapter.itemCount}")
            try {
                // use Activity's savePurchase() helper which collects UI and uses `purchaseItems`
                savePurchase()
                Log.i("AddPurchaseActivity", "savePurchase() invoked")
            } catch (ex: Exception) {
                Log.e("AddPurchaseActivity", "Exception calling savePurchase", ex)
                Toast.makeText(this, "Save failed to start: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }

        // extra touch listener to ensure user taps are reaching the button
        binding.saveButton.setOnTouchListener { v, event ->
            Log.i("AddPurchaseActivity", "saveButton touched: event=$event")
            // return false so onClick still runs
            false
        }

        // observe save status from ViewModel to provide visible feedback and capture errors
        viewModel.saveStatus.observe(this) { status ->
            when {
                status == null -> {
                    // no-op
                }
                status == "saving" -> {
                    Log.i("AddPurchaseActivity", "Saving purchase...")
                    // show overlay and disable form interaction
                    binding.loadingOverlay.visibility = android.view.View.VISIBLE
                }
                status == "success" -> {
                    Log.i("AddPurchaseActivity", "Purchase saved successfully")
                    Toast.makeText(this, "Purchase saved", Toast.LENGTH_SHORT).show()
                    // hide overlay and return to Purchases list
                    binding.loadingOverlay.visibility = android.view.View.GONE
                    setResult(RESULT_OK)
                    finish()
                }
                status.startsWith("error") -> {
                    Log.e("AddPurchaseActivity", "Save error: $status")
                    Toast.makeText(this, "Error saving purchase: $status", Toast.LENGTH_LONG).show()
                    binding.loadingOverlay.visibility = android.view.View.GONE
                }
                else -> {
                    Log.i("AddPurchaseActivity", "Save status: $status")
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("AddPurchaseActivity", "onStart: activity started")
    }

    override fun onResume() {
        super.onResume()
        Log.i("AddPurchaseActivity", "onResume: activity resumed; saveButton attached=${binding.saveButton != null}")
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
                binding.invoiceDateEditText.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showAddPurchaseItemDialog() {
        val dialogBinding = DialogAddPurchaseItemBinding.inflate(layoutInflater)

        // guard to avoid recursive TextWatcher triggers when we programmatically setText()
        var isUpdating = false

        // Input filter to restrict decimals to 2 places
        class DecimalDigitsInputFilter(private val digitsBeforeZero: Int, private val digitsAfterZero: Int) : InputFilter {
            // Build a regex like: ^\d{0,10}(\.\d{0,2})?$
            private val pattern = Regex("""^\d{0,${digitsBeforeZero}}(\.\d{0,${digitsAfterZero}})?$""")
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: android.text.Spanned?, dstart: Int, dend: Int): CharSequence? {
                try {
                    val newVal = (dest?.substring(0, dstart) ?: "") + (source?.subSequence(start, end) ?: "") + (dest?.substring(dend) ?: "")
                    if (newVal.isEmpty()) return null
                    if (pattern.matches(newVal)) return null
                } catch (e: Exception) {
                    // ignore
                }
                return ""
            }
        }

        // apply filter to decimal fields (user input)
        val decimalFilter = DecimalDigitsInputFilter(10, 2)
        dialogBinding.mrpEditText.filters = arrayOf<InputFilter>(decimalFilter)
        dialogBinding.discountAmountEditText.filters = arrayOf<InputFilter>(decimalFilter)
        dialogBinding.discountPercentageEditText.filters = arrayOf<InputFilter>(decimalFilter)
        dialogBinding.rateEditText.filters = arrayOf<InputFilter>(decimalFilter)
        dialogBinding.taxPercentageEditText.filters = arrayOf<InputFilter>(decimalFilter)

    // declare watcher variables so they can be removed/added inside other watchers
    lateinit var generalWatcher: TextWatcher
    lateinit var discountAmountWatcher: TextWatcher
    lateinit var discountPercentageWatcher: TextWatcher
    lateinit var rateWatcher: TextWatcher

    // Helper to recalculate dependent fields
    fun recalc() {
            if (isUpdating) return
            val mrp = dialogBinding.mrpEditText.text.toString().toDoubleOrNull() ?: 0.0
            val discountAmount = dialogBinding.discountAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val discountPercentage = dialogBinding.discountPercentageEditText.text.toString().toDoubleOrNull() ?: 0.0
            var rate = dialogBinding.rateEditText.text.toString().toDoubleOrNull() ?: 0.0
            val quantity = dialogBinding.quantityEditText.text.toString().toIntOrNull() ?: 0
            val taxPercentage = dialogBinding.taxPercentageEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Two-way relation between discount amount and percentage
            try {
                if (discountAmount > 0 && mrp > 0) {
                    val pct = (discountAmount / mrp) * 100.0
                    val pctStr = String.format(Locale.getDefault(), "%.2f", pct)
                    val rateStr = String.format(Locale.getDefault(), "%.2f", (mrp - discountAmount))
                    if (dialogBinding.discountPercentageEditText.text.toString() != pctStr || dialogBinding.rateEditText.text.toString() != rateStr) {
                        isUpdating = true
                        if (dialogBinding.discountPercentageEditText.text.toString() != pctStr) dialogBinding.discountPercentageEditText.setText(pctStr)
                        if (dialogBinding.rateEditText.text.toString() != rateStr) dialogBinding.rateEditText.setText(rateStr)
                        isUpdating = false
                    }
                    rate = mrp - discountAmount
                } else if (discountPercentage > 0 && mrp > 0) {
                    val amt = mrp * (discountPercentage / 100.0)
                    val amtStr = String.format(Locale.getDefault(), "%.2f", amt)
                    val rateStr2 = String.format(Locale.getDefault(), "%.2f", (mrp - amt))
                    if (dialogBinding.discountAmountEditText.text.toString() != amtStr || dialogBinding.rateEditText.text.toString() != rateStr2) {
                        isUpdating = true
                        if (dialogBinding.discountAmountEditText.text.toString() != amtStr) dialogBinding.discountAmountEditText.setText(amtStr)
                        if (dialogBinding.rateEditText.text.toString() != rateStr2) dialogBinding.rateEditText.setText(rateStr2)
                        isUpdating = false
                    }
                    rate = mrp - amt
                } else if (rate > 0 && mrp > 0) {
                    val amt = mrp - rate
                    val pct = if (mrp != 0.0) (amt / mrp) * 100.0 else 0.0
                    val amtStr2 = String.format(Locale.getDefault(), "%.2f", amt)
                    val pctStr2 = String.format(Locale.getDefault(), "%.2f", pct)
                    if (dialogBinding.discountAmountEditText.text.toString() != amtStr2 || dialogBinding.discountPercentageEditText.text.toString() != pctStr2) {
                        isUpdating = true
                        if (dialogBinding.discountAmountEditText.text.toString() != amtStr2) dialogBinding.discountAmountEditText.setText(amtStr2)
                        if (dialogBinding.discountPercentageEditText.text.toString() != pctStr2) dialogBinding.discountPercentageEditText.setText(pctStr2)
                        isUpdating = false
                    }
                }

                val taxable = (dialogBinding.rateEditText.text.toString().toDoubleOrNull() ?: 0.0) * quantity
                val tax = taxable * (taxPercentage / 100.0)
                val total = taxable // per plan

                // display taxable/tax/total in non-editable fields
                val taxableStr = String.format(Locale.getDefault(), "%.2f", taxable)
                val taxStr = String.format(Locale.getDefault(), "%.2f", tax)
                val totalStr = String.format(Locale.getDefault(), "%.2f", total)
                if (dialogBinding.taxableEditText.text.toString() != taxableStr || dialogBinding.taxEditText.text.toString() != taxStr || dialogBinding.totalEditText.text.toString() != totalStr) {
                    isUpdating = true
                    if (dialogBinding.taxableEditText.text.toString() != taxableStr) dialogBinding.taxableEditText.setText(taxableStr)
                    if (dialogBinding.taxEditText.text.toString() != taxStr) dialogBinding.taxEditText.setText(taxStr)
                    if (dialogBinding.totalEditText.text.toString() != totalStr) dialogBinding.totalEditText.setText(totalStr)
                    isUpdating = false
                }
            } catch (e: Exception) {
                // guard against parse errors or other unexpected runtime exceptions while editing
                android.util.Log.w("AddPurchaseActivity", "recalc failed", e)
            }
        }

        // Add TextWatchers for live updates
        generalWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { recalc() }
        }

        // Replace per-keystroke watchers with on-focus-change handlers for three interdependent fields
        dialogBinding.discountAmountEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (isUpdating) return@OnFocusChangeListener
                val txt = dialogBinding.discountAmountEditText.text.toString()
                if (txt.isEmpty()) {
                    isUpdating = true
                    dialogBinding.discountPercentageEditText.setText("")
                    dialogBinding.rateEditText.setText("")
                    isUpdating = false
                } else {
                    // user finished editing discount amount, compute pct and rate if possible
                    val mrp = dialogBinding.mrpEditText.text.toString().toDoubleOrNull() ?: 0.0
                    val discountAmount = txt.toDoubleOrNull() ?: 0.0
                    if (mrp > 0) {
                        val pct = (discountAmount / mrp) * 100.0
                        val pctStr = String.format(Locale.getDefault(), "%.2f", pct)
                        val rateStr = String.format(Locale.getDefault(), "%.2f", (mrp - discountAmount))
                        isUpdating = true
                        dialogBinding.discountPercentageEditText.setText(pctStr)
                        dialogBinding.rateEditText.setText(rateStr)
                        isUpdating = false
                    }
                }
                recalc()
            }
        }

        dialogBinding.discountPercentageEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (isUpdating) return@OnFocusChangeListener
                val txt = dialogBinding.discountPercentageEditText.text.toString()
                if (txt.isEmpty()) {
                    isUpdating = true
                    dialogBinding.discountAmountEditText.setText("")
                    dialogBinding.rateEditText.setText("")
                    isUpdating = false
                } else {
                    val mrp = dialogBinding.mrpEditText.text.toString().toDoubleOrNull() ?: 0.0
                    val pct = txt.toDoubleOrNull() ?: 0.0
                    if (mrp > 0) {
                        val amt = mrp * (pct / 100.0)
                        val amtStr = String.format(Locale.getDefault(), "%.2f", amt)
                        val rateStr = String.format(Locale.getDefault(), "%.2f", (mrp - amt))
                        isUpdating = true
                        dialogBinding.discountAmountEditText.setText(amtStr)
                        dialogBinding.rateEditText.setText(rateStr)
                        isUpdating = false
                    }
                }
                recalc()
            }
        }

        dialogBinding.rateEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (isUpdating) return@OnFocusChangeListener
                val txt = dialogBinding.rateEditText.text.toString()
                if (txt.isEmpty()) {
                    isUpdating = true
                    dialogBinding.discountAmountEditText.setText("")
                    dialogBinding.discountPercentageEditText.setText("")
                    isUpdating = false
                } else {
                    val mrp = dialogBinding.mrpEditText.text.toString().toDoubleOrNull() ?: 0.0
                    val rate = txt.toDoubleOrNull() ?: 0.0
                    if (mrp > 0) {
                        val amt = mrp - rate
                        val pct = if (mrp != 0.0) (amt / mrp) * 100.0 else 0.0
                        val amtStr = String.format(Locale.getDefault(), "%.2f", amt)
                        val pctStr = String.format(Locale.getDefault(), "%.2f", pct)
                        isUpdating = true
                        dialogBinding.discountAmountEditText.setText(amtStr)
                        dialogBinding.discountPercentageEditText.setText(pctStr)
                        isUpdating = false
                    }
                }
                recalc()
            }
        }

        // attach remaining listeners
        dialogBinding.mrpEditText.addTextChangedListener(generalWatcher)
        dialogBinding.quantityEditText.addTextChangedListener(generalWatcher)
        dialogBinding.taxPercentageEditText.addTextChangedListener(generalWatcher)
        // Create a dialog using a transparent AlertDialog but handle title/buttons inside layout
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        dialog.show()

        // Constrain dialog width for tablets and set max/default widths
        try {
            val window = dialog.window
            window?.let {
                val params = it.attributes
                // ensure dialog is centered on large screens
                it.setGravity(android.view.Gravity.CENTER)
                val maxWidth = resources.getDimensionPixelSize(com.example.inv_5.R.dimen.dialog_max_width)
                val defaultWidth = resources.getDimensionPixelSize(com.example.inv_5.R.dimen.dialog_default_width)
                // use min of screen width and configured maxWidth
                val screenWidth = resources.displayMetrics.widthPixels
                val desired = Math.min(screenWidth - (32 * resources.displayMetrics.density).toInt(), maxWidth)
                params.width = if (screenWidth >= maxWidth) desired else defaultWidth
                it.attributes = params
            }
        } catch (e: Exception) {
            Log.w("AddPurchaseActivity", "Failed to set dialog width", e)
        }

        // Wire the in-layout Add and Cancel buttons
        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Handle Enter/Done key on the last editable field to trigger Add button
        dialogBinding.taxPercentageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                dialogBinding.addButton.performClick()
                true
            } else {
                false
            }
        }

        dialogBinding.addButton.setOnClickListener {
            // copy validation & add-item logic from previous positive-button handler
            val barcode = dialogBinding.barcodeEditText.text.toString()
            val hsn = dialogBinding.hsnEditText.text.toString()
            val productName = dialogBinding.productNameEditText.text.toString().trim()
            val mrp = dialogBinding.mrpEditText.text.toString().toDoubleOrNull() ?: 0.0
            val discountAmount = dialogBinding.discountAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
            val discountPercentage = dialogBinding.discountPercentageEditText.text.toString().toDoubleOrNull() ?: 0.0
            val rate = dialogBinding.rateEditText.text.toString().toDoubleOrNull() ?: 0.0
            val salePrice = dialogBinding.salePriceEditText.text.toString().toDoubleOrNull()
            val quantity = dialogBinding.quantityEditText.text.toString().toIntOrNull() ?: 0
            val taxPercentage = dialogBinding.taxPercentageEditText.text.toString().toDoubleOrNull() ?: 0.0

            val taxable = (dialogBinding.taxableEditText.text.toString().toDoubleOrNull() ?: (rate * quantity))
            val tax = (dialogBinding.taxEditText.text.toString().toDoubleOrNull() ?: (taxable * (taxPercentage / 100.0)))
            val total = (dialogBinding.totalEditText.text.toString().toDoubleOrNull() ?: taxable)

            // Clear previous errors on TextInputLayouts
            dialogBinding.productNameLayout.error = null
            dialogBinding.mrpLayout.error = null
            dialogBinding.salePriceLayout.error = null
            dialogBinding.rateLayout.error = null
            dialogBinding.quantityLayout.error = null
            dialogBinding.totalLayout.error = null

            // Validation: Product Name, MRP, Rate/SalePrice, Quantity, Total must be non-empty/non-zero
            if (productName.isEmpty()) {
                dialogBinding.productNameLayout.error = "Required"
                dialogBinding.productNameEditText.requestFocus()
                return@setOnClickListener
            }
            if (mrp <= 0.0) {
                dialogBinding.mrpLayout.error = "Enter valid MRP"
                dialogBinding.mrpEditText.requestFocus()
                return@setOnClickListener
            }
            // Either salePrice (explicit) or rate must be > 0
            val effectiveSalePriceField = if (dialogBinding.salePriceEditText.text.toString().isNotBlank()) dialogBinding.salePriceEditText else dialogBinding.rateEditText
            if ((salePrice ?: rate) <= 0.0) {
                if (effectiveSalePriceField === dialogBinding.salePriceEditText) {
                    dialogBinding.salePriceLayout.error = "Enter sale price or rate"
                } else {
                    dialogBinding.rateLayout.error = "Enter sale price or rate"
                }
                effectiveSalePriceField.requestFocus()
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

            val effectiveRate = salePrice ?: rate
            val purchaseItem = PurchaseItem(
                id = System.currentTimeMillis().toString(),
                purchaseId = "", // Will be set when saving the purchase
                productId = "", // productId no longer mapped directly; keep empty
                productBarcode = barcode,
                productName = productName,
                productSalePrice = salePrice ?: rate,
                hsn = hsn,
                mrp = mrp,
                discountAmount = discountAmount,
                discountPercentage = discountPercentage,
                rate = effectiveRate,
                quantity = quantity,
                taxable = taxable,
                tax = tax,
                total = total
            )

            // use adapter helper so RecyclerView updates correctly
            adapter.addItem(purchaseItem)
            // scroll to the inserted item
            binding.purchaseItemsRecyclerView.scrollToPosition(adapter.itemCount - 1)
            // update summary totals after adding an item
            updateSummary()
            // dismiss dialog after successful add
            dialog.dismiss()
        }

        // Wire the TextInputLayout end icon (barcode scan) to show inline scanner
        dialogBinding.barcodeLayout.setEndIconOnClickListener {
            // show scanner host and request permission as needed
            val scannerHost = dialogBinding.root.findViewById<android.view.View>(com.example.inv_5.R.id.scannerHost) as android.widget.FrameLayout
            // check camera permission
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestCameraPermissionAndStart {
                    showScannerInHost(scannerHost, dialogBinding)
                }
            } else {
                showScannerInHost(scannerHost, dialogBinding)
            }
        }
    }

    // Helper to request camera permission and call continuation on grant
    private fun requestCameraPermissionAndStart(onGranted: () -> Unit) {
        val permission = android.Manifest.permission.CAMERA
        onCameraGrantedCallback = onGranted
        cameraPermissionLauncher.launch(permission)
    }

    private fun showScannerInHost(scannerHost: android.widget.FrameLayout, dialogBinding: com.example.inv_5.databinding.DialogAddPurchaseItemBinding) {
        try {
            // inflate scanner view and attach
            val scannerView = InlineBarcodeScanner(this)
            scannerHost.removeAllViews()
            scannerHost.addView(scannerView)
            scannerHost.visibility = android.view.View.VISIBLE

            // wire close button from inflated view
            val closeBtn = scannerView.findViewById<android.widget.ImageButton>(R.id.closeScannerButton)
            closeBtn.setOnClickListener {
                try { scannerView.stopScanning() } catch (e: Exception) {}
                scannerHost.visibility = android.view.View.GONE
            }

            // start scanning and handle result (dedupe same code within short interval)
            var lastScanned: String? = null
            var lastScannedAt = 0L
            scannerView.startScanning { code ->
                val now = System.currentTimeMillis()
                if (code == lastScanned && now - lastScannedAt < 1500L) {
                    // ignore duplicate rapid scans of the same code
                    Log.i("AddPurchaseActivity", "Ignored duplicate scan for $code")
                    return@startScanning
                }
                lastScanned = code
                lastScannedAt = now

                // populate barcode field and hide scanner
                dialogBinding.barcodeEditText.setText(code)
                scannerView.stopScanning()
                scannerHost.visibility = android.view.View.GONE

                // trigger lookup by barcode only (do not include MRP)
                lifecycleScope.launch {
                    val products = withContext(Dispatchers.IO) { viewModel.findProductsByBarcode(code) }
                    Log.i("AddPurchaseActivity", "barcode lookup for='$code' returned ${products.size} products")
                    when {
                        products.isEmpty() -> {
                            Toast.makeText(this@AddPurchaseActivity, "No product found with barcode", Toast.LENGTH_SHORT).show()
                        }
                        products.size == 1 -> {
                            val p = products[0]
                            dialogBinding.productNameEditText.setText(p.name)
                            dialogBinding.hsnEditText.setText(p.category)
                            dialogBinding.mrpEditText.setText(String.format(Locale.getDefault(), "%.2f", p.mrp))
                        }
                        else -> {
                            val items = products.map { "MRP: ${String.format(Locale.getDefault(), "%.2f", it.mrp)} | Sale: ${String.format(Locale.getDefault(), "%.2f", it.salePrice)}" }.toTypedArray()
                            AlertDialog.Builder(this@AddPurchaseActivity)
                                .setTitle("Select product (by MRP)")
                                .setItems(items) { _: DialogInterface?, which: Int ->
                                    val p = products[which]
                                    dialogBinding.productNameEditText.setText(p.name)
                                    dialogBinding.hsnEditText.setText(p.category)
                                    dialogBinding.mrpEditText.setText(String.format(Locale.getDefault(), "%.2f", p.mrp))
                                }
                                .show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AddPurchaseActivity", "Failed to start scanner", e)
            Toast.makeText(this, "Scanner failed to start", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSummary() {
        // As per plan: totalTaxable = Sum of Taxable, totalTax = Sum of Tax, totalQty = Sum of Quantity, totalAmount = Sum of Total
        val totalQty = purchaseItems.sumOf { it.quantity }
        val totalTaxable = purchaseItems.sumOf { it.taxable }
        val totalTax = purchaseItems.sumOf { it.tax }
        val totalAmount = purchaseItems.sumOf { it.total }

        // format and display
    binding.totalQtyValue.text = totalQty.toString()
    binding.totalTaxableValue.text = String.format(Locale.getDefault(), "%.2f", totalTaxable)
    binding.totalTaxValue.text = String.format(Locale.getDefault(), "%.2f", totalTax)
    binding.totalAmountValue.text = String.format(Locale.getDefault(), "%.2f", totalAmount)
    }

    private fun savePurchase() {
        val vendor = binding.vendorEditText.text.toString()
        val invoiceNo = binding.invoiceNoEditText.text.toString()
        // Parse invoice date from invoiceDateEditText (format dd/MM/yyyy); fallback to current date
        val invoiceDateStr = binding.invoiceDateEditText.text.toString()
        val invoiceDate: Date = try {
            val fmt = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            fmt.parse(invoiceDateStr) ?: Calendar.getInstance().time
        } catch (e: Exception) {
            Calendar.getInstance().time
        }
        val status = if (binding.activeRadioButton.isChecked) "Active" else "Inactive"

        val totalQty = purchaseItems.sumOf { it.quantity }
        val totalTaxable = purchaseItems.sumOf { it.taxable }
        val totalAmount = purchaseItems.sumOf { it.total }

        // If editing, delete existing items for this purchase id before saving updated set
        lifecycleScope.launch {
            try {
                val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
                val idToUse = editingPurchaseId ?: generateNextPurchaseId()
                
                if (!editingPurchaseId.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) { db.purchaseItemDao().deleteByPurchaseId(idToUse) }
                }
                
                val currentTime = Calendar.getInstance().time
                val purchase = Purchase(
                    id = idToUse,
                    vendor = vendor,
                    totalAmount = totalAmount,
                    invoiceNo = invoiceNo,
                    invoiceDate = invoiceDate,
                    addedDate = originalPurchase?.addedDate ?: currentTime,
                    updatedDate = if (originalPurchase != null) currentTime else null,
                    totalQty = totalQty,
                    totalTaxable = totalTaxable,
                    status = status
                )
                
                viewModel.savePurchase(purchase, purchaseItems)
            } catch (e: Exception) {
                Log.w("AddPurchaseActivity", "Failed to clear existing purchase items before save", e)
            }
        }
    }

    private suspend fun generateNextPurchaseId(): String {
        return withContext(Dispatchers.IO) {
            val db = com.example.inv_5.data.database.DatabaseProvider.getInstance(applicationContext)
            
            // Find the highest sequential numeric ID that exists
            var nextId = 1
            while (true) {
                val existingPurchase = db.purchaseDao().getById(nextId.toString())
                if (existingPurchase == null) {
                    break
                }
                nextId++
            }
            nextId.toString()
        }
    }
}