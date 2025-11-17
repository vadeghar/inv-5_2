package com.example.inv_5.utils

import android.content.Context
import android.os.Environment
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.PurchaseItem
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class ValuationMethod {
    FIFO,    // First In First Out
    LIFO,    // Last In First Out
    WEIGHTED_AVERAGE
}

data class ProductValuation(
    val product: Product,
    val quantity: Int,
    val valuationRate: Double,
    val inventoryValue: Double,
    val method: ValuationMethod
)

object InventoryValuationReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Generate Inventory Valuation Report
     * Supports FIFO, LIFO, and Weighted Average methods
     */
    suspend fun exportInventoryValuation(
        context: Context,
        method: ValuationMethod
    ): File {
        val db = DatabaseProvider.getInstance(context)
        
        // Get all products with stock
        val allProducts = db.productDao().listAll()
        val productsWithStock = allProducts.filter { it.quantityOnHand > 0 }
        
        if (productsWithStock.isEmpty()) {
            throw Exception("No products with stock found")
        }

        // Get all purchase items and purchases for valuation
        val allPurchaseItems = db.purchaseItemDao().listAll()
        val allPurchases = db.purchaseDao().listAll()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Inventory Valuation - ${method.name}")

        // Create header
        createHeaderRow(sheet, workbook, method)

        // Calculate valuations
        val valuations = mutableListOf<ProductValuation>()
        var totalInventoryValue = 0.0

        for (product in productsWithStock) {
            val valuation = calculateProductValuation(
                product,
                allPurchaseItems.filter { it.productId == product.id },
                allPurchases,
                method
            )
            valuations.add(valuation)
            totalInventoryValue += valuation.inventoryValue
        }

        // Sort by product name
        val sortedValuations = valuations.sortedBy { it.product.name }

        // Fill data
        var rowIndex = 2
        for ((index, valuation) in sortedValuations.withIndex()) {
            val row = sheet.createRow(rowIndex++)
            val product = valuation.product

            row.createCell(0).setCellValue((index + 1).toDouble()) // S.No
            row.createCell(1).setCellValue(product.barCode)
            row.createCell(2).setCellValue(product.name)
            row.createCell(3).setCellValue(product.category)
            row.createCell(4).setCellValue(valuation.quantity.toDouble())
            row.createCell(5).setCellValue(valuation.valuationRate)
            row.createCell(6).setCellValue(valuation.inventoryValue)
            row.createCell(7).setCellValue(product.mrp)
            
            // Potential profit (if sold at MRP)
            val potentialProfit = (product.mrp - valuation.valuationRate) * valuation.quantity
            row.createCell(8).setCellValue(potentialProfit)
        }

        // Add totals row
        val totalRow = sheet.createRow(rowIndex + 1)
        val totalStyle = workbook.createCellStyle()
        val totalFont = workbook.createFont()
        totalFont.bold = true
        totalStyle.setFont(totalFont)
        
        val totalCell = totalRow.createCell(0)
        totalCell.setCellValue("TOTAL")
        totalCell.cellStyle = totalStyle
        
        totalRow.createCell(4).apply {
            setCellValue(sortedValuations.sumOf { it.quantity }.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(6).apply {
            setCellValue(totalInventoryValue)
            cellStyle = totalStyle
        }
        totalRow.createCell(8).apply {
            val totalPotentialProfit = sortedValuations.sumOf { 
                (it.product.mrp - it.valuationRate) * it.quantity 
            }
            setCellValue(totalPotentialProfit)
            cellStyle = totalStyle
        }

        // Set column widths
        setColumnWidths(sheet)

        // Create summary sheet
        createSummarySheet(workbook, sortedValuations, method, totalInventoryValue)

        // Save file
        val fileName = "Inventory_Valuation_${method.name}_${timestampFormat.format(Date())}.xlsx"
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

    private suspend fun calculateProductValuation(
        product: Product,
        purchaseItems: List<PurchaseItem>,
        allPurchases: List<com.example.inv_5.data.entities.Purchase>,
        method: ValuationMethod
    ): ProductValuation {
        if (purchaseItems.isEmpty()) {
            // No purchase history, use MRP as fallback
            return ProductValuation(
                product = product,
                quantity = product.quantityOnHand,
                valuationRate = product.mrp,
                inventoryValue = product.quantityOnHand * product.mrp,
                method = method
            )
        }

        // Get purchase items with dates
        val itemsWithDates = purchaseItems.mapNotNull { item ->
            allPurchases.find { it.id == item.purchaseId }?.let { purchase ->
                item to purchase.invoiceDate
            }
        }.sortedBy { it.second } // Sort by date (oldest first)

        val valuationRate = when (method) {
            ValuationMethod.FIFO -> calculateFIFO(itemsWithDates, product.quantityOnHand)
            ValuationMethod.LIFO -> calculateLIFO(itemsWithDates, product.quantityOnHand)
            ValuationMethod.WEIGHTED_AVERAGE -> calculateWeightedAverage(purchaseItems)
        }

        return ProductValuation(
            product = product,
            quantity = product.quantityOnHand,
            valuationRate = valuationRate,
            inventoryValue = product.quantityOnHand * valuationRate,
            method = method
        )
    }

    /**
     * FIFO: Value closing stock using oldest purchases first
     */
    private fun calculateFIFO(
        itemsWithDates: List<Pair<PurchaseItem, Date>>,
        closingStock: Int
    ): Double {
        var remainingQty = closingStock
        var totalValue = 0.0

        // Start from oldest purchases
        for ((item, _) in itemsWithDates) {
            if (remainingQty <= 0) break
            
            val qtyToValue = minOf(remainingQty, item.quantity)
            totalValue += qtyToValue * item.rate
            remainingQty -= qtyToValue
        }

        return if (closingStock > 0) totalValue / closingStock else 0.0
    }

    /**
     * LIFO: Value closing stock using newest purchases first
     */
    private fun calculateLIFO(
        itemsWithDates: List<Pair<PurchaseItem, Date>>,
        closingStock: Int
    ): Double {
        var remainingQty = closingStock
        var totalValue = 0.0

        // Start from newest purchases (reverse order)
        for ((item, _) in itemsWithDates.reversed()) {
            if (remainingQty <= 0) break
            
            val qtyToValue = minOf(remainingQty, item.quantity)
            totalValue += qtyToValue * item.rate
            remainingQty -= qtyToValue
        }

        return if (closingStock > 0) totalValue / closingStock else 0.0
    }

    /**
     * Weighted Average: Average of all purchase rates weighted by quantity
     */
    private fun calculateWeightedAverage(purchaseItems: List<PurchaseItem>): Double {
        val totalQty = purchaseItems.sumOf { it.quantity }
        val totalValue = purchaseItems.sumOf { it.quantity * it.rate }
        
        return if (totalQty > 0) totalValue / totalQty else 0.0
    }

    private fun createHeaderRow(sheet: Sheet, workbook: Workbook, method: ValuationMethod) {
        // Title row
        val titleRow = sheet.createRow(0)
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)
        
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Inventory Valuation Report (${method.name}) - ${dateFormat.format(Date())}")
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
            "Quantity",
            "Valuation Rate",
            "Inventory Value",
            "MRP",
            "Potential Profit"
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
        sheet.setColumnWidth(4, 3500)   // Quantity
        sheet.setColumnWidth(5, 4000)   // Valuation Rate
        sheet.setColumnWidth(6, 4500)   // Inventory Value
        sheet.setColumnWidth(7, 3500)   // MRP
        sheet.setColumnWidth(8, 4500)   // Potential Profit
    }

    private fun createSummarySheet(
        workbook: Workbook,
        valuations: List<ProductValuation>,
        method: ValuationMethod,
        totalInventoryValue: Double
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
        titleCell.setCellValue("Inventory Valuation Summary (${method.name})")
        titleCell.cellStyle = titleStyle

        rowIndex++ // Empty row

        // Method description
        val methodRow = summarySheet.createRow(rowIndex++)
        methodRow.createCell(0).setCellValue("Valuation Method:")
        methodRow.createCell(1).setCellValue(method.name)

        val descRow = summarySheet.createRow(rowIndex++)
        descRow.createCell(0).setCellValue("Description:")
        descRow.createCell(1).setCellValue(when (method) {
            ValuationMethod.FIFO -> "First In First Out - Oldest purchases valued first"
            ValuationMethod.LIFO -> "Last In First Out - Newest purchases valued first"
            ValuationMethod.WEIGHTED_AVERAGE -> "Weighted average of all purchase rates"
        })

        rowIndex++ // Empty row

        // Total metrics
        val totalProductsRow = summarySheet.createRow(rowIndex++)
        totalProductsRow.createCell(0).setCellValue("Total Products:")
        totalProductsRow.createCell(1).setCellValue(valuations.size.toDouble())

        val totalQtyRow = summarySheet.createRow(rowIndex++)
        totalQtyRow.createCell(0).setCellValue("Total Quantity:")
        totalQtyRow.createCell(1).setCellValue(valuations.sumOf { it.quantity }.toDouble())

        val totalValueRow = summarySheet.createRow(rowIndex++)
        val boldStyle = workbook.createCellStyle()
        val boldFont = workbook.createFont()
        boldFont.bold = true
        boldStyle.setFont(boldFont)
        
        totalValueRow.createCell(0).apply {
            setCellValue("Total Inventory Value:")
            cellStyle = boldStyle
        }
        totalValueRow.createCell(1).apply {
            setCellValue(totalInventoryValue)
            cellStyle = boldStyle
        }

        // Category-wise breakdown
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
            setCellValue("Inventory Value")
            cellStyle = headerStyle
        }

        val categoryGroups = valuations.groupBy { it.product.category }
        categoryGroups.toSortedMap().forEach { (category, items) ->
            val row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(category)
            row.createCell(1).setCellValue(items.sumOf { it.inventoryValue })
        }

        // Set column widths
        summarySheet.setColumnWidth(0, 6000)
        summarySheet.setColumnWidth(1, 6000)
    }
}
