package com.example.inv_5.ui.stockmanagement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inv_5.data.dao.StockAdjustmentDao
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.daos.ProductDao
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.StockAdjustment
import com.example.inv_5.data.repository.ActivityLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class StockManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getInstance(application)
    private val productDao: ProductDao = database.productDao()
    private val stockAdjustmentDao: StockAdjustmentDao = database.stockAdjustmentDao()
    private val activityLogRepository = ActivityLogRepository(application)

    // All products
    private val _allProducts = MutableLiveData<List<Product>>()
    val allProducts: LiveData<List<Product>> = _allProducts

    // Filtered products
    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    // Stock statistics
    private val _totalProducts = MutableLiveData<Int>()
    val totalProducts: LiveData<Int> = _totalProducts

    private val _outOfStockCount = MutableLiveData<Int>()
    val outOfStockCount: LiveData<Int> = _outOfStockCount

    private val _lowStockCount = MutableLiveData<Int>()
    val lowStockCount: LiveData<Int> = _lowStockCount

    private val _inventoryValue = MutableLiveData<Double>()
    val inventoryValue: LiveData<Double> = _inventoryValue

    // Search query
    private var currentSearchQuery: String = ""
    private var currentFilter: StockFilter = StockFilter.ALL

    enum class StockFilter {
        ALL,
        IN_STOCK,
        OUT_OF_STOCK,
        LOW_STOCK
    }

    init {
        loadStockData()
    }

    /**
     * Load all stock data and statistics
     */
    fun loadStockData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val products = productDao.listAll()
                _allProducts.postValue(products)
                
                // Apply current filter and search
                applyFilterAndSearch(products)

                // Calculate statistics
                _totalProducts.postValue(productDao.getTotalProducts())
                _outOfStockCount.postValue(productDao.getOutOfStockCount())
                _lowStockCount.postValue(productDao.getLowStockCount())
                _inventoryValue.postValue(productDao.getTotalInventoryValue())
            }
        }
    }

    /**
     * Search products by name or barcode
     */
    fun searchProducts(query: String) {
        currentSearchQuery = query
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val products = if (query.isEmpty()) {
                    productDao.listAll()
                } else {
                    productDao.searchProducts(query, 1000, 0)
                }
                applyFilterAndSearch(products)
            }
        }
    }

    /**
     * Apply stock filter
     */
    fun applyFilter(filter: StockFilter) {
        currentFilter = filter
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val products = if (currentSearchQuery.isEmpty()) {
                    productDao.listAll()
                } else {
                    productDao.searchProducts(currentSearchQuery, 1000, 0)
                }
                applyFilterAndSearch(products)
            }
        }
    }

    /**
     * Apply both filter and search to products list
     */
    private fun applyFilterAndSearch(products: List<Product>) {
        val filtered = when (currentFilter) {
            StockFilter.ALL -> products
            StockFilter.IN_STOCK -> products.filter { it.quantityOnHand > 0 }
            StockFilter.OUT_OF_STOCK -> products.filter { it.quantityOnHand == 0 }
            StockFilter.LOW_STOCK -> products.filter { it.quantityOnHand > 0 && it.quantityOnHand <= it.reorderPoint }
        }
        _filteredProducts.postValue(filtered)
    }

    /**
     * Adjust stock for a product
     */
    fun adjustStock(
        product: Product,
        newQuantity: Int,
        reason: StockAdjustment.AdjustmentReason,
        notes: String?,
        adjustedBy: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val difference = newQuantity - product.quantityOnHand

                // Create stock adjustment record
                val adjustment = StockAdjustment(
                    id = UUID.randomUUID().toString(),
                    productId = product.id,
                    productName = product.name,
                    barcode = product.barCode,
                    previousQuantity = product.quantityOnHand,
                    newQuantity = newQuantity,
                    difference = difference,
                    reason = reason,
                    notes = notes,
                    adjustedBy = adjustedBy,
                    adjustmentDate = Date(),
                    timestamp = Date()
                )

                // Insert adjustment record
                stockAdjustmentDao.insert(adjustment)

                // Update product quantity
                productDao.updateQuantity(product.id, newQuantity)

                // Log to activity log
                activityLogRepository.logStockAdjustment(
                    productName = product.name,
                    previousQty = product.quantityOnHand,
                    newQty = newQuantity,
                    reason = reason.name,
                    adjustmentId = adjustment.id
                )

                // Reload data
                loadStockData()
            }
        }
    }

    /**
     * Calculate stock valuation using different methods
     */
    suspend fun calculateStockValuation(method: ValuationMethod): Map<String, Double> {
        return withContext(Dispatchers.IO) {
            when (method) {
                ValuationMethod.FIFO -> calculateFIFO()
                ValuationMethod.LIFO -> calculateLIFO()
                ValuationMethod.WEIGHTED_AVERAGE -> calculateWeightedAverage()
            }
        }
    }

    enum class ValuationMethod {
        FIFO,  // First In, First Out
        LIFO,  // Last In, First Out
        WEIGHTED_AVERAGE
    }

    private fun calculateFIFO(): Map<String, Double> {
        // TODO: Implement FIFO calculation using purchase history
        // Query purchase items ordered by date (oldest first)
        // Calculate cost based on oldest purchases first
        return mapOf("totalValue" to 0.0)
    }

    private fun calculateLIFO(): Map<String, Double> {
        // TODO: Implement LIFO calculation using purchase history
        // Query purchase items ordered by date (newest first)
        // Calculate cost based on newest purchases first
        return mapOf("totalValue" to 0.0)
    }

    private fun calculateWeightedAverage(): Map<String, Double> {
        // TODO: Implement weighted average calculation
        // Calculate average cost across all purchases
        // Multiply by current quantity
        return mapOf("totalValue" to 0.0)
    }
}
