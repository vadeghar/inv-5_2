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

data class COGSData(
    val product: Product,
    val quantitySold: Int,
    val averageCost: Double,
    val totalCOGS: Double,
    val totalRevenue: Double,
    val grossProfit: Double,
    val profitMargin: Double
)

object COGSReportExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Generate Cost of Goods Sold (COGS) Report
     * Shows stock consumed for sales and profit calculations
     */
    suspend fun exportCOGSReport(
        context: Context,
        startDate: Date?,
        endDate: Date?
    ): File {
        val db = DatabaseProvider.getInstance(context)
        
        // Get all sales and sale items
        val allSales = db.saleDao().listAll()
        val allSaleItems = db.saleItemDao().listAll()
        val allPurchaseItems = db.purchaseItemDao().listAll()
        val allProducts = db.productDao().listAll()

        // Filter sales by date range if provided
        val filteredSales = if (startDate != null && endDate != null) {
            allSales.filter { it.saleDate >= startDate && it.saleDate <= endDate }
        } else {
            allSales
        }

        if (filteredSales.isEmpty()) {
            throw Exception("No sales found for the selected period")
        }

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("COGS Report")

        // Create header
        createHeaderRow(sheet, workbook, startDate, endDate)

        // Calculate COGS for each product
        val cogsDataList = mutableListOf<COGSData>()
        var totalCOGS = 0.0
        var totalRevenue = 0.0
        var totalGrossProfit = 0.0

        val productMap = allProducts.associateBy { it.id }

        // Group sale items by product
        val saleItemsByProduct = allSaleItems
            .filter { item -> filteredSales.any { it.id == item.saleId } }
            .groupBy { it.productId }

        for ((productId, saleItems) in saleItemsByProduct) {
            val product = productMap[productId] ?: continue
            
            val quantitySold = saleItems.sumOf { it.quantity }
            
            // Calculate average cost from purchase items (using weighted average)
            val purchaseItemsForProduct = allPurchaseItems.filter { it.productId == productId }
            val averageCost = if (purchaseItemsForProduct.isNotEmpty()) {
                val totalPurchaseQty = purchaseItemsForProduct.sumOf { it.quantity }
                val totalPurchaseValue = purchaseItemsForProduct.sumOf { it.quantity * it.rate }
                if (totalPurchaseQty > 0) totalPurchaseValue / totalPurchaseQty else 0.0
            } else {
                0.0
            }

            val cogs = quantitySold * averageCost
            val revenue = saleItems.sumOf { it.total }
            val grossProfit = revenue - cogs
            val profitMargin = if (revenue > 0) (grossProfit / revenue) * 100 else 0.0

            cogsDataList.add(COGSData(
                product = product,
                quantitySold = quantitySold,
                averageCost = averageCost,
                totalCOGS = cogs,
                totalRevenue = revenue,
                grossProfit = grossProfit,
                profitMargin = profitMargin
            ))

            totalCOGS += cogs
            totalRevenue += revenue
            totalGrossProfit += grossProfit
        }

        // Sort by total COGS (highest first)
        val sortedData = cogsDataList.sortedByDescending { it.totalCOGS }

        // Fill data
        var rowIndex = 2
        for ((index, data) in sortedData.withIndex()) {
            val row = sheet.createRow(rowIndex++)

            row.createCell(0).setCellValue((index + 1).toDouble()) // S.No
            row.createCell(1).setCellValue(data.product.barCode)
            row.createCell(2).setCellValue(data.product.name)
            row.createCell(3).setCellValue(data.product.category)
            row.createCell(4).setCellValue(data.quantitySold.toDouble())
            row.createCell(5).setCellValue(data.averageCost)
            row.createCell(6).setCellValue(data.totalCOGS)
            row.createCell(7).setCellValue(data.totalRevenue)
            row.createCell(8).setCellValue(data.grossProfit)
            row.createCell(9).setCellValue(data.profitMargin)
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
            setCellValue(sortedData.sumOf { it.quantitySold }.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(6).apply {
            setCellValue(totalCOGS)
            cellStyle = totalStyle
        }
        totalRow.createCell(7).apply {
            setCellValue(totalRevenue)
            cellStyle = totalStyle
        }
        totalRow.createCell(8).apply {
            setCellValue(totalGrossProfit)
            cellStyle = totalStyle
        }
        totalRow.createCell(9).apply {
            val overallMargin = if (totalRevenue > 0) (totalGrossProfit / totalRevenue) * 100 else 0.0
            setCellValue(overallMargin)
            cellStyle = totalStyle
        }

        // Set column widths
        setColumnWidths(sheet)

        // Create summary sheet
        createSummarySheet(workbook, sortedData, totalCOGS, totalRevenue, totalGrossProfit, startDate, endDate)

        // Save file
        val dateRangeStr = if (startDate != null && endDate != null) {
            "_${dateFormat.format(startDate).replace("/", "")}_to_${dateFormat.format(endDate).replace("/", "")}"
        } else {
            ""
        }
        val fileName = "COGS_Report${dateRangeStr}_${timestampFormat.format(Date())}.xlsx"
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
            "Cost of Goods Sold (COGS) Report - ${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}"
        } else {
            "Cost of Goods Sold (COGS) Report - All Time"
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
            "Category",
            "Qty Sold",
            "Avg Cost",
            "Total COGS",
            "Total Revenue",
            "Gross Profit",
            "Profit Margin %"
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
        sheet.setColumnWidth(4, 3500)   // Qty Sold
        sheet.setColumnWidth(5, 3500)   // Avg Cost
        sheet.setColumnWidth(6, 4000)   // Total COGS
        sheet.setColumnWidth(7, 4000)   // Total Revenue
        sheet.setColumnWidth(8, 4000)   // Gross Profit
        sheet.setColumnWidth(9, 4000)   // Profit Margin %
    }

    private fun createSummarySheet(
        workbook: Workbook,
        cogsData: List<COGSData>,
        totalCOGS: Double,
        totalRevenue: Double,
        totalGrossProfit: Double,
        startDate: Date?,
        endDate: Date?
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
        titleCell.setCellValue("COGS Report Summary")
        titleCell.cellStyle = titleStyle

        rowIndex++ // Empty row

        // Period
        val periodRow = summarySheet.createRow(rowIndex++)
        periodRow.createCell(0).setCellValue("Period:")
        periodRow.createCell(1).setCellValue(
            if (startDate != null && endDate != null) {
                "${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}"
            } else {
                "All Time"
            }
        )

        rowIndex++ // Empty row

        // Key metrics
        val boldStyle = workbook.createCellStyle()
        val boldFont = workbook.createFont()
        boldFont.bold = true
        boldStyle.setFont(boldFont)

        val totalRevenueRow = summarySheet.createRow(rowIndex++)
        totalRevenueRow.createCell(0).apply {
            setCellValue("Total Revenue:")
            cellStyle = boldStyle
        }
        totalRevenueRow.createCell(1).apply {
            setCellValue(totalRevenue)
            cellStyle = boldStyle
        }

        val totalCOGSRow = summarySheet.createRow(rowIndex++)
        totalCOGSRow.createCell(0).apply {
            setCellValue("Total COGS:")
            cellStyle = boldStyle
        }
        totalCOGSRow.createCell(1).apply {
            setCellValue(totalCOGS)
            cellStyle = boldStyle
        }

        val totalProfitRow = summarySheet.createRow(rowIndex++)
        totalProfitRow.createCell(0).apply {
            setCellValue("Total Gross Profit:")
            cellStyle = boldStyle
        }
        totalProfitRow.createCell(1).apply {
            setCellValue(totalGrossProfit)
            cellStyle = boldStyle
        }

        val marginRow = summarySheet.createRow(rowIndex++)
        marginRow.createCell(0).apply {
            setCellValue("Overall Profit Margin:")
            cellStyle = boldStyle
        }
        marginRow.createCell(1).apply {
            val margin = if (totalRevenue > 0) (totalGrossProfit / totalRevenue) * 100 else 0.0
            setCellValue("${"%.2f".format(margin)}%")
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
            setCellValue("COGS")
            cellStyle = headerStyle
        }
        categoryHeaderRow.createCell(2).apply {
            setCellValue("Revenue")
            cellStyle = headerStyle
        }
        categoryHeaderRow.createCell(3).apply {
            setCellValue("Profit")
            cellStyle = headerStyle
        }

        val categoryGroups = cogsData.groupBy { it.product.category }
        categoryGroups.toSortedMap().forEach { (category, items) ->
            val row = summarySheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(category)
            row.createCell(1).setCellValue(items.sumOf { it.totalCOGS })
            row.createCell(2).setCellValue(items.sumOf { it.totalRevenue })
            row.createCell(3).setCellValue(items.sumOf { it.grossProfit })
        }

        // Set column widths
        summarySheet.setColumnWidth(0, 6000)
        summarySheet.setColumnWidth(1, 5000)
        summarySheet.setColumnWidth(2, 5000)
        summarySheet.setColumnWidth(3, 5000)
    }
}
