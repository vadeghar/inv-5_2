package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inv_5.data.entities.PurchaseItem

@Dao
interface PurchaseItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItem(purchaseItem: PurchaseItem)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun listByPurchaseId(purchaseId: String): List<PurchaseItem>

    @Query("SELECT * FROM purchase_items")
    suspend fun listAll(): List<PurchaseItem>

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteByPurchaseId(purchaseId: String)

    @Query("""
        SELECT pi.*, p.invoiceNo, p.invoiceDate, p.vendor, p.supplierId
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE pi.productId = :productId
        ORDER BY p.invoiceDate DESC, p.addedDate DESC
    """)
    suspend fun getTransactionsByProductId(productId: String): List<PurchaseItemWithDocument>

    @Query("""
        SELECT pi.*, p.invoiceNo, p.invoiceDate, p.vendor, p.supplierId
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE pi.productId = :productId
        AND p.invoiceDate >= :startDate
        AND p.invoiceDate <= :endDate
        ORDER BY p.invoiceDate DESC, p.addedDate DESC
    """)
    suspend fun getTransactionsByProductIdAndDateRange(
        productId: String,
        startDate: Long,
        endDate: Long
    ): List<PurchaseItemWithDocument>

    // Dashboard queries
    @Query("""
        SELECT SUM(pi.total) 
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE DATE(p.addedDate/1000, 'unixepoch') = DATE('now')
    """)
    suspend fun getTodayPurchaseValue(): Double?

    @Query("""
        SELECT SUM(pi.quantity) 
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE pi.productId = :productId
    """)
    suspend fun getTotalPurchasedQuantity(productId: String): Int?

    @Query("""
        SELECT DATE(p.addedDate/1000, 'unixepoch') as date, SUM(pi.quantity) as totalQuantity
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE p.addedDate >= :startDate
        GROUP BY date
        ORDER BY date ASC
    """)
    suspend fun getDailyPurchaseQuantities(startDate: Long): List<DailyStockMovement>
}

data class DailyStockMovement(
    val date: String,
    val totalQuantity: Int
)

/**
 * Data class to hold purchase item with document details
 */
data class PurchaseItemWithDocument(
    val id: String,
    val purchaseId: String,
    val productId: String,
    val productBarcode: String,
    val productName: String,
    val productSalePrice: Double,
    val hsn: String,
    val mrp: Double,
    val discountAmount: Double,
    val discountPercentage: Double,
    val rate: Double,
    val quantity: Int,
    val taxable: Double,
    val tax: Double,
    val total: Double,
    val invoiceNo: String,
    val invoiceDate: Long,
    val vendor: String,
    val supplierId: String?
)