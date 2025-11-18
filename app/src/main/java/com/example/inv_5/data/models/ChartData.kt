package com.example.inv_5.data.models

/**
 * Data class for chart entries
 */
data class ChartEntry(
    val label: String,
    val value: Float
)

/**
 * Data class for dual-line chart (Purchases vs Sales)
 */
data class TrendChartData(
    val dates: List<String>,
    val purchaseValues: List<Float>,
    val saleValues: List<Float>
)

/**
 * Data class for stock movement bar chart
 */
data class StockMovementData(
    val dates: List<String>,
    val purchaseQuantities: List<Float>,
    val saleQuantities: List<Float>
)
