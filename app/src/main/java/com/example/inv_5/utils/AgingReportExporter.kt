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
import java.util.concurrent.TimeUnit

data class AgingBucket(
    val name: String,
    val minDays: Int,
    val maxDays: Int?  // null means no upper limit
)

data class ProductAgingData(
    val product: Product,
    val currentStock: Int,
    val oldestPurchaseDate: Date?,
    val ageDays: Int,
    val agingBucket: String,
    val averageCost: Double,
    val stockValue: Double
)

object AgingReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    // Define aging buckets
    private val AGING_BUCKETS = listOf(
        AgingBucket("0-30 days", 0, 30),
        AgingBucket("31-60 days", 31, 60),
        AgingBucket("61-90 days", 61, 90),
        AgingBucket("91-180 days", 91, 180),
        AgingBucket("181-365 days", 181, 365),
        AgingBucket("Over 1 year", 366, null)
    )

    /**
     * Generate Inventory Aging Report
     * Shows stock age in buckets to identify old/slow-moving stock
     */
    suspend fun exportAgingReport(context: Context): File {
        val db = DatabaseProvider.getInstance(context)
        
        // Get all products with stock
        val allProducts = db.productDao().listAll()
        val productsWithStock = allProducts.filter { it.quantityOnHand > 0 }
        
        if (productsWithStock.isEmpty()) {
            throw Exception("No products with stock found")
        }

        // Get all purchase items and purchases
        val allPurchaseItems = db.purchaseItemDao().listAll()
        val allPurchases = db.purchaseDao().listAll()

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Inventory Aging")

        // Create header
        createHeaderRow(sheet, workbook)

        // Calculate aging for each product
        val agingDataList = mutableListOf<ProductAgingData>()
        val currentDate = Date()

        for (product in productsWithStock) {
            val purchaseItemsForProduct = allPurchaseItems.filter { it.productId == product.id }
            
            // Find oldest purchase date (FIFO basis for aging)
            val oldestPurchaseDate = purchaseItemsForProduct
                .mapNotNull { item ->
                    allPurchases.find { it.id == item.purchaseId }?.invoiceDate
                }
                .minOrNull()

            // Calculate age in days
            val ageDays = if (oldestPurchaseDate != null) {
                val diffInMillis = currentDate.time - oldestPurchaseDate.time
                TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
            } else {
                0
            }

            // Determine aging bucket
            val agingBucket = AGING_BUCKETS.find { bucket ->
                ageDays >= bucket.minDays && (bucket.maxDays == null || ageDays <= bucket.maxDays)
            }?.name ?: "Unknown"

            // Calculate average cost
            val averageCost = if (purchaseItemsForProduct.isNotEmpty()) {
                val totalPurchaseQty = purchaseItemsForProduct.sumOf { it.quantity }
                val totalPurchaseValue = purchaseItemsForProduct.sumOf { it.quantity * it.rate }
                if (totalPurchaseQty > 0) totalPurchaseValue / totalPurchaseQty else 0.0
            } else {
                product.mrp
            }

            val stockValue = product.quantityOnHand * averageCost

            agingDataList.add(ProductAgingData(
                product = product,
                currentStock = product.quantityOnHand,
                oldestPurchaseDate = oldestPurchaseDate,
                ageDays = ageDays,
                agingBucket = agingBucket,
                averageCost = averageCost,
                stockValue = stockValue
            ))
        }

        // Sort by age (oldest first)
        val sortedData = agingDataList.sortedByDescending { it.ageDays }

        // Fill data
        var rowIndex = 2
        for ((index, data) in sortedData.withIndex()) {
            val row = sheet.createRow(rowIndex++)

            // Apply color coding based on age
            val cellStyle = createAgingCellStyle(workbook, data.agingBucket)

            row.createCell(0).setCellValue((index + 1).toDouble()) // S.No
            val cell1 = row.createCell(1)
            cell1.setCellValue(data.product.barCode)
            cell1.setCellStyle(cellStyle)
            
            val cell2 = row.createCell(2)
            cell2.setCellValue(data.product.name)
            cell2.setCellStyle(cellStyle)
            
            row.createCell(3).setCellValue(data.product.category)
            row.createCell(4).setCellValue(data.currentStock.toDouble())
            row.createCell(5).setCellValue(data.averageCost)
            row.createCell(6).setCellValue(data.stockValue)
            row.createCell(7).setCellValue(
                if (data.oldestPurchaseDate != null) dateFormat.format(data.oldestPurchaseDate) else "N/A"
            )
            row.createCell(8).setCellValue(data.ageDays.toDouble())
            
            val cell9 = row.createCell(9)
            cell9.setCellValue(data.agingBucket)
            cell9.setCellStyle(cellStyle)
        }

        // Add totals row
        val totalRow = sheet.createRow(rowIndex + 1)
        val totalStyle = workbook.createCellStyle()
        val totalFont = workbook.createFont()
        totalFont.bold = true
        totalStyle.setFont(totalFont)
        
        val totalCell = totalRow.createCell(0)
        totalCell.setCellValue("TOTAL")
        totalCell.setCellStyle(totalStyle)
        
        val cell4 = totalRow.createCell(4)
        cell4.setCellValue(sortedData.sumOf { it.currentStock }.toDouble())
        cell4.setCellStyle(totalStyle)
        
        val cell6 = totalRow.createCell(6)
        cell6.setCellValue(sortedData.sumOf { it.stockValue })
        cell6.setCellStyle(totalStyle)

        // Set column widths
        setColumnWidths(sheet)

        // Create summary sheets
        createSummarySheet(workbook, sortedData)
        createBucketAnalysisSheet(workbook, sortedData)

        // Save file
        val fileName = "Inventory_Aging_Report_${timestampFormat.format(Date())}.xlsx"
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

    private fun createAgingCellStyle(workbook: Workbook, agingBucket: String): CellStyle {
        val style = workbook.createCellStyle()
        
        // Color code based on aging bucket
        when (agingBucket) {
            "0-30 days" -> {
                // Green - Fresh stock
                style.fillForegroundColor = IndexedColors.LIGHT_GREEN.getIndex()
            }
            "31-60 days" -> {
                // Light Yellow - Normal
                style.fillForegroundColor = IndexedColors.LIGHT_YELLOW.getIndex()
            }
            "61-90 days" -> {
                // Yellow - Attention needed
                style.fillForegroundColor = IndexedColors.YELLOW.getIndex()
            }
            "91-180 days" -> {
                // Orange - Aging stock
                style.fillForegroundColor = IndexedColors.LIGHT_ORANGE.getIndex()
            }
            "181-365 days" -> {
                // Light Red - Old stock
                style.fillForegroundColor = IndexedColors.CORAL.getIndex()
            }
            "Over 1 year" -> {
                // Red - Dead stock
                style.fillForegroundColor = IndexedColors.RED.getIndex()
            }
        }
        
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        return style
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
        titleCell.setCellValue("Inventory Aging Report - ${dateFormat.format(Date())}")
        titleCell.setCellStyle(titleStyle)

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
            "Current Stock",
            "Avg Cost",
            "Stock Value",
            "Oldest Purchase",
            "Age (Days)",
            "Aging Bucket"
        )

        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.setCellStyle(headerStyle)
        }
    }

    private fun setColumnWidths(sheet: Sheet) {
        sheet.setColumnWidth(0, 2000)   // S.No
        sheet.setColumnWidth(1, 4000)   // Barcode
        sheet.setColumnWidth(2, 7000)   // Product Name
        sheet.setColumnWidth(3, 4000)   // Category
        sheet.setColumnWidth(4, 4000)   // Current Stock
        sheet.setColumnWidth(5, 3500)   // Avg Cost
        sheet.setColumnWidth(6, 4000)   // Stock Value
        sheet.setColumnWidth(7, 4500)   // Oldest Purchase
        sheet.setColumnWidth(8, 3500)   // Age (Days)
        sheet.setColumnWidth(9, 4500)   // Aging Bucket
    }

    private fun createSummarySheet(
        workbook: Workbook,
        agingData: List<ProductAgingData>
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
        titleCell.setCellValue("Inventory Aging Summary")
        titleCell.setCellStyle(titleStyle)

        rowIndex++ // Empty row

        // Total metrics
        val totalProductsRow = summarySheet.createRow(rowIndex++)
        totalProductsRow.createCell(0).setCellValue("Total Products with Stock:")
        totalProductsRow.createCell(1).setCellValue(agingData.size.toDouble())

        val totalQtyRow = summarySheet.createRow(rowIndex++)
        totalQtyRow.createCell(0).setCellValue("Total Stock Quantity:")
        totalQtyRow.createCell(1).setCellValue(agingData.sumOf { it.currentStock }.toDouble())

        val totalValueRow = summarySheet.createRow(rowIndex++)
        val boldStyle = workbook.createCellStyle()
        val boldFont = workbook.createFont()
        boldFont.bold = true
        boldStyle.setFont(boldFont)
        
        val valueCell0 = totalValueRow.createCell(0)
        valueCell0.setCellValue("Total Stock Value:")
        valueCell0.setCellStyle(boldStyle)
        
        val valueCell1 = totalValueRow.createCell(1)
        valueCell1.setCellValue(agingData.sumOf { it.stockValue })
        valueCell1.setCellStyle(boldStyle)

        val avgAgeRow = summarySheet.createRow(rowIndex++)
        avgAgeRow.createCell(0).setCellValue("Average Stock Age (Days):")
        val avgAge = if (agingData.isNotEmpty()) agingData.map { it.ageDays }.average() else 0.0
        avgAgeRow.createCell(1).setCellValue(avgAge)

        // Set column widths
        summarySheet.setColumnWidth(0, 7000)
        summarySheet.setColumnWidth(1, 5000)
    }

    private fun createBucketAnalysisSheet(
        workbook: Workbook,
        agingData: List<ProductAgingData>
    ) {
        val bucketSheet = workbook.createSheet("Bucket Analysis")
        var rowIndex = 0

        // Title
        val titleRow = bucketSheet.createRow(rowIndex++)
        val titleStyle = workbook.createCellStyle()
        val titleFont = workbook.createFont()
        titleFont.bold = true
        titleFont.fontHeightInPoints = 14
        titleStyle.setFont(titleFont)
        
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Aging Bucket Analysis")
        titleCell.setCellStyle(titleStyle)

        rowIndex++ // Empty row

        // Headers
        val headerRow = bucketSheet.createRow(rowIndex++)
        val headerStyle = workbook.createCellStyle()
        val headerFont = workbook.createFont()
        headerFont.bold = true
        headerStyle.setFont(headerFont)
        
        val headerCell0 = headerRow.createCell(0)
        headerCell0.setCellValue("Aging Bucket")
        headerCell0.setCellStyle(headerStyle)
        
        val headerCell1 = headerRow.createCell(1)
        headerCell1.setCellValue("Product Count")
        headerCell1.setCellStyle(headerStyle)
        
        val headerCell2 = headerRow.createCell(2)
        headerCell2.setCellValue("Total Quantity")
        headerCell2.setCellStyle(headerStyle)
        
        val headerCell3 = headerRow.createCell(3)
        headerCell3.setCellValue("Stock Value")
        headerCell3.setCellStyle(headerStyle)
        
        val headerCell4 = headerRow.createCell(4)
        headerCell4.setCellValue("% of Total Value")
        headerCell4.setCellStyle(headerStyle)

        // Group by bucket
        val bucketGroups = agingData.groupBy { it.agingBucket }
        val totalValue = agingData.sumOf { it.stockValue }

        // Display in bucket order
        for (bucket in AGING_BUCKETS) {
            val items = bucketGroups[bucket.name] ?: continue
            val row = bucketSheet.createRow(rowIndex++)
            
            val cellStyle = createAgingCellStyle(workbook, bucket.name)
            
            val bucketCell = row.createCell(0)
            bucketCell.setCellValue(bucket.name)
            bucketCell.setCellStyle(cellStyle)
            
            row.createCell(1).setCellValue(items.size.toDouble())
            row.createCell(2).setCellValue(items.sumOf { it.currentStock }.toDouble())
            row.createCell(3).setCellValue(items.sumOf { it.stockValue })
            
            val percentage = if (totalValue > 0) (items.sumOf { it.stockValue } / totalValue) * 100 else 0.0
            row.createCell(4).setCellValue(percentage)
        }

        // Set column widths
        bucketSheet.setColumnWidth(0, 5000)
        bucketSheet.setColumnWidth(1, 4000)
        bucketSheet.setColumnWidth(2, 4000)
        bucketSheet.setColumnWidth(3, 5000)
        bucketSheet.setColumnWidth(4, 5000)
    }
}
