package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inv_5.data.entities.SaleItem

@Dao
interface SaleItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(saleItem: SaleItem)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun listBySaleId(saleId: String): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    suspend fun listAll(): List<SaleItem>

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteBySaleId(saleId: String)

    @Query("""
        SELECT si.*, s.customerName, s.customerPhone, s.saleDate, s.customerId
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE si.productId = :productId
        ORDER BY s.saleDate DESC, s.addedDate DESC
    """)
    suspend fun getTransactionsByProductId(productId: String): List<SaleItemWithDocument>

    @Query("""
        SELECT si.*, s.customerName, s.customerPhone, s.saleDate, s.customerId
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE si.productId = :productId
        AND s.saleDate >= :startDate
        AND s.saleDate <= :endDate
        ORDER BY s.saleDate DESC, s.addedDate DESC
    """)
    suspend fun getTransactionsByProductIdAndDateRange(
        productId: String,
        startDate: Long,
        endDate: Long
    ): List<SaleItemWithDocument>

    // Dashboard queries
    @Query("""
        SELECT SUM(si.quantity) 
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE si.productId = :productId
    """)
    suspend fun getTotalSoldQuantity(productId: String): Int?

    @Query("""
        SELECT DATE(s.addedDate/1000, 'unixepoch') as date, SUM(si.quantity) as totalQuantity
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE s.addedDate >= :startDate
        GROUP BY date
        ORDER BY date ASC
    """)
    suspend fun getDailySaleQuantities(startDate: Long): List<DailySaleMovement>
}

data class DailySaleMovement(
    val date: String,
    val totalQuantity: Int
)

/**
 * Data class to hold sale item with document details
 */
data class SaleItemWithDocument(
    val id: String,
    val saleId: String,
    val productId: String,
    val productBarcode: String,
    val productName: String,
    val hsn: String,
    val mrp: Double,
    val salePrice: Double,
    val discountPercentage: Double,
    val quantity: Int,
    val taxPercentage: Double,
    val taxable: Double,
    val tax: Double,
    val total: Double,
    val customerName: String,
    val customerPhone: String,
    val saleDate: Long,
    val customerId: String?
)