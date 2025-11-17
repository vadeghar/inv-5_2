package com.example.inv_5.utils

import android.content.Context
import android.os.Environment
import com.example.inv_5.data.models.ProductHistorySummary
import com.example.inv_5.data.models.ProductTransaction
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ItemHistoryExporter(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())

    fun exportItemHistory(
        summary: ProductHistorySummary,
        transactions: List<ProductTransaction>,
        dateRangeLabel: String
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Item History")

            // Create styles
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                setBorderBottom(BorderStyle.THIN)
                setBorderTop(BorderStyle.THIN)
                setBorderLeft(BorderStyle.THIN)
                setBorderRight(BorderStyle.THIN)
                alignment = HorizontalAlignment.CENTER
                val font = workbook.createFont()
                font.bold = true
                setFont(font)
            }

            val titleStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont()
                font.bold = true
                font.fontHeightInPoints = 14
                setFont(font)
            }

            val purchaseStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont()
                font.color = IndexedColors.GREEN.index
                setFont(font)
            }

            val saleStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont()
                font.color = IndexedColors.RED.index
                setFont(font)
            }

            var rowNum = 0

            // Title
            var row = sheet.createRow(rowNum++)
            var cell = row.createCell(0)
            cell.setCellValue("Item Movement History")
            cell.cellStyle = titleStyle
            rowNum++

            // Product Info
            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Product:")
            cell = row.createCell(1)
            cell.setCellValue(summary.productName)

            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Barcode:")
            cell = row.createCell(1)
            cell.setCellValue(summary.productBarcode)

            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Current Stock:")
            cell = row.createCell(1)
            cell.setCellValue(summary.currentStock.toDouble())

            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Date Range:")
            cell = row.createCell(1)
            cell.setCellValue(dateRangeLabel)

            rowNum++

            // Summary
            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Summary")
            cell.cellStyle = titleStyle

            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Opening Balance:")
            cell = row.createCell(1)
            cell.setCellValue(summary.openingBalance.toDouble())

            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Total Purchases:")
            cell = row.createCell(1)
            cell.setCellValue(summary.totalPurchases.toDouble())

            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Total Sales:")
            cell = row.createCell(1)
            cell.setCellValue(summary.totalSales.toDouble())

            row = sheet.createRow(rowNum++)
            cell = row.createCell(0)
            cell.setCellValue("Closing Balance:")
            cell = row.createCell(1)
            cell.setCellValue(summary.closingBalance.toDouble())

            rowNum++

            // Transactions header
            row = sheet.createRow(rowNum++)
            val headers = listOf(
                "Date",
                "Document",
                "Type",
                "Quantity",
                "Rate",
                "Amount",
                "Balance",
                "Partner"
            )
            headers.forEachIndexed { index, header ->
                cell = row.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // Transactions data
            transactions.forEach { transaction ->
                row = sheet.createRow(rowNum++)
                
                cell = row.createCell(0)
                cell.setCellValue(dateFormat.format(transaction.date))

                cell = row.createCell(1)
                cell.setCellValue(transaction.documentNumber)

                cell = row.createCell(2)
                cell.setCellValue(transaction.documentType.toString())

                cell = row.createCell(3)
                val quantityStr = when (transaction.documentType) {
                    ProductTransaction.TransactionType.PURCHASE -> "+${transaction.quantity}"
                    ProductTransaction.TransactionType.SALE -> "-${transaction.quantity}"
                }
                cell.setCellValue(quantityStr)
                cell.cellStyle = when (transaction.documentType) {
                    ProductTransaction.TransactionType.PURCHASE -> purchaseStyle
                    ProductTransaction.TransactionType.SALE -> saleStyle
                }

                cell = row.createCell(4)
                cell.setCellValue(transaction.rate)

                cell = row.createCell(5)
                cell.setCellValue(transaction.amount)

                cell = row.createCell(6)
                cell.setCellValue(transaction.runningBalance.toDouble())

                cell = row.createCell(7)
                cell.setCellValue(transaction.customerOrSupplier ?: "")
            }

            // Auto-size columns
            for (i in 0..7) {
                sheet.autoSizeColumn(i)
            }

            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "ItemHistory_${summary.productName.replace(" ", "_")}_$timestamp.xlsx"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
