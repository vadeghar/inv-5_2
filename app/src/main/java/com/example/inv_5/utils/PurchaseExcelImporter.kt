package com.example.inv_5.utils

import android.content.Context
import android.net.Uri
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Purchase
import com.example.inv_5.data.entities.PurchaseItem
import com.example.inv_5.data.entities.Supplier
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToLong

data class ImportResult(
    val success: Boolean,
    val message: String,
    val purchasesCreated: Int = 0,
    val itemsImported: Int = 0,
    val suppliersCreated: Int = 0,
    val errors: List<String> = emptyList()
)

data class ExcelRow(
    val rowNumber: Int,
    val supplierName: String,
    val contactPerson: String?,
    val phone: String?,
    val address: String?,
    val invoiceNumber: String,
    val purchaseDate: Date,
    val isActive: Boolean,
    val barcode: String,
    val productName: String,
    val hsn: String?,
    val mrp: Double,
    val discountAmt: Double?,
    val discountPct: Double?,
    val rate: Double?,
    val quantity: Int,
    val taxPct: Double
)

data class PurchaseGroup(
    val supplierName: String,
    val purchaseDate: Date,
    val invoiceNumber: String,
    val isActive: Boolean,
    val rows: MutableList<ExcelRow> = mutableListOf()
)

object PurchaseExcelImporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    suspend fun importPurchases(context: Context, uri: Uri): ImportResult {
        val errors = mutableListOf<String>()
        var purchasesCreated = 0
        var itemsImported = 0
        var suppliersCreated = 0

        try {
            // Read Excel file
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ImportResult(false, "Failed to open file")

            val workbook: Workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            // Parse rows
            val rows = parseExcelRows(sheet, errors)
            if (rows.isEmpty()) {
                workbook.close()
                inputStream.close()
                return ImportResult(
                    false,
                    "No valid data found in Excel file",
                    errors = errors
                )
            }

            // Group by supplier + invoice number
            val purchaseGroups = groupPurchases(rows)

            // Get database
            val db = DatabaseProvider.getInstance(context)

            // Process each purchase group
            for (group in purchaseGroups) {
                try {
                    // Get or create supplier
                    val supplier = getOrCreateSupplier(
                        db,
                        group.supplierName,
                        group.rows.first().contactPerson,
                        group.rows.first().phone,
                        group.rows.first().address
                    )
                    if (supplier != null && supplier.addedDt == Date()) {
                        suppliersCreated++
                    }

                    // Create purchase with items
                    val result = createPurchaseWithItems(db, group, supplier?.id, errors)
                    if (result.first) {
                        purchasesCreated++
                        itemsImported += result.second
                    }
                } catch (e: Exception) {
                    errors.add("Purchase '${group.invoiceNumber}': ${e.message}")
                }
            }

            workbook.close()
            inputStream.close()

            return if (purchasesCreated > 0) {
                ImportResult(
                    success = true,
                    message = "Import successful! $purchasesCreated purchases, $itemsImported items imported",
                    purchasesCreated = purchasesCreated,
                    itemsImported = itemsImported,
                    suppliersCreated = suppliersCreated,
                    errors = errors
                )
            } else {
                ImportResult(
                    success = false,
                    message = "No purchases were imported. Please check the file format.",
                    errors = errors
                )
            }

        } catch (e: Exception) {
            return ImportResult(
                success = false,
                message = "Import failed: ${e.message}",
                errors = errors + e.message.toString()
            )
        }
    }

    private fun parseExcelRows(sheet: Sheet, errors: MutableList<String>): List<ExcelRow> {
        val rows = mutableListOf<ExcelRow>()
        
        // Skip header row (row 0)
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            
            try {
                // Skip empty rows or sample rows (check if all cells are empty or "(auto-calc)")
                if (isEmptyRow(row)) continue

                val excelRow = ExcelRow(
                    rowNumber = i + 1,
                    supplierName = getCellStringValue(row, 0).trim(),
                    contactPerson = getCellStringValue(row, 1).takeIf { it.isNotBlank() },
                    phone = getCellStringValue(row, 2).takeIf { it.isNotBlank() },
                    address = getCellStringValue(row, 3).takeIf { it.isNotBlank() },
                    invoiceNumber = getCellStringValue(row, 4).trim(),
                    purchaseDate = parseDateCell(row, 5),
                    isActive = getCellStringValue(row, 6).trim().equals("true", ignoreCase = true),
                    barcode = getCellStringValue(row, 7).trim(),
                    productName = getCellStringValue(row, 8).trim(),
                    hsn = getCellStringValue(row, 9).takeIf { it.isNotBlank() },
                    mrp = getCellNumericValue(row, 10),
                    discountAmt = getCellNumericValue(row, 11).takeIf { it > 0 },
                    discountPct = getCellNumericValue(row, 12).takeIf { it > 0 },
                    rate = getCellNumericValue(row, 13).takeIf { it > 0 },
                    quantity = getCellNumericValue(row, 14).toInt(),
                    taxPct = getCellNumericValue(row, 16)
                )

                // Validate required fields
                if (excelRow.supplierName.isEmpty()) {
                    errors.add("Row ${i + 1}: Supplier name is required")
                    continue
                }
                if (excelRow.invoiceNumber.isEmpty()) {
                    errors.add("Row ${i + 1}: Invoice number is required")
                    continue
                }
                if (excelRow.barcode.isEmpty()) {
                    errors.add("Row ${i + 1}: Barcode is required")
                    continue
                }
                if (excelRow.productName.isEmpty()) {
                    errors.add("Row ${i + 1}: Product name is required")
                    continue
                }
                if (excelRow.quantity <= 0) {
                    errors.add("Row ${i + 1}: Quantity must be greater than 0")
                    continue
                }

                rows.add(excelRow)
            } catch (e: Exception) {
                errors.add("Row ${i + 1}: ${e.message}")
            }
        }

        return rows
    }

    private fun isEmptyRow(row: Row): Boolean {
        // Check if all important cells are empty
        val supplierName = getCellStringValue(row, 0)
        val invoiceNumber = getCellStringValue(row, 4)
        val barcode = getCellStringValue(row, 7)
        
        return supplierName.isBlank() && invoiceNumber.isBlank() && barcode.isBlank()
    }

    private fun groupPurchases(rows: List<ExcelRow>): List<PurchaseGroup> {
        val groups = mutableMapOf<String, PurchaseGroup>()

        for (row in rows) {
            // Group by supplier name + invoice number
            val key = "${row.supplierName}_${row.invoiceNumber}"
            
            val group = groups.getOrPut(key) {
                PurchaseGroup(
                    supplierName = row.supplierName,
                    purchaseDate = row.purchaseDate,
                    invoiceNumber = row.invoiceNumber,
                    isActive = row.isActive
                )
            }
            
            group.rows.add(row)
        }

        return groups.values.toList()
    }

    private suspend fun getOrCreateSupplier(
        db: com.example.inv_5.data.database.AppDatabase,
        name: String,
        contactPerson: String?,
        phone: String?,
        address: String?
    ): Supplier? {
        // Try to find existing supplier by name
        val suppliersByName = db.supplierDao().searchSuppliers(name, 10, 0)
        val exactNameMatch = suppliersByName.find { it.name.equals(name, ignoreCase = true) }
        
        if (exactNameMatch != null) {
            return exactNameMatch
        }

        // Try to find existing supplier by phone number if provided
        if (!phone.isNullOrBlank()) {
            val suppliersByPhone = db.supplierDao().searchSuppliers(phone, 10, 0)
            val exactPhoneMatch = suppliersByPhone.find { 
                it.phone?.equals(phone, ignoreCase = true) == true 
            }
            
            if (exactPhoneMatch != null) {
                return exactPhoneMatch
            }
        }

        // Create new supplier
        val newSupplier = Supplier(
            id = UUID.randomUUID().toString(),
            name = name,
            contactPerson = contactPerson,
            phone = phone,
            email = null,
            address = address,
            isActive = true,
            addedDt = Date(),
            updatedDt = Date()
        )

        db.supplierDao().insertSupplier(newSupplier)
        return newSupplier
    }

    private suspend fun createPurchaseWithItems(
        db: com.example.inv_5.data.database.AppDatabase,
        group: PurchaseGroup,
        supplierId: String?,
        errors: MutableList<String>
    ): Pair<Boolean, Int> {
        // Group items by barcode + MRP to detect duplicates
        val itemGroups = mutableMapOf<String, MutableList<ExcelRow>>()
        
        for (row in group.rows) {
            val key = "${row.barcode}_${row.mrp}"
            itemGroups.getOrPut(key) { mutableListOf() }.add(row)
        }

        // Create purchase items
        val purchaseItems = mutableListOf<PurchaseItem>()
        var totalQty = 0
        var totalTaxable = 0.0
        var totalAmount = 0.0

        for ((key, rows) in itemGroups) {
            // Sum quantities for duplicate barcode+MRP
            val totalQuantity = rows.sumOf { it.quantity }
            val firstRow = rows.first()

            // Calculate rate
            val rate = calculateRate(firstRow)
            
            // Calculate total, taxable, and tax
            // Correct formula with tax included in total:
            // Total = Rate × Quantity (invoice amount with tax included)
            // Taxable = Total / (1 + Tax%/100) (original amount before tax)
            // Tax Amount = Total - Taxable (ensures sum equals total)
            val total = rate * totalQuantity
            val taxableRaw = if (firstRow.taxPct > 0) {
                total / (1 + (firstRow.taxPct / 100.0))
            } else {
                total
            }
            // Round taxable to 2 decimals first
            val taxable = (taxableRaw * 100).roundToLong() / 100.0
            // Calculate tax as difference to ensure taxable + tax = total
            val taxAmount = total - taxable

            // Get or create product
            val product = getOrCreateProduct(db, firstRow, rate)

            val purchaseItem = PurchaseItem(
                id = UUID.randomUUID().toString(),
                purchaseId = "", // Will be set after creating purchase
                productId = product.id,
                productBarcode = firstRow.barcode,
                productName = firstRow.productName,
                productSalePrice = rate,
                hsn = firstRow.hsn ?: "",
                mrp = firstRow.mrp,
                discountAmount = firstRow.discountAmt ?: 0.0,
                discountPercentage = firstRow.discountPct ?: 0.0,
                rate = rate,
                quantity = totalQuantity,
                taxable = taxable,
                tax = taxAmount,
                total = total
            )

            purchaseItems.add(purchaseItem)
            totalQty += totalQuantity
            totalTaxable += taxable
            totalAmount += total

            // Update product stock
            db.productDao().updateQuantity(product.id, product.quantityOnHand + totalQuantity)
        }

        // Create purchase
        val purchase = Purchase(
            id = UUID.randomUUID().toString(),
            vendor = group.supplierName,
            supplierId = supplierId,
            totalAmount = totalAmount,
            invoiceNo = group.invoiceNumber,
            invoiceDate = group.purchaseDate,
            addedDate = Date(),
            updatedDate = Date(),
            totalQty = totalQty,
            totalTaxable = totalTaxable,
            status = if (group.isActive) "Active" else "Inactive"
        )

        db.purchaseDao().insertPurchase(purchase)

        // Update purchase items with purchase ID and insert
        purchaseItems.forEach { item ->
            item.purchaseId = purchase.id
            db.purchaseItemDao().insertPurchaseItem(item)
        }

        return Pair(true, purchaseItems.size)
    }

    private fun calculateRate(row: ExcelRow): Double {
        // If rate is provided, use it
        if (row.rate != null && row.rate > 0) {
            return row.rate
        }

        // Calculate from MRP and discount
        var rate = row.mrp

        if (row.discountAmt != null && row.discountAmt > 0) {
            // Discount amount provided
            rate -= row.discountAmt
        } else if (row.discountPct != null && row.discountPct > 0) {
            // Discount percentage provided
            rate -= (row.mrp * row.discountPct / 100.0)
        }

        return rate
    }

    private suspend fun getOrCreateProduct(
        db: com.example.inv_5.data.database.AppDatabase,
        row: ExcelRow,
        rate: Double
    ): com.example.inv_5.data.entities.Product {
        // Try to find existing product by barcode
        val existingProducts = db.productDao().getByBarcode(row.barcode)
        val existingProduct = existingProducts.firstOrNull()
        
        if (existingProduct != null) {
            return existingProduct
        }

        // Create new product
        val newProduct = com.example.inv_5.data.entities.Product(
            id = UUID.randomUUID().toString(),
            name = row.productName,
            mrp = row.mrp,
            salePrice = rate,
            barCode = row.barcode,
            category = "Imported",
            quantityOnHand = 0, // Will be updated separately
            reorderPoint = 1,
            maximumStockLevel = 100,
            isActive = true,
            addedDt = Date(),
            updatedDt = Date()
        )

        db.productDao().insertProduct(newProduct)
        return newProduct
    }

    private fun getCellStringValue(row: Row, cellIndex: Int): String {
        val cell = row.getCell(cellIndex) ?: return ""
        
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue ?: ""
            CellType.NUMERIC -> {
                // Check if it's a date
                if (DateUtil.isCellDateFormatted(cell)) {
                    dateFormat.format(cell.dateCellValue)
                } else {
                    cell.numericCellValue.toLong().toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue ?: ""
                } catch (e: Exception) {
                    cell.numericCellValue.toString()
                }
            }
            else -> ""
        }
    }

    private fun getCellNumericValue(row: Row, cellIndex: Int): Double {
        val cell = row.getCell(cellIndex) ?: return 0.0
        
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> {
                val value = cell.stringCellValue.trim()
                // Skip auto-calc placeholders
                if (value.contains("auto-calc", ignoreCase = true)) {
                    0.0
                } else {
                    value.toDoubleOrNull() ?: 0.0
                }
            }
            CellType.FORMULA -> {
                try {
                    cell.numericCellValue
                } catch (e: Exception) {
                    0.0
                }
            }
            else -> 0.0
        }
    }

    private fun parseDateCell(row: Row, cellIndex: Int): Date {
        val cell = row.getCell(cellIndex) ?: throw Exception("Date is required")
        
        return when (cell.cellType) {
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue
                } else {
                    throw Exception("Invalid date format")
                }
            }
            CellType.STRING -> {
                val dateStr = cell.stringCellValue.trim()
                try {
                    dateFormat.parse(dateStr) ?: throw Exception("Invalid date: $dateStr")
                } catch (e: Exception) {
                    throw Exception("Date must be in dd/MM/yyyy format. Got: $dateStr")
                }
            }
            else -> throw Exception("Invalid date cell type")
        }
    }
}
