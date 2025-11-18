package com.example.inv_5.data.models

data class DashboardStats(
    val totalProducts: Int = 0,
    val activeProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val outOfStockProducts: Int = 0,
    val totalInventoryValue: Double = 0.0,
    val todayPurchases: Int = 0,
    val todayPurchaseValue: Double = 0.0,
    val todaySales: Int = 0,
    val todaySaleValue: Double = 0.0,
    val thisWeekPurchases: Int = 0,
    val thisWeekSales: Int = 0,
    val thisMonthPurchases: Int = 0,
    val thisMonthSales: Int = 0
)

data class RecentActivity(
    val id: String,
    val type: ActivityType,
    val documentNumber: String,
    val date: Long,
    val itemCount: Int,
    val totalAmount: Double = 0.0,
    val customerOrSupplier: String? = null
)

enum class ActivityType {
    PURCHASE,
    SALE
}

data class TopProduct(
    val productId: String,
    val productName: String,
    val barcode: String,
    val currentStock: Int,
    val totalSold: Int,
    val totalPurchased: Int,
    val value: Double
)

data class StockMovement(
    val date: Long,
    val purchases: Int,
    val sales: Int
)
