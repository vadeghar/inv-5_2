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

data class StockMovement(
    val product: Product,
    val openingStock: Int,
    val inward: Int,        // From purchases
    val outward: Int,       // From sales
    val closingBalance: Int
)

object StockReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Generate Stock Summary Report
     * Shows opening stock, inward, outward, and closing balance for all products
     */
    suspend fun exportStockSummary(
        context: Context,
        startDate: Date? = null,
        endDate: Date? = null
    ): File {
        val db = DatabaseProvider.getInstance(context)
        
        // Get all products
        val allProducts = db.productDao().listAll()
        
        if (allProducts.isEmpty()) {
            throw Exception("No products found in inventory")
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Stock Summary")

        // Calculate stock movements for each product
        val stockMovements = mutableListOf<StockMovement>()
        
        for (product in allProducts) {
            val movement = calculateStockMovement(db, product, startDate, endDate)
            stockMovements.add(movement)
        }

        // Create header
        createHeaderRow(sheet, workbook, startDate, endDate)

        // Fill data (start at row 2 since row 0 is title, row 1 is headers)
        var rowIndex = 2
        var totalOpeningStock = 0
        var totalInward = 0
        var totalOutward = 0
        var totalClosingBalance = 0
        var totalStockValue = 0.0
        
        // Get all purchase items to calculate average purchase rates
        val allPurchaseItems = db.purchaseItemDao().listAll()
        
        for (movement in stockMovements) {
            val row = sheet.createRow(rowIndex++)
            val product = movement.product

            // Get average purchase rate for this product
            val purchaseItemsForProduct = allPurchaseItems.filter { it.productId == product.id }
            val avgPurchaseRate = if (purchaseItemsForProduct.isNotEmpty()) {
                purchaseItemsForProduct.map { it.rate }.average()
            } else {
                0.0
            }
            
            // Get HSN from latest purchase item or use empty
            val hsn = purchaseItemsForProduct.lastOrNull()?.hsn ?: ""

            row.createCell(0).setCellValue((rowIndex - 2).toDouble()) // S.No (subtract 2 for title and header rows)
            row.createCell(1).setCellValue(product.barCode)
            row.createCell(2).setCellValue(product.name)
            row.createCell(3).setCellValue(hsn)
            row.createCell(4).setCellValue(movement.openingStock.toDouble())
            row.createCell(5).setCellValue(movement.inward.toDouble())
            row.createCell(6).setCellValue(movement.outward.toDouble())
            row.createCell(7).setCellValue(movement.closingBalance.toDouble())
            row.createCell(8).setCellValue(avgPurchaseRate)
            
            val stockValue = movement.closingBalance * avgPurchaseRate
            row.createCell(9).setCellValue(stockValue)
            
            row.createCell(10).setCellValue(product.mrp)
            row.createCell(11).setCellValue(if (product.isActive) "Active" else "Inactive")

            totalOpeningStock += movement.openingStock
            totalInward += movement.inward
            totalOutward += movement.outward
            totalClosingBalance += movement.closingBalance
            totalStockValue += stockValue
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
            setCellValue(totalOpeningStock.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(5).apply {
            setCellValue(totalInward.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(6).apply {
            setCellValue(totalOutward.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(7).apply {
            setCellValue(totalClosingBalance.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(9).apply {
            setCellValue(totalStockValue)
            cellStyle = totalStyle
        }

        // Set column widths
        setColumnWidths(sheet)

        // Add summary sheet
        createSummarySheet(
            workbook,
            allProducts.size,
            totalOpeningStock,
            totalInward,
            totalOutward,
            totalClosingBalance,
            totalStockValue,
            startDate,
            endDate
        )

        // Save file
        val dateRangeStr = if (startDate != null && endDate != null) {
            "_${dateFormat.format(startDate).replace("/", "")}_to_${dateFormat.format(endDate).replace("/", "")}"
        } else {
            ""
        }
        val fileName = "Stock_Summary${dateRangeStr}_${timestampFormat.format(Date())}.xlsx"
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
     * Calculate stock movement for a product within date range
     */
    private suspend fun calculateStockMovement(
        db: com.example.inv_5.data.database.AppDatabase,
        product: Product,
        startDate: Date?,
        endDate: Date?
    ): StockMovement {
        // If no date range, use current stock as closing and calculate backwards
        if (startDate == null || endDate == null) {
            // Get total purchases and sales for this product (all time)
            val purchaseItems = db.purchaseItemDao().listAll()
                .filter { it.productId == product.id }
            val saleItems = db.saleItemDao().listAll()
                .filter { it.productId == product.id }

            val totalInward = purchaseItems.sumOf { it.quantity }
            val totalOutward = saleItems.sumOf { it.quantity }
            val closingBalance = product.quantityOnHand

            return StockMovement(
                product = product,
                openingStock = 0, // No opening stock for all-time view
                inward = totalInward,
                outward = totalOutward,
                closingBalance = closingBalance
            )
        }

        // With date range: calculate opening, movements, and closing
        // Opening stock = current stock - (purchases after start) + (sales after start)
        val purchasesInRange = db.purchaseItemDao().listAll()
            .filter { item ->
                item.productId == product.id &&
                db.purchaseDao().getById(item.purchaseId)?.let { purchase ->
                    purchase.invoiceDate >= startDate && purchase.invoiceDate <= endDate
                } ?: false
            }

        val salesInRange = db.saleItemDao().listAll()
            .filter { item ->
                item.productId == product.id &&
                db.saleDao().getById(item.saleId)?.let { sale ->
                    sale.saleDate >= startDate && sale.saleDate <= endDate
                } ?: false
            }

        val inward = purchasesInRange.sumOf { it.quantity }
        val outward = salesInRange.sumOf { it.quantity }
        
        // Closing balance is current stock
        val closingBalance = product.quantityOnHand
        
        // Opening = Closing - Inward + Outward
        val openingStock = closingBalance - inward + outward

        return StockMovement(
            product = product,
            openingStock = openingStock.coerceAtLeast(0),
            inward = inward,
            outward = outward,
            closingBalance = closingBalance
        )
    }

    private fun createHeaderRow(sheet: Sheet, workbook: Workbook, startDate: Date?, endDate: Date?) {
        // Title row
        val titleRow = sheet.createRow(0)
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)
        
        val titleCell = titleRow.createCell(0)
        val title = if (startDate != null && endDate != null) {
            "Stock Summary Report - ${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}"
        } else {
            "Stock Summary Report - All Time"
        }
        titleCell.setCellValue(title)
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
            "HSN",
            "Opening Stock",
            "Inward (Purchases)",
            "Outward (Sales)",
            "Closing Balance",
            "Purchase Rate",
            "Stock Value",
            "MRP",
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
        sheet.setColumnWidth(3, 3000)   // HSN
        sheet.setColumnWidth(4, 4000)   // Opening Stock
        sheet.setColumnWidth(5, 5000)   // Inward
        sheet.setColumnWidth(6, 5000)   // Outward
        sheet.setColumnWidth(7, 4500)   // Closing Balance
        sheet.setColumnWidth(8, 4000)   // Purchase Rate
        sheet.setColumnWidth(9, 4000)   // Stock Value
        sheet.setColumnWidth(10, 3500)  // MRP
        sheet.setColumnWidth(11, 3000)  // Status
    }

    private fun createSummarySheet(
        workbook: Workbook,
        totalProducts: Int,
        totalOpeningStock: Int,
        totalInward: Int,
        totalOutward: Int,
        totalClosingBalance: Int,
        totalStockValue: Double,
        startDate: Date?,
        endDate: Date?
    ) {
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
        cell.setCellValue("Stock Summary Report")
        cell.cellStyle = titleStyle
        
        rowIdx++ // Empty row
        
        // Date range
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Report Period:")
        cell.cellStyle = labelStyle
        if (startDate != null && endDate != null) {
            row.createCell(1).setCellValue("${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}")
        } else {
            row.createCell(1).setCellValue("All Time")
        }
        
        rowIdx++ // Empty row
        
        // Total Products
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Products:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(totalProducts.toDouble())
        
        // Total Opening Stock
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Opening Stock:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(totalOpeningStock.toDouble())
        
        // Total Inward
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Inward (Purchases):")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(totalInward.toDouble())
        
        // Total Outward
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Outward (Sales):")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(totalOutward.toDouble())
        
        // Total Closing Balance
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Closing Balance:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(totalClosingBalance.toDouble())
        
        rowIdx++ // Empty row
        
        // Total Stock Value
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Total Stock Value:")
        cell.cellStyle = labelStyle
        val valueCell = row.createCell(1)
        valueCell.setCellValue(totalStockValue)
        
        rowIdx++ // Empty row
        
        // Export info
        row = summarySheet.createRow(rowIdx++)
        cell = row.createCell(0)
        cell.setCellValue("Generated on:")
        cell.cellStyle = labelStyle
        row.createCell(1).setCellValue(SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()))
        
        summarySheet.setColumnWidth(0, 6000)
        summarySheet.setColumnWidth(1, 5000)
    }
}
