package com.example.inv_5.utils

import android.content.Context
import android.os.Environment
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Product
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class OutOfStockProduct(
    val product: Product,
    val lastPurchaseDate: Date?,
    val lastSaleDate: Date?,
    val category: String
)

object OutOfStockReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Generate Out-of-Stock Report
     * Shows all products with zero quantity
     */
    suspend fun exportOutOfStockReport(context: Context): File {
        val db = DatabaseProvider.getInstance(context)
        
        // Get all products with zero quantity
        val allProducts = db.productDao().listAll()
        val outOfStockProducts = allProducts.filter { it.quantityOnHand == 0 }
        
        if (outOfStockProducts.isEmpty()) {
            throw Exception("No out-of-stock products found")
        }

        // Get all purchase and sale items to find last transaction dates
        val allPurchaseItems = db.purchaseItemDao().listAll()
        val allSaleItems = db.saleItemDao().listAll()
        val allPurchases = db.purchaseDao().listAll()
        val allSales = db.saleDao().listAll()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Out of Stock Products")

        // Create header
        createHeaderRow(sheet, workbook)

        // Fill data
        var rowIndex = 2
        val outOfStockData = mutableListOf<OutOfStockProduct>()
        
        for (product in outOfStockProducts) {
            // Find last purchase date for this product
            val lastPurchaseItem = allPurchaseItems
                .filter { it.productId == product.id }
                .mapNotNull { item ->
                    allPurchases.find { it.id == item.purchaseId }?.let { purchase ->
                        item to purchase.invoiceDate
                    }
                }
                .maxByOrNull { it.second }
            
            // Find last sale date for this product
            val lastSaleItem = allSaleItems
                .filter { it.productId == product.id }
                .mapNotNull { item ->
                    allSales.find { it.id == item.saleId }?.let { sale ->
                        item to sale.saleDate
                    }
                }
                .maxByOrNull { it.second }

            outOfStockData.add(OutOfStockProduct(
                product = product,
                lastPurchaseDate = lastPurchaseItem?.second,
                lastSaleDate = lastSaleItem?.second,
                category = product.category
            ))
        }

        // Sort by category, then by product name
        val sortedData = outOfStockData.sortedWith(compareBy({ it.category }, { it.product.name }))

        for ((index, data) in sortedData.withIndex()) {
            val row = sheet.createRow(rowIndex++)
            val product = data.product

            row.createCell(0).setCellValue((index + 1).toDouble()) // S.No
            row.createCell(1).setCellValue(product.barCode)
            row.createCell(2).setCellValue(product.name)
            row.createCell(3).setCellValue(data.category)
            row.createCell(4).setCellValue(product.mrp)
            row.createCell(5).setCellValue(product.salePrice)
            row.createCell(6).setCellValue(
                if (data.lastPurchaseDate != null) dateFormat.format(data.lastPurchaseDate) else "Never"
            )
            row.createCell(7).setCellValue(
                if (data.lastSaleDate != null) dateFormat.format(data.lastSaleDate) else "Never"
            )
            row.createCell(8).setCellValue(if (product.isActive) "Active" else "Inactive")
        }

        // Add summary row
        val summaryRow = sheet.createRow(rowIndex + 1)
        val summaryStyle = workbook.createCellStyle()
        val summaryFont = workbook.createFont()
        summaryFont.bold = true
        summaryStyle.setFont(summaryFont)
        
        val summaryCell = summaryRow.createCell(0)
        summaryCell.setCellValue("Total Out-of-Stock Products: ${sortedData.size}")
        summaryCell.cellStyle = summaryStyle

        // Set column widths
        setColumnWidths(sheet)

        // Create summary sheet
        createSummarySheet(workbook, sortedData)

        // Save file
        val fileName = "Out_of_Stock_Report_${timestampFormat.format(Date())}.xlsx"
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
        // Title row
        val titleRow = sheet.createRow(0)
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)
        
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Out-of-Stock Report - ${dateFormat.format(Date())}")
        titleCell.cellStyle = titleStyle

        // Header row
        val headerRow = sheet.createRow(1)
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
            "S.No",
            "Barcode",
            "Product Name",
            "Category",
            "MRP",
            "Sale Price",
            "Last Purchase Date",
            "Last Sale Date",
            "Status"
        )

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
    }

    private fun setColumnWidths(sheet: Sheet) {
        sheet.setColumnWidth(0, 2000)   // S.No
        sheet.setColumnWidth(1, 4000)   // Barcode
        sheet.setColumnWidth(2, 7000)   // Product Name
        sheet.setColumnWidth(3, 4000)   // Category
        sheet.setColumnWidth(4, 3500)   // MRP
        sheet.setColumnWidth(5, 3500)   // Sale Price
        sheet.setColumnWidth(6, 5000)   // Last Purchase Date
        sheet.setColumnWidth(7, 5000)   // Last Sale Date
        sheet.setColumnWidth(8, 3000)   // Status
    }

    private fun createSummarySheet(
        workbook: Workbook,
        outOfStockData: List<OutOfStockProduct>
    ) {
        val summarySheet = workbook.createSheet("Summary")
        var rowIndex = 0

        // Title
        val titleRow = summarySheet.createRow(rowIndex++)
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)
        
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Out-of-Stock Report Summary")
        titleCell.cellStyle = titleStyle

        rowIndex++ // Empty row

        // Total count
        val totalRow = summarySheet.createRow(rowIndex++)
        totalRow.createCell(0).setCellValue("Total Out-of-Stock Products:")
        totalRow.createCell(1).setCellValue(outOfStockData.size.toDouble())

        // Count by category
        val categoryGroups = outOfStockData.groupBy { it.category }
        
        rowIndex++ // Empty row
        val categoryHeaderRow = summarySheet.createRow(rowIndex++)
        val headerStyle = workbook.createCellStyle()
        val headerFont = workbook.createFont()
        headerFont.bold = true
        headerStyle.setFont(headerFont)
        
        categoryHeaderRow.createCell(0).apply {
            setCellValue("Category")
            cellStyle = headerStyle
        }
        categoryHeaderRow.createCell(1).apply {
            setCellValue("Count")
            cellStyle = headerStyle
        }

        categoryGroups.toSortedMap().forEach { (category, products) ->
            val row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(category)
            row.createCell(1).setCellValue(products.size.toDouble())
        }

        // Set column widths
        summarySheet.setColumnWidth(0, 6000)
        summarySheet.setColumnWidth(1, 4000)
    }
}
