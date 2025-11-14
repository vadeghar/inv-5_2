package com.example.inv_5.ui.settings

import android.Manifest
import android.app.Activity
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
import com.example.inv_5.databinding.FragmentSettingsBinding
import com.example.inv_5.utils.PurchaseExcelExporter
import com.example.inv_5.utils.PurchaseExcelImporter
import com.example.inv_5.utils.SaleExcelExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission required for file operations", Toast.LENGTH_LONG).show()
            }
        }

        // File picker launcher for import
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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupClickListeners()

        return root
    }

    private fun setupClickListeners() {
        binding.btnExportPurchases.setOnClickListener {
            exportPurchasesToExcel()
        }

        binding.btnExportPurchasesSummary.setOnClickListener {
            showDateRangeDialogForSummaryExport()
        }

        binding.btnDownloadTemplate.setOnClickListener {
            downloadTemplate()
        }

        binding.btnImportPurchases.setOnClickListener {
            openFilePicker()
        }

        // Sales export buttons
        binding.btnExportSales.setOnClickListener {
            exportSalesToExcel()
        }

        binding.btnExportSalesSummary.setOnClickListener {
            showDateRangeDialogForSalesSummaryExport()
        }

        binding.btnExportProducts.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnImportProducts.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ uses MANAGE_EXTERNAL_STORAGE
            if (Environment.isExternalStorageManager()) {
                true
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
                false
            }
        } else {
            // Android 10 and below
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                permissionLauncher.launch(permission)
                false
            }
        }
    }

    private fun exportPurchasesToExcel() {
        if (!checkAndRequestPermissions()) {
            Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
            return
        }

        // Show date range dialog
        val dialog = android.app.AlertDialog.Builder(requireContext())
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
            .create()
        
        dialog.show()
    }

    private fun showStartDatePickerForPurchases(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setTitle("Select Start Date")
        datePickerDialog.show()
    }

    private fun showEndDatePickerForPurchases(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 23, 59, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setTitle("Select End Date")
        datePickerDialog.datePicker.minDate = startDate.time
        datePickerDialog.show()
    }

    private fun performExport(startDate: Date?, endDate: Date?) {
        lifecycleScope.launch {
            try {
                binding.btnExportPurchases.isEnabled = false
                binding.btnExportPurchases.text = "Exporting..."

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
                    "Exported successfully!$dateRangeText\nFile: ${file.name}\nLocation: Downloads folder",
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
                binding.btnExportPurchases.text = "Export Purchases to Excel"
            }
        }
    }

    private fun downloadTemplate() {
        if (!checkAndRequestPermissions()) {
            Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.btnDownloadTemplate.isEnabled = false
                binding.btnDownloadTemplate.text = "Generating..."

                val file = withContext(Dispatchers.IO) {
                    PurchaseExcelExporter.generateTemplate(requireContext())
                }

                Toast.makeText(
                    requireContext(),
                    "Template downloaded!\nFile: ${file.name}\nLocation: Downloads folder",
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
        lifecycleScope.launch {
            try {
                binding.btnImportPurchases.isEnabled = false
                binding.btnImportPurchases.text = "Importing..."

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

                    android.app.AlertDialog.Builder(requireContext())
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

                    android.app.AlertDialog.Builder(requireContext())
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
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_2, null)
        
        // Create a custom dialog with date pickers
        val calendar = Calendar.getInstance()
        var startDate: Date? = null
        var endDate: Date? = null
        
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Export Purchase Summary")
            .setMessage("Select date range for export\n(Leave empty to export all)")
            .setPositiveButton("Export All") { _, _ ->
                exportPurchasesSummary(null, null)
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Select Range") { _, _ ->
                showStartDatePicker { start ->
                    startDate = start
                    showEndDatePicker(start) { end ->
                        endDate = end
                        exportPurchasesSummary(startDate, endDate)
                    }
                }
            }
            .create()
        
        dialog.show()
    }

    private fun showStartDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setTitle("Select Start Date")
        datePickerDialog.show()
    }

    private fun showEndDatePicker(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 23, 59, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setTitle("Select End Date")
        datePickerDialog.datePicker.minDate = startDate.time
        datePickerDialog.show()
    }

    private fun exportPurchasesSummary(startDate: Date?, endDate: Date?) {
        if (!checkAndRequestPermissions()) {
            return
        }

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
                binding.btnExportPurchasesSummary.text = "Export Purchase Summary to Excel"
            }
        }
    }

    // ==================== SALES EXPORT FUNCTIONS ====================

    private fun exportSalesToExcel() {
        if (!checkAndRequestPermissions()) {
            return
        }

        // Show date range dialog
        val dialog = android.app.AlertDialog.Builder(requireContext())
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
            .create()
        
        dialog.show()
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
                binding.btnExportSales.text = "Export Sales to Excel"
            }
        }
    }

    private fun showDateRangeDialogForSalesSummaryExport() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
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
            .create()
        
        dialog.show()
    }

    private fun showStartDatePickerForSales(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setTitle("Select Start Date")
        datePickerDialog.show()
    }

    private fun showEndDatePickerForSales(startDate: Date, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 23, 59, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setTitle("Select End Date")
        datePickerDialog.datePicker.minDate = startDate.time
        datePickerDialog.show()
    }

    private fun exportSalesSummary(startDate: Date?, endDate: Date?) {
        if (!checkAndRequestPermissions()) {
            return
        }

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
                binding.btnExportSalesSummary.text = "Export Sale Summary to Excel"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
