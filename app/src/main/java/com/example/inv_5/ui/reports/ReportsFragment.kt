package com.example.inv_5.ui.reports

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.inv_5.R
import com.example.inv_5.databinding.FragmentReportsBinding
import com.example.inv_5.utils.AgingReportExporter
import com.example.inv_5.utils.COGSReportExporter
import com.example.inv_5.utils.InventoryValuationReportExporter
import com.example.inv_5.utils.OutOfStockReportExporter
import com.example.inv_5.utils.PurchaseExcelExporter
import com.example.inv_5.utils.PurchaseExcelImporter
import com.example.inv_5.utils.SaleExcelExporter
import com.example.inv_5.utils.StockReportExporter
import com.example.inv_5.utils.ValuationMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            val message = if (isGranted) {
                "Permission granted"
            } else {
                "Storage permission is required for exports"
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    importPurchasesFromExcel(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        setupClickListeners()
        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnExportStockSummary.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showDateRangeDialogForStockReport()
        }

        binding.btnExportOutOfStock.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            exportOutOfStockReport()
        }

        binding.btnExportInventoryValuation.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showValuationMethodDialog()
        }

        binding.btnExportCOGS.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showDateRangeDialogForCOGS()
        }

        binding.btnExportAgingReport.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            exportAgingReport()
        }

        binding.btnExportPurchases.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            exportPurchasesToExcel()
        }

        binding.btnExportPurchasesSummary.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showDateRangeDialogForSummaryExport()
        }

        binding.btnDownloadTemplate.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            downloadTemplate()
        }

        binding.btnImportPurchases.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            openFilePicker()
        }

        binding.btnExportSales.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            exportSalesToExcel()
        }

        binding.btnExportSalesSummary.setOnClickListener {
            if (!checkAndRequestPermissions()) {
                Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showDateRangeDialogForSalesSummaryExport()
        }

        binding.btnExportProducts.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnImportProducts.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportPurchasesToExcel() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export Purchases")
            .setMessage("Select date range for export\n(Leave empty to export all)")
            .setPositiveButton("Export All") { _, _ ->
                performExport(null, null)
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Select Range") { _, _ ->
                showStartDatePickerForPurchases { start ->
                    showEndDatePickerForPurchases(start) { end ->
                        performExport(start, end)
                    }
                }
            }
            .show()
    }

    private fun showStartDatePickerForPurchases(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select Start Date")
        }.show()
    }

    private fun showEndDatePickerForPurchases(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 23, 59, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select End Date")
            datePicker.minDate = startDate.time
        }.show()
    }

    private fun performExport(startDate: Date?, endDate: Date?) {
        binding.btnExportPurchases.isEnabled = false
        binding.btnExportPurchases.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    PurchaseExcelExporter.exportPurchases(requireContext(), startDate, endDate)
                }

                val dateRangeText = if (startDate != null && endDate != null) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    "\nDate Range: ${sdf.format(startDate)} to ${sdf.format(endDate)}"
                } else {
                    ""
                }

                Toast.makeText(
                    requireContext(),
                    "Purchases exported successfully!$dateRangeText\nFile: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportPurchases.isEnabled = true
                binding.btnExportPurchases.text = getString(R.string.label_purchases_to_excel)
            }
        }
    }

    private fun downloadTemplate() {
        binding.btnDownloadTemplate.isEnabled = false
        binding.btnDownloadTemplate.text = "Generating..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    PurchaseExcelExporter.generateTemplate(requireContext())
                }

                Toast.makeText(
                    requireContext(),
                    "Template downloaded!\nFile: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to generate template: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnDownloadTemplate.isEnabled = true
                binding.btnDownloadTemplate.text = "Download Import Template"
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Excel File"))
    }

    private fun importPurchasesFromExcel(uri: Uri) {
        binding.btnImportPurchases.isEnabled = false
        binding.btnImportPurchases.text = "Importing..."

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    PurchaseExcelImporter.importPurchases(requireContext(), uri)
                }

                if (result.success) {
                    val message = buildString {
                        append("Import Successful!\n\n")
                        append("📦 Purchases Created: ${result.purchasesCreated}\n")
                        append("📋 Items Imported: ${result.itemsImported}\n")
                        append("👥 Suppliers Created: ${result.suppliersCreated}\n")

                        if (result.errors.isNotEmpty()) {
                            append("\n⚠️ Warnings (${result.errors.size}):\n")
                            result.errors.take(5).forEach { error ->
                                append("• $error\n")
                            }
                            if (result.errors.size > 5) {
                                append("• ... and ${result.errors.size - 5} more\n")
                            }
                        }
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle("Import Complete")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val errorMessage = buildString {
                        append("Import Failed!\n\n")
                        append(result.message)
                        if (result.errors.isNotEmpty()) {
                            append("\n\nErrors:\n")
                            result.errors.forEach { error ->
                                append("• $error\n")
                            }
                        }
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle("Import Failed")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK", null)
                        .show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Import failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnImportPurchases.isEnabled = true
                binding.btnImportPurchases.text = "Import Purchases from Excel"
            }
        }
    }

    private fun showDateRangeDialogForSummaryExport() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export Purchase Summary")
            .setMessage("Select date range for export\n(Leave empty to export all)")
            .setPositiveButton("Export All") { _, _ ->
                exportPurchasesSummary(null, null)
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Select Range") { _, _ ->
                showStartDatePicker { start ->
                    showEndDatePicker(start) { end ->
                        exportPurchasesSummary(start, end)
                    }
                }
            }
            .show()
    }

    private fun showStartDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select Start Date")
        }.show()
    }

    private fun showEndDatePicker(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 23, 59, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select End Date")
            datePicker.minDate = startDate.time
        }.show()
    }

    private fun exportPurchasesSummary(startDate: Date?, endDate: Date?) {
        binding.btnExportPurchasesSummary.isEnabled = false
        binding.btnExportPurchasesSummary.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    PurchaseExcelExporter.exportPurchasesSummary(requireContext(), startDate, endDate)
                }

                val dateRangeText = if (startDate != null && endDate != null) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    "\nDate Range: ${sdf.format(startDate)} to ${sdf.format(endDate)}"
                } else {
                    "\nAll purchases exported"
                }

                Toast.makeText(
                    requireContext(),
                    "Purchase summary exported successfully!$dateRangeText\n${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportPurchasesSummary.isEnabled = true
                binding.btnExportPurchasesSummary.text = getString(R.string.label_purchase_summary_to_excel)
            }
        }
    }

    private fun exportSalesToExcel() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export Sales")
            .setMessage("Select date range for export\n(Leave empty to export all)")
            .setPositiveButton("Export All") { _, _ ->
                performSalesExport(null, null)
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Select Range") { _, _ ->
                showStartDatePickerForSales { start ->
                    showEndDatePickerForSales(start) { end ->
                        performSalesExport(start, end)
                    }
                }
            }
            .show()
    }

    private fun performSalesExport(startDate: Date?, endDate: Date?) {
        binding.btnExportSales.isEnabled = false
        binding.btnExportSales.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    SaleExcelExporter.exportSales(requireContext(), startDate, endDate)
                }

                val dateRangeText = if (startDate != null && endDate != null) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    "\nDate Range: ${sdf.format(startDate)} to ${sdf.format(endDate)}"
                } else {
                    ""
                }

                Toast.makeText(
                    requireContext(),
                    "Sales exported successfully!$dateRangeText\n${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportSales.isEnabled = true
                binding.btnExportSales.text = getString(R.string.label_sales_to_excel)
            }
        }
    }

    private fun showDateRangeDialogForSalesSummaryExport() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export Sale Summary")
            .setMessage("Select date range for export\n(Leave empty to export all)")
            .setPositiveButton("Export All") { _, _ ->
                exportSalesSummary(null, null)
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Select Range") { _, _ ->
                showStartDatePickerForSales { start ->
                    showEndDatePickerForSales(start) { end ->
                        exportSalesSummary(start, end)
                    }
                }
            }
            .show()
    }

    private fun showStartDatePickerForSales(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select Start Date")
        }.show()
    }

    private fun showEndDatePickerForSales(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 23, 59, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select End Date")
            datePicker.minDate = startDate.time
        }.show()
    }

    private fun exportSalesSummary(startDate: Date?, endDate: Date?) {
        binding.btnExportSalesSummary.isEnabled = false
        binding.btnExportSalesSummary.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    SaleExcelExporter.exportSalesSummary(requireContext(), startDate, endDate)
                }

                val dateRangeText = if (startDate != null && endDate != null) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    "\nDate Range: ${sdf.format(startDate)} to ${sdf.format(endDate)}"
                } else {
                    "\nAll sales exported"
                }

                Toast.makeText(
                    requireContext(),
                    "Sale summary exported successfully!$dateRangeText\n${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportSalesSummary.isEnabled = true
                binding.btnExportSalesSummary.text = getString(R.string.label_sale_summary_to_excel)
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                true
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
                false
            }
        } else {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                permissionLauncher.launch(permission)
                false
            }
        }
    }

    // Stock Report Functions
    private fun showDateRangeDialogForStockReport() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export Stock Summary Report")
            .setMessage("Select time period for the report")
            .setPositiveButton("Export All") { _, _ ->
                exportStockSummary(null, null)
            }
            .setNeutralButton("Select Date Range") { _, _ ->
                showStartDatePickerForStock { startDate ->
                    showEndDatePickerForStock(startDate) { endDate ->
                        exportStockSummary(startDate, endDate)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showStartDatePickerForStock(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select Start Date")
        }.show()
    }

    private fun showEndDatePickerForStock(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val startCalendar = Calendar.getInstance().apply { time = startDate }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select End Date")
            datePicker.minDate = startCalendar.timeInMillis
        }.show()
    }

    private fun exportStockSummary(startDate: Date?, endDate: Date?) {
        binding.btnExportStockSummary.isEnabled = false
        binding.btnExportStockSummary.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    StockReportExporter.exportStockSummary(requireContext(), startDate, endDate)
                }

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateRangeText = if (startDate != null && endDate != null) {
                    " from ${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}"
                } else {
                    " (All Time)"
                }

                Toast.makeText(
                    requireContext(),
                    "Stock Summary Report$dateRangeText exported successfully to: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportStockSummary.isEnabled = true
                binding.btnExportStockSummary.text = getString(R.string.label_stock_summary_report)
            }
        }
    }

    private fun exportOutOfStockReport() {
        binding.btnExportOutOfStock.isEnabled = false
        binding.btnExportOutOfStock.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    OutOfStockReportExporter.exportOutOfStockReport(requireContext())
                }

                Toast.makeText(
                    requireContext(),
                    "Out-of-Stock Report exported successfully to: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportOutOfStock.isEnabled = true
                binding.btnExportOutOfStock.text = getString(R.string.label_out_of_stock_report)
            }
        }
    }

    private fun showValuationMethodDialog() {
        val methods = arrayOf("FIFO (First In First Out)", "LIFO (Last In First Out)", "Weighted Average")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Valuation Method")
            .setItems(methods) { _, which ->
                val method = when (which) {
                    0 -> ValuationMethod.FIFO
                    1 -> ValuationMethod.LIFO
                    2 -> ValuationMethod.WEIGHTED_AVERAGE
                    else -> ValuationMethod.WEIGHTED_AVERAGE
                }
                exportInventoryValuation(method)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportInventoryValuation(method: ValuationMethod) {
        binding.btnExportInventoryValuation.isEnabled = false
        binding.btnExportInventoryValuation.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    InventoryValuationReportExporter.exportInventoryValuation(requireContext(), method)
                }

                Toast.makeText(
                    requireContext(),
                    "Inventory Valuation Report (${method.name}) exported successfully to: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportInventoryValuation.isEnabled = true
                binding.btnExportInventoryValuation.text = getString(R.string.label_inventory_valuation_report)
            }
        }
    }

    private fun showDateRangeDialogForCOGS() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export COGS Report")
            .setMessage("Select time period for the report")
            .setPositiveButton("Export All") { _, _ ->
                exportCOGSReport(null, null)
            }
            .setNeutralButton("Select Date Range") { _, _ ->
                showStartDatePickerForCOGS { startDate ->
                    showEndDatePickerForCOGS(startDate) { endDate ->
                        exportCOGSReport(startDate, endDate)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showStartDatePickerForCOGS(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select Start Date")
        }.show()
    }

    private fun showEndDatePickerForCOGS(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val startCalendar = Calendar.getInstance().apply { time = startDate }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select End Date")
            datePicker.minDate = startCalendar.timeInMillis
        }.show()
    }

    private fun exportCOGSReport(startDate: Date?, endDate: Date?) {
        binding.btnExportCOGS.isEnabled = false
        binding.btnExportCOGS.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    COGSReportExporter.exportCOGSReport(requireContext(), startDate, endDate)
                }

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateRangeText = if (startDate != null && endDate != null) {
                    " from ${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}"
                } else {
                    " (All Time)"
                }

                Toast.makeText(
                    requireContext(),
                    "COGS Report$dateRangeText exported successfully to: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportCOGS.isEnabled = true
                binding.btnExportCOGS.text = getString(R.string.label_cogs_report)
            }
        }
    }

    private fun exportAgingReport() {
        binding.btnExportAgingReport.isEnabled = false
        binding.btnExportAgingReport.text = "Exporting..."

        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    AgingReportExporter.exportAgingReport(requireContext())
                }

                Toast.makeText(
                    requireContext(),
                    "Inventory Aging Report exported successfully to: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnExportAgingReport.isEnabled = true
                binding.btnExportAgingReport.text = getString(R.string.label_inventory_aging_report)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}