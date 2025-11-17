package com.example.inv_5.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.models.ProductHistorySummary
import com.example.inv_5.data.models.ProductTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class ItemHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getInstance(application)
    private val productDao = db.productDao()
    private val purchaseItemDao = db.purchaseItemDao()
    private val saleItemDao = db.saleItemDao()

    private val _transactions = MutableLiveData<List<ProductTransaction>>()
    val transactions: LiveData<List<ProductTransaction>> = _transactions

    private val _summary = MutableLiveData<ProductHistorySummary>()
    val summary: LiveData<ProductHistorySummary> = _summary

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private var currentProduct: Product? = null
    private var allTransactions: List<ProductTransaction> = emptyList()

    fun loadProductHistory(
        productId: String,
        startDate: Date? = null,
        endDate: Date? = null,
        filterType: ProductTransaction.TransactionType? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val product = withContext(Dispatchers.IO) {
                    productDao.getById(productId)
                }

                if (product != null) {
                    currentProduct = product
                    
                    // Fetch transactions
                    val transactions = withContext(Dispatchers.IO) {
                        fetchTransactions(productId, startDate, endDate)
                    }

                    // Apply filter if needed
                    val filteredTransactions = if (filterType != null) {
                        transactions.filter { it.documentType == filterType }
                    } else {
                        transactions
                    }

                    // Calculate running balance
                    val transactionsWithBalance = calculateRunningBalance(
                        filteredTransactions,
                        product.quantityOnHand,
                        startDate
                    )

                    allTransactions = transactionsWithBalance
                    _transactions.value = transactionsWithBalance

                    // Calculate summary
                    val summary = calculateSummary(product, transactionsWithBalance, startDate)
                    _summary.value = summary
                }
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun fetchTransactions(
        productId: String,
        startDate: Date?,
        endDate: Date?
    ): List<ProductTransaction> {
        val purchases = if (startDate != null && endDate != null) {
            purchaseItemDao.getTransactionsByProductIdAndDateRange(
                productId,
                startDate.time,
                endDate.time
            )
        } else {
            purchaseItemDao.getTransactionsByProductId(productId)
        }

        val sales = if (startDate != null && endDate != null) {
            saleItemDao.getTransactionsByProductIdAndDateRange(
                productId,
                startDate.time,
                endDate.time
            )
        } else {
            saleItemDao.getTransactionsByProductId(productId)
        }

        // Convert to ProductTransaction and combine
        val purchaseTransactions = purchases.map {
            ProductTransaction(
                id = it.id,
                date = Date(it.invoiceDate),
                documentNumber = it.invoiceNo,
                documentType = ProductTransaction.TransactionType.PURCHASE,
                quantity = it.quantity,
                rate = it.rate,
                amount = it.total,
                runningBalance = 0, // Will be calculated later
                customerOrSupplier = it.vendor,
                documentId = it.purchaseId
            )
        }

        val saleTransactions = sales.map {
            ProductTransaction(
                id = it.id,
                date = Date(it.saleDate),
                documentNumber = "SALE-${it.saleId.takeLast(8)}",
                documentType = ProductTransaction.TransactionType.SALE,
                quantity = it.quantity,
                rate = it.salePrice,  // Use salePrice from SaleItem
                amount = it.total,
                runningBalance = 0, // Will be calculated later
                customerOrSupplier = it.customerName,
                documentId = it.saleId
            )
        }

        // Combine and sort by date (oldest first for balance calculation)
        return (purchaseTransactions + saleTransactions).sortedBy { it.date }
    }

    private fun calculateRunningBalance(
        transactions: List<ProductTransaction>,
        currentStock: Int,
        startDate: Date?
    ): List<ProductTransaction> {
        if (transactions.isEmpty()) return emptyList()

        // Calculate opening balance
        // If no date filter, opening is 0
        // If date filter, opening = current - (purchases after start) + (sales after start)
        val openingBalance = if (startDate == null) {
            0
        } else {
            val purchasesAfterStart = transactions
                .filter { it.documentType == ProductTransaction.TransactionType.PURCHASE }
                .sumOf { it.quantity }
            val salesAfterStart = transactions
                .filter { it.documentType == ProductTransaction.TransactionType.SALE }
                .sumOf { it.quantity }
            currentStock - purchasesAfterStart + salesAfterStart
        }

        var runningBalance = openingBalance
        return transactions.map { transaction ->
            runningBalance += when (transaction.documentType) {
                ProductTransaction.TransactionType.PURCHASE -> transaction.quantity
                ProductTransaction.TransactionType.SALE -> -transaction.quantity
            }
            transaction.copy(runningBalance = runningBalance)
        }.sortedByDescending { it.date } // Show newest first in UI
    }

    private fun calculateSummary(
        product: Product,
        transactions: List<ProductTransaction>,
        startDate: Date?
    ): ProductHistorySummary {
        val totalPurchases = transactions
            .filter { it.documentType == ProductTransaction.TransactionType.PURCHASE }
            .sumOf { it.quantity }

        val totalSales = transactions
            .filter { it.documentType == ProductTransaction.TransactionType.SALE }
            .sumOf { it.quantity }

        val openingBalance = if (startDate == null || transactions.isEmpty()) {
            0
        } else {
            product.quantityOnHand - totalPurchases + totalSales
        }

        val closingBalance = if (transactions.isEmpty()) {
            product.quantityOnHand
        } else {
            transactions.last().runningBalance
        }

        return ProductHistorySummary(
            productId = product.id,
            productName = product.name,
            productBarcode = product.barCode,
            currentStock = product.quantityOnHand,
            openingBalance = openingBalance,
            totalPurchases = totalPurchases,
            totalSales = totalSales,
            closingBalance = closingBalance
        )
    }

    fun getFilteredTransactions(filterType: ProductTransaction.TransactionType?): List<ProductTransaction> {
        return if (filterType != null) {
            allTransactions.filter { it.documentType == filterType }
        } else {
            allTransactions
        }
    }
}
