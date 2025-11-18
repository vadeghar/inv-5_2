package com.example.inv_5.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Purchase
import com.example.inv_5.data.entities.Sale
import com.example.inv_5.data.models.ActivityType
import com.example.inv_5.data.models.ChartEntry
import com.example.inv_5.data.models.DashboardStats
import com.example.inv_5.data.models.RecentActivity
import com.example.inv_5.data.models.StockMovementData
import com.example.inv_5.data.models.TrendChartData
import com.example.inv_5.data.models.StockMovement
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getInstance(application)
    private val productDao = database.productDao()
    private val purchaseDao = database.purchaseDao()
    private val saleDao = database.saleDao()
    private val purchaseItemDao = database.purchaseItemDao()
    private val saleItemDao = database.saleItemDao()

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> = _dashboardStats

    private val _recentActivities = MutableLiveData<List<RecentActivity>>()
    val recentActivities: LiveData<List<RecentActivity>> = _recentActivities

    private val _stockMovements = MutableLiveData<List<StockMovement>>()
    val stockMovements: LiveData<List<StockMovement>> = _stockMovements

    private val _trendChartData = MutableLiveData<TrendChartData>()
    val trendChartData: LiveData<TrendChartData> = _trendChartData

    private val _stockMovementChartData = MutableLiveData<StockMovementData>()
    val stockMovementChartData: LiveData<StockMovementData> = _stockMovementChartData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Fetch all dashboard data in parallel
                val statsDeferred = async { fetchDashboardStats() }
                val recentActivitiesDeferred = async { fetchRecentActivities() }
                val stockMovementsDeferred = async { fetchStockMovements() }
                val trendChartDeferred = async { fetchTrendChartData() }
                val stockMovementChartDeferred = async { fetchStockMovementChartData() }

                _dashboardStats.value = statsDeferred.await()
                _recentActivities.value = recentActivitiesDeferred.await()
                _stockMovements.value = stockMovementsDeferred.await()
                _trendChartData.value = trendChartDeferred.await()
                _stockMovementChartData.value = stockMovementChartDeferred.await()

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error loading dashboard: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchDashboardStats(): DashboardStats {
        val totalProducts = productDao.getTotalProducts()
        val activeProducts = productDao.getActiveProducts()
        val lowStockProducts = productDao.getLowStockCount()
        val outOfStockProducts = productDao.getOutOfStockCount()
        val totalInventoryValue = productDao.getTotalInventoryValue() ?: 0.0

        val todayPurchases = purchaseDao.getTodayPurchaseCount()
        val todayPurchaseValue = purchaseItemDao.getTodayPurchaseValue() ?: 0.0
        val todaySales = saleDao.getTodaySaleCount()
        val todaySaleValue = saleDao.getTodaySaleValue() ?: 0.0

        val thisWeekPurchases = purchaseDao.getWeekPurchaseCount()
        val thisWeekSales = saleDao.getWeekSaleCount()
        val thisMonthPurchases = purchaseDao.getMonthPurchaseCount()
        val thisMonthSales = saleDao.getMonthSaleCount()

        return DashboardStats(
            totalProducts = totalProducts,
            activeProducts = activeProducts,
            lowStockProducts = lowStockProducts,
            outOfStockProducts = outOfStockProducts,
            totalInventoryValue = totalInventoryValue,
            todayPurchases = todayPurchases,
            todayPurchaseValue = todayPurchaseValue,
            todaySales = todaySales,
            todaySaleValue = todaySaleValue,
            thisWeekPurchases = thisWeekPurchases,
            thisWeekSales = thisWeekSales,
            thisMonthPurchases = thisMonthPurchases,
            thisMonthSales = thisMonthSales
        )
    }

    private suspend fun fetchRecentActivities(): List<RecentActivity> {
        val recentPurchases = purchaseDao.getRecentPurchases(5)
        val recentSales = saleDao.getRecentSales(5)

        val activities = mutableListOf<RecentActivity>()

        // Convert purchases to activities
        recentPurchases.forEach { purchase: Purchase ->
            val items = purchaseItemDao.listByPurchaseId(purchase.id)
            activities.add(
                RecentActivity(
                    id = purchase.id,
                    type = ActivityType.PURCHASE,
                    documentNumber = purchase.invoiceNo,
                    date = purchase.addedDate.time,
                    itemCount = items.size,
                    totalAmount = items.sumOf { it.total },
                    customerOrSupplier = purchase.vendor
                )
            )
        }

        // Convert sales to activities
        recentSales.forEach { sale: Sale ->
            val items = saleItemDao.listBySaleId(sale.id)
            activities.add(
                RecentActivity(
                    id = sale.id,
                    type = ActivityType.SALE,
                    documentNumber = "SALE-${sale.id.take(8)}",
                    date = sale.addedDate.time,
                    itemCount = items.size,
                    totalAmount = sale.totalAmount,
                    customerOrSupplier = sale.customerName.ifEmpty { "Walk-in Customer" }
                )
            )
        }

        // Sort by date descending and take top 10
        return activities.sortedByDescending { it.date }.take(10)
    }

    private suspend fun fetchStockMovements(): List<StockMovement> {
        val calendar = Calendar.getInstance()
        val movements = mutableListOf<StockMovement>()

        // Get data for last 7 days
        for (i in 6 downTo 0) {
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_YEAR, -i)
            dayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            dayCalendar.set(Calendar.MINUTE, 0)
            dayCalendar.set(Calendar.SECOND, 0)
            dayCalendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = dayCalendar.timeInMillis

            dayCalendar.set(Calendar.HOUR_OF_DAY, 23)
            dayCalendar.set(Calendar.MINUTE, 59)
            dayCalendar.set(Calendar.SECOND, 59)
            dayCalendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = dayCalendar.timeInMillis

            val purchases = purchaseDao.getPurchasesByDateRange(startOfDay, endOfDay)
            val sales = saleDao.getSalesByDateRange(startOfDay, endOfDay)

            val purchaseCount = purchases.sumOf { purchase: Purchase ->
                purchaseItemDao.listByPurchaseId(purchase.id).sumOf { it.quantity }
            }

            val saleCount = sales.sumOf { sale: Sale ->
                saleItemDao.listBySaleId(sale.id).sumOf { it.quantity }
            }

            movements.add(
                StockMovement(
                    date = startOfDay,
                    purchases = purchaseCount,
                    sales = saleCount
                )
            )
        }

        return movements
    }

    private suspend fun fetchTrendChartData(): TrendChartData {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -29) // Last 30 days
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dailyPurchases = purchaseDao.getDailyPurchaseTotals(startDate)
        val dailySales = saleDao.getDailySaleTotals(startDate)

        // Create a map of all 30 days with 0 values
        val dateMap = mutableMapOf<String, Pair<Float, Float>>()
        for (i in 0 until 30) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.add(Calendar.DAY_OF_YEAR, -(29 - i))
            val dateKey = String.format(
                "%04d-%02d-%02d",
                dayCalendar.get(Calendar.YEAR),
                dayCalendar.get(Calendar.MONTH) + 1,
                dayCalendar.get(Calendar.DAY_OF_MONTH)
            )
            dateMap[dateKey] = Pair(0f, 0f)
        }

        // Fill in actual purchase values
        dailyPurchases.forEach { daily ->
            val existing = dateMap[daily.date] ?: Pair(0f, 0f)
            dateMap[daily.date] = Pair(daily.total.toFloat(), existing.second)
        }

        // Fill in actual sale values
        dailySales.forEach { daily ->
            val existing = dateMap[daily.date] ?: Pair(0f, 0f)
            dateMap[daily.date] = Pair(existing.first, daily.total.toFloat())
        }

        val sortedDates = dateMap.keys.sorted()
        val dates = sortedDates.map { 
            val parts = it.split("-")
            "${parts[2]}/${parts[1]}" // Format as DD/MM
        }
        val purchaseValues = sortedDates.map { dateMap[it]?.first ?: 0f }
        val saleValues = sortedDates.map { dateMap[it]?.second ?: 0f }

        return TrendChartData(dates, purchaseValues, saleValues)
    }

    private suspend fun fetchStockMovementChartData(): StockMovementData {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6) // Last 7 days
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dailyPurchaseQty = purchaseItemDao.getDailyPurchaseQuantities(startDate)
        val dailySaleQty = saleItemDao.getDailySaleQuantities(startDate)

        // Create a map of all 7 days
        val dateMap = mutableMapOf<String, Pair<Float, Float>>()
        for (i in 0 until 7) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.add(Calendar.DAY_OF_YEAR, -(6 - i))
            val dateKey = String.format(
                "%04d-%02d-%02d",
                dayCalendar.get(Calendar.YEAR),
                dayCalendar.get(Calendar.MONTH) + 1,
                dayCalendar.get(Calendar.DAY_OF_MONTH)
            )
            dateMap[dateKey] = Pair(0f, 0f)
        }

        // Fill in actual purchase quantities
        dailyPurchaseQty.forEach { daily ->
            val existing = dateMap[daily.date] ?: Pair(0f, 0f)
            dateMap[daily.date] = Pair(daily.totalQuantity.toFloat(), existing.second)
        }

        // Fill in actual sale quantities
        dailySaleQty.forEach { daily ->
            val existing = dateMap[daily.date] ?: Pair(0f, 0f)
            dateMap[daily.date] = Pair(existing.first, daily.totalQuantity.toFloat())
        }

        val sortedDates = dateMap.keys.sorted()
        val dates = sortedDates.map {
            val parts = it.split("-")
            "${parts[2]}/${parts[1]}" // Format as DD/MM
        }
        val purchaseQty = sortedDates.map { dateMap[it]?.first ?: 0f }
        val saleQty = sortedDates.map { dateMap[it]?.second ?: 0f }

        return StockMovementData(dates, purchaseQty, saleQty)
    }

    fun refresh() {
        loadDashboardData()
    }
}