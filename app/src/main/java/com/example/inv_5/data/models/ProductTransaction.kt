package com.example.inv_5.data.models

import java.util.Date

/**
 * Data class representing a product transaction (purchase or sale)
 * Used for displaying product movement history
 */
data class ProductTransaction(
    val id: String,
    val date: Date,
    val documentNumber: String,
    val documentType: TransactionType,
    val quantity: Int,
    val rate: Double,
    val amount: Double,
    val runningBalance: Int,
    val customerOrSupplier: String? = null,
    val documentId: String? = null
) {
    enum class TransactionType {
        PURCHASE,
        SALE
    }
}

/**
 * Summary statistics for product history
 */
data class ProductHistorySummary(
    val productId: String,
    val productName: String,
    val productBarcode: String,
    val currentStock: Int,
    val openingBalance: Int,
    val totalPurchases: Int,
    val totalSales: Int,
    val closingBalance: Int
)
