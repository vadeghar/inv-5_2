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

object SaleExcelExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Export all sales with detailed items
     */
    suspend fun exportSales(context: Context, startDate: Date? = null, endDate: Date? = null): File {
        val db = DatabaseProvider.getInstance(context)
        var sales = db.saleDao().listAll()
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            sales = sales.filter { sale ->
                sale.saleDate >= startDate && sale.saleDate <= endDate
            }
        }
        
        if (sales.isEmpty()) {
            throw Exception("No sales found to export")
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sales")

        // Create header row
        createHeaderRow(sheet, workbook)

        // Fill data
        var rowIndex = 1
        var totalSales = 0
        var totalItems = 0
        var grandTotalAmount = 0.0
        
        for (sale in sales) {
            val items = db.saleItemDao().listBySaleId(sale.id)
            val customer = sale.customerId?.let { db.customerDao().getById(it) }

            if (items.isEmpty()) continue
            
            totalSales++
            totalItems += items.size

            for (item in items) {
                val product = db.productDao().getById(item.productId)
                val row = sheet.createRow(rowIndex++)

                // Customer info (same for all items in this sale)
                row.createCell(0).setCellValue(customer?.name ?: sale.customerName)
                row.createCell(1).setCellValue(customer?.contactPerson ?: "")
                row.createCell(2).setCellValue(customer?.phone ?: sale.customerPhone)
                row.createCell(3).setCellValue(customer?.address ?: sale.customerAddress)

                // Sale info
                row.createCell(4).setCellValue(dateFormat.format(sale.saleDate))
                row.createCell(5).setCellValue(if (sale.status == "Active") "True" else "False")

                // Item info
                row.createCell(6).setCellValue(product?.barCode ?: item.productBarcode)
                row.createCell(7).setCellValue(product?.name ?: item.productName)
                row.createCell(8).setCellValue(item.hsn)
                row.createCell(9).setCellValue(item.mrp)
                row.createCell(10).setCellValue(item.salePrice)
                row.createCell(11).setCellValue(item.discountPercentage)
                row.createCell(12).setCellValue(item.quantity.toDouble())
                
                // Calculated fields
                row.createCell(13).setCellValue(item.taxable)
                row.createCell(14).setCellValue(item.taxPercentage)
                row.createCell(15).setCellValue(item.tax)
                row.createCell(16).setCellValue(item.total)
                
                grandTotalAmount += item.total
            }
        }

        // Set column widths manually
        setColumnWidths(sheet)

        // Add summary sheet
        createSummarySheet(workbook, totalSales, totalItems, grandTotalAmount)

        // Save file
        val dateRangeStr = if (startDate != null && endDate != null) {
            "_${dateFormat.format(startDate).replace("/", "")}_to_${dateFormat.format(endDate).replace("/", "")}"
        } else {
            ""
        }
        val fileName = "Sales${dateRangeStr}_${timestampFormat.format(Date())}.xlsx"
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

    /**
     * Export only sale summary (high-level details without items)
     */
    suspend fun exportSalesSummary(context: Context, startDate: Date? = null, endDate: Date? = null): File {
        val db = DatabaseProvider.getInstance(context)
        var sales = db.saleDao().listAll()
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            sales = sales.filter { sale ->
                sale.saleDate >= startDate && sale.saleDate <= endDate
            }
        }
        
        if (sales.isEmpty()) {
            throw Exception("No sales found in the selected date range")
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sale Summary")

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
            "Customer Name",
            "Contact Person",
            "Phone",
            "Sale Date",
            "Total Qty",
            "Total Taxable",
            "Total Tax",
            "Total Amount",
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
        var grandTotalTaxable = 0.0
        var grandTotalTax = 0.0
        var grandTotalAmount = 0.0
        
        for (sale in sales) {
            val customer = sale.customerId?.let { db.customerDao().getById(it) }
            val row = sheet.createRow(rowIndex++)

            row.createCell(0).setCellValue(customer?.name ?: sale.customerName)
            row.createCell(1).setCellValue(customer?.contactPerson ?: "")
            row.createCell(2).setCellValue(customer?.phone ?: sale.customerPhone)
            row.createCell(3).setCellValue(dateFormat.format(sale.saleDate))
            row.createCell(4).setCellValue(sale.totalQty.toDouble())
            row.createCell(5).setCellValue(sale.totalTaxable)
            row.createCell(6).setCellValue(sale.totalTax)
            row.createCell(7).setCellValue(sale.totalAmount)
            row.createCell(8).setCellValue(sale.status)
            
            grandTotalQty += sale.totalQty
            grandTotalTaxable += sale.totalTaxable
            grandTotalTax += sale.totalTax
            grandTotalAmount += sale.totalAmount
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
        
        totalRow.createCell(4).apply {
            setCellValue(grandTotalQty.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(5).apply {
            setCellValue(grandTotalTaxable)
            cellStyle = totalStyle
        }
        totalRow.createCell(6).apply {
            setCellValue(grandTotalTax)
            cellStyle = totalStyle
        }
        totalRow.createCell(7).apply {
            setCellValue(grandTotalAmount)
            cellStyle = totalStyle
        }

        // Set column widths
        sheet.setColumnWidth(0, 6000)  // Customer Name
        sheet.setColumnWidth(1, 5000)  // Contact Person
        sheet.setColumnWidth(2, 4000)  // Phone
        sheet.setColumnWidth(3, 3500)  // Sale Date
        sheet.setColumnWidth(4, 3000)  // Total Qty
        sheet.setColumnWidth(5, 4000)  // Total Taxable
        sheet.setColumnWidth(6, 4000)  // Total Tax
        sheet.setColumnWidth(7, 4000)  // Total Amount
        sheet.setColumnWidth(8, 3000)  // Status

        // Save file
        val dateRangeStr = if (startDate != null && endDate != null) {
            "_${dateFormat.format(startDate).replace("/", "")}_to_${dateFormat.format(endDate).replace("/", "")}"
        } else {
            ""
        }
        val fileName = "Sale_Summary${dateRangeStr}_${timestampFormat.format(Date())}.xlsx"
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

    private fun createHeaderRow(sheet: Sheet, workbook: Workbook) {
        val headerRow = sheet.createRow(0)
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

        val headers = arrayOf(
            "Customer Name",
            "Contact Person",
            "Phone",
            "Address",
            "Sale Date",
            "Active",
            "Bar Code",
            "Product Name",
            "HSN",
            "MRP",
            "Sale Price",
            "Discount %",
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
        sheet.setColumnWidth(0, 6000)   // Customer Name
        sheet.setColumnWidth(1, 5000)   // Contact Person
        sheet.setColumnWidth(2, 4000)   // Phone
        sheet.setColumnWidth(3, 7000)   // Address
        sheet.setColumnWidth(4, 3500)   // Sale Date
        sheet.setColumnWidth(5, 2500)   // Active
        sheet.setColumnWidth(6, 4000)   // Bar Code
        sheet.setColumnWidth(7, 6000)   // Product Name
        sheet.setColumnWidth(8, 3000)   // HSN
        sheet.setColumnWidth(9, 3000)   // MRP
        sheet.setColumnWidth(10, 3500)  // Sale Price
        sheet.setColumnWidth(11, 3500)  // Discount %
        sheet.setColumnWidth(12, 3000)  // Quantity
        sheet.setColumnWidth(13, 3500)  // Taxable
        sheet.setColumnWidth(14, 3000)  // Tax %
        sheet.setColumnWidth(15, 3500)  // Tax Amount
        sheet.setColumnWidth(16, 3500)  // Total
    }

    private fun createSummarySheet(workbook: Workbook, totalSales: Int, totalItems: Int, grandTotal: Double) {
        val summarySheet = workbook.createSheet("Summary")
        
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)

        val labelStyle = workbook.createCellStyle()
        val labelFont = workbook.createFont()
        labelFont.bold = true
        labelStyle.setFont(labelFont)

        var rowIdx = 0
        
        // Title
        var row = summarySheet.createRow(rowIdx++)
        var cell = row.createCell(0)
        cell.setCellValue("Sales Export Summary")
        cell.cellStyle = titleStyle
        
        rowIdx++ // Empty row
        
        // Total Sales
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Sales:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(totalSales.toDouble())
        
        // Total Items
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Items:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(totalItems.toDouble())
        
        // Grand Total
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Grand Total Amount:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(grandTotal)
        
        rowIdx++ // Empty row
        
        // Export info
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Exported on:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()))
        
        summarySheet.setColumnWidth(0, 5000)
        summarySheet.setColumnWidth(1, 5000)
    }
}
