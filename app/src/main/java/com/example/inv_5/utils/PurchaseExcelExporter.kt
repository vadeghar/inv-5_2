package com.example.inv_5.utils

import android.content.Context
import android.os.Environment
import com.example.inv_5.data.database.DatabaseProvider
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PurchaseExcelExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    suspend fun exportPurchases(context: Context, startDate: Date? = null, endDate: Date? = null): File {
        val db = DatabaseProvider.getInstance(context)
        var purchases = db.purchaseDao().listAll()
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            purchases = purchases.filter { purchase ->
                purchase.invoiceDate >= startDate && purchase.invoiceDate <= endDate
            }
        }
        
        if (purchases.isEmpty()) {
            throw Exception("No purchases found to export")
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Purchases")

        // Create header row
        createHeaderRow(sheet, workbook)

        // Fill data
        var rowIndex = 1
        var totalPurchases = 0
        var totalItems = 0
        var grandTotalAmount = 0.0
        
        for (purchase in purchases) {
            val items = db.purchaseItemDao().listByPurchaseId(purchase.id)
            val supplier = purchase.supplierId?.let { db.supplierDao().getById(it) }

            if (items.isEmpty()) continue
            
            totalPurchases++
            totalItems += items.size

            for (item in items) {
                val product = db.productDao().getById(item.productId)
                val row = sheet.createRow(rowIndex++)

                // Supplier info (same for all items in this purchase)
                row.createCell(0).setCellValue(supplier?.name ?: "")
                row.createCell(1).setCellValue(supplier?.contactPerson ?: "")
                row.createCell(2).setCellValue(supplier?.phone ?: "")
                row.createCell(3).setCellValue(supplier?.address ?: "")

                // Purchase info
                row.createCell(4).setCellValue(purchase.invoiceNo)
                row.createCell(5).setCellValue(dateFormat.format(purchase.invoiceDate))
                row.createCell(6).setCellValue(if (purchase.status == "Active") "True" else "False")

                // Item info
                row.createCell(7).setCellValue(product?.barCode ?: item.productBarcode)
                row.createCell(8).setCellValue(product?.name ?: item.productName)
                row.createCell(9).setCellValue(item.hsn)
                row.createCell(10).setCellValue(item.mrp)
                row.createCell(11).setCellValue(item.discountAmount)
                row.createCell(12).setCellValue(item.discountPercentage)
                row.createCell(13).setCellValue(item.rate)
                row.createCell(14).setCellValue(item.quantity.toDouble())
                
                // Calculated fields (from database)
                row.createCell(15).setCellValue(item.taxable)
                row.createCell(16).setCellValue(item.tax) // This is tax percentage stored in item
                row.createCell(17).setCellValue(item.taxable * (item.tax / 100.0)) // Calculate tax amount
                row.createCell(18).setCellValue(item.total)
                
                grandTotalAmount += item.total
            }
        }

        // Set column widths manually (autoSizeColumn uses AWT which is not available on Android)
        setColumnWidths(sheet)

        // Add summary sheet
        createSummarySheet(workbook, totalPurchases, totalItems, grandTotalAmount)

        // Save file
        val fileName = "Purchases_${timestampFormat.format(Date())}.xlsx"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()

        return file
    }

    private fun createSummarySheet(workbook: Workbook, totalPurchases: Int, totalItems: Int, grandTotal: Double) {
        val summarySheet = workbook.createSheet("Summary")
        
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)
        
        var rowIdx = 0
        
        // Title
        var row = summarySheet.createRow(rowIdx++)
        var cell = row.createCell(0)
        cell.setCellValue("Purchase Export Summary")
        cell.cellStyle = titleStyle
        
        rowIdx++ // Empty row
        
        // Export date
        row = summarySheet.createRow(rowIdx++)
        row.createCell(0).setCellValue("Export Date:")
        row.createCell(1).setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()))
        
        // Total purchases
        row = summarySheet.createRow(rowIdx++)
        row.createCell(0).setCellValue("Total Purchases:")
        row.createCell(1).setCellValue(totalPurchases.toDouble())
        
        // Total items
        row = summarySheet.createRow(rowIdx++)
        row.createCell(0).setCellValue("Total Items:")
        row.createCell(1).setCellValue(totalItems.toDouble())
        
        // Grand total
        row = summarySheet.createRow(rowIdx++)
        row.createCell(0).setCellValue("Grand Total Amount:")
        row.createCell(1).setCellValue(grandTotal)
        
        // Set column widths manually
        summarySheet.setColumnWidth(0, 5000)
        summarySheet.setColumnWidth(1, 4000)
    }

    private fun createHeaderRow(sheet: Sheet, workbook: Workbook) {
        val headerRow = sheet.createRow(0)
        
        // Create header style
        val headerStyle = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        headerStyle.setFont(font)
        headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        
        val headers = arrayOf(
            "Supplier Name",
            "Contact Person",
            "Phone",
            "Address",
            "Invoice Number",
            "Purchase Date",
            "Active",
            "Bar Code",
            "Product Name",
            "HSN",
            "MRP",
            "Discount Amt",
            "Discount %",
            "Rate",
            "Quantity",
            "Taxable",
            "Tax %",
            "Tax Amount",
            "Total"
        )

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
    }

    private fun setColumnWidths(sheet: Sheet) {
        // Set column widths manually (in units of 1/256th of a character width)
        // Approximate widths for better readability
        sheet.setColumnWidth(0, 4000)  // Supplier Name
        sheet.setColumnWidth(1, 3500)  // Contact Person
        sheet.setColumnWidth(2, 3000)  // Phone
        sheet.setColumnWidth(3, 5000)  // Address
        sheet.setColumnWidth(4, 3500)  // Invoice Number
        sheet.setColumnWidth(5, 3500)  // Purchase Date
        sheet.setColumnWidth(6, 2000)  // Active
        sheet.setColumnWidth(7, 3000)  // Bar Code
        sheet.setColumnWidth(8, 5000)  // Product Name
        sheet.setColumnWidth(9, 3000)  // HSN
        sheet.setColumnWidth(10, 2500) // MRP
        sheet.setColumnWidth(11, 3000) // Discount Amt
        sheet.setColumnWidth(12, 3000) // Discount %
        sheet.setColumnWidth(13, 2500) // Rate
        sheet.setColumnWidth(14, 2500) // Quantity
        sheet.setColumnWidth(15, 2500) // Taxable
        sheet.setColumnWidth(16, 2500) // Tax %
        sheet.setColumnWidth(17, 3000) // Tax Amount
        sheet.setColumnWidth(18, 2500) // Total
    }

    /**
     * Generate an empty template Excel file for users to fill in for import
     */
    fun generateTemplate(context: Context): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Purchase Template")

        // Create header row
        createHeaderRow(sheet, workbook)

        // Add a sample row with instructions
        val instructionStyle = workbook.createCellStyle()
        val font = workbook.createFont()
        font.italic = true
        font.color = IndexedColors.GREY_50_PERCENT.index
        instructionStyle.setFont(font)

        val row1 = sheet.createRow(1)
        row1.createCell(0).setCellValue("ABC Suppliers")
        row1.createCell(1).setCellValue("John Doe")
        row1.createCell(2).setCellValue("9876543210")
        row1.createCell(3).setCellValue("123 Market St")
        row1.createCell(4).setCellValue("INV-001")
        row1.createCell(5).setCellValue("14/11/2025")
        row1.createCell(6).setCellValue("True")
        row1.createCell(7).setCellValue("BAR001")
        row1.createCell(8).setCellValue("Product A")
        row1.createCell(9).setCellValue("12345678")
        row1.createCell(10).setCellValue(100.0)
        row1.createCell(11).setCellValue(10.0)
        row1.createCell(12).setCellValue(0.0)
        row1.createCell(13).setCellValue(90.0)
        row1.createCell(14).setCellValue(5.0)
        row1.createCell(15).setCellValue("(auto-calc)")
        row1.createCell(16).setCellValue(18.0)
        row1.createCell(17).setCellValue("(auto-calc)")
        row1.createCell(18).setCellValue("(auto-calc)")

        for (i in 0..18) {
            row1.getCell(i)?.cellStyle = instructionStyle
        }

        // Set column widths manually
        setColumnWidths(sheet)

        // Add instructions sheet
        val instructionsSheet = workbook.createSheet("Instructions")
        addInstructions(instructionsSheet, workbook)

        val fileName = "Purchase_Import_Template.xlsx"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()

        return file
    }

    private fun addInstructions(sheet: Sheet, workbook: Workbook) {
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)

        var rowIdx = 0
        var row = sheet.createRow(rowIdx++)
        var cell = row.createCell(0)
        cell.setCellValue("Purchase Import Instructions")
        cell.cellStyle = titleStyle

        rowIdx++

        val instructions = arrayOf(
            "Column Guidelines:",
            "1. Supplier Name - Required",
            "2. Contact Person - Optional",
            "3. Phone - Optional",
            "4. Address - Optional",
            "5. Invoice Number - Required",
            "6. Purchase Date - Required (Format: dd/MM/yyyy)",
            "7. Active - Required (True/False)",
            "8. Bar Code - Required",
            "9. Product Name - Required",
            "10. HSN - Optional",
            "11. MRP - Required",
            "12. Discount Amt - Optional (Use this OR Discount %)",
            "13. Discount % - Optional (Use this OR Discount Amt)",
            "14. Rate - Optional (Will be calculated)",
            "15. Quantity - Required",
            "16. Taxable - Auto-calculated (Rate × Qty)",
            "17. Tax % - Required",
            "18. Tax Amount - Auto-calculated",
            "19. Total - Auto-calculated",
            "",
            "Import Rules:",
            "• Same Supplier + Same Date = Single Purchase",
            "• Same Barcode + Same MRP = Quantities added",
            "• Provide either Discount Amt OR Discount %"
        )

        for (instruction in instructions) {
            row = sheet.createRow(rowIdx++)
            row.createCell(0).setCellValue(instruction)
        }

        sheet.setColumnWidth(0, 15000)
    }

    /**
     * Export only purchase summary (high-level details without items)
     */
    suspend fun exportPurchasesSummary(context: Context, startDate: Date? = null, endDate: Date? = null): File {
        val db = DatabaseProvider.getInstance(context)
        var purchases = db.purchaseDao().listAll()
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            purchases = purchases.filter { purchase ->
                purchase.invoiceDate >= startDate && purchase.invoiceDate <= endDate
            }
        }
        
        if (purchases.isEmpty()) {
            throw Exception("No purchases found in the selected date range")
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Purchase Summary")

        // Create header style
        val headerStyle = workbook.createCellStyle()
        val headerFont = workbook.createFont()
        headerFont.bold = true
        headerFont.fontHeightInPoints = 11
        headerStyle.setFont(headerFont)
        headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
        headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        headerStyle.borderBottom = BorderStyle.THIN
        headerStyle.borderTop = BorderStyle.THIN
        headerStyle.borderLeft = BorderStyle.THIN
        headerStyle.borderRight = BorderStyle.THIN

        // Create header row
        val headerRow = sheet.createRow(0)
        val headers = arrayOf(
            "Invoice Number",
            "Supplier Name",
            "Contact Person",
            "Phone",
            "Invoice Date",
            "Total Qty",
            "Total Amount",
            "Total Taxable",
            "Status"
        )
        
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        // Fill data
        var rowIndex = 1
        var grandTotalQty = 0
        var grandTotalAmount = 0.0
        var grandTotalTaxable = 0.0
        
        for (purchase in purchases) {
            val supplier = purchase.supplierId?.let { db.supplierDao().getById(it) }
            val row = sheet.createRow(rowIndex++)

            row.createCell(0).setCellValue(purchase.invoiceNo)
            row.createCell(1).setCellValue(supplier?.name ?: "")
            row.createCell(2).setCellValue(supplier?.contactPerson ?: "")
            row.createCell(3).setCellValue(supplier?.phone ?: "")
            row.createCell(4).setCellValue(dateFormat.format(purchase.invoiceDate))
            row.createCell(5).setCellValue(purchase.totalQty.toDouble())
            row.createCell(6).setCellValue(purchase.totalAmount)
            row.createCell(7).setCellValue(purchase.totalTaxable)
            row.createCell(8).setCellValue(purchase.status)
            
            grandTotalQty += purchase.totalQty
            grandTotalAmount += purchase.totalAmount
            grandTotalTaxable += purchase.totalTaxable
        }

        // Add totals row
        val totalRow = sheet.createRow(rowIndex)
        val totalStyle = workbook.createCellStyle()
        val totalFont = workbook.createFont()
        totalFont.bold = true
        totalStyle.setFont(totalFont)
        
        val totalCell = totalRow.createCell(0)
        totalCell.setCellValue("TOTAL")
        totalCell.cellStyle = totalStyle
        
        totalRow.createCell(5).apply {
            setCellValue(grandTotalQty.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(6).apply {
            setCellValue(grandTotalAmount)
            cellStyle = totalStyle
        }
        totalRow.createCell(7).apply {
            setCellValue(grandTotalTaxable)
            cellStyle = totalStyle
        }

        // Set column widths
        sheet.setColumnWidth(0, 4000)  // Invoice Number
        sheet.setColumnWidth(1, 6000)  // Supplier Name
        sheet.setColumnWidth(2, 5000)  // Contact Person
        sheet.setColumnWidth(3, 4000)  // Phone
        sheet.setColumnWidth(4, 3500)  // Invoice Date
        sheet.setColumnWidth(5, 3000)  // Total Qty
        sheet.setColumnWidth(6, 4000)  // Total Amount
        sheet.setColumnWidth(7, 4000)  // Total Taxable
        sheet.setColumnWidth(8, 3000)  // Status

        // Save file
        val dateRangeStr = if (startDate != null && endDate != null) {
            "_${dateFormat.format(startDate).replace("/", "")}_to_${dateFormat.format(endDate).replace("/", "")}"
        } else {
            ""
        }
        val fileName = "Purchase_Summary${dateRangeStr}_${timestampFormat.format(Date())}.xlsx"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        FileOutputStream(file).use { fos ->
            workbook.write(fos)
        }
        workbook.close()

        return file
    }
}
