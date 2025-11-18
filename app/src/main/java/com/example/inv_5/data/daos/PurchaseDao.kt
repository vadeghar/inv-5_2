package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inv_5.data.entities.Purchase

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)

    @Query("SELECT * FROM purchases ORDER BY addedDate DESC LIMIT :limit OFFSET :offset")
    suspend fun listPurchases(limit: Int, offset: Int): List<Purchase>

    @Query("SELECT * FROM purchases WHERE vendor LIKE '%' || :searchQuery || '%' OR invoiceNo LIKE '%' || :searchQuery || '%' ORDER BY addedDate DESC LIMIT :limit OFFSET :offset")
    suspend fun searchPurchases(searchQuery: String, limit: Int, offset: Int): List<Purchase>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Purchase?

    @Query("SELECT * FROM purchases ORDER BY addedDate DESC")
    suspend fun listAll(): List<Purchase>

    @Query("SELECT COUNT(*) FROM purchases")
    suspend fun getCount(): Int

    // Dashboard queries
    @Query("SELECT * FROM purchases ORDER BY addedDate DESC LIMIT :limit")
    suspend fun getRecentPurchases(limit: Int = 5): List<Purchase>

    @Query("SELECT COUNT(*) FROM purchases WHERE DATE(addedDate/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayPurchaseCount(): Int

    @Query("SELECT COUNT(*) FROM purchases WHERE DATE(addedDate/1000, 'unixepoch') >= DATE('now', '-7 days')")
    suspend fun getWeekPurchaseCount(): Int

    @Query("SELECT COUNT(*) FROM purchases WHERE strftime('%Y-%m', addedDate/1000, 'unixepoch') = strftime('%Y-%m', 'now')")
    suspend fun getMonthPurchaseCount(): Int

    @Query("SELECT * FROM purchases WHERE addedDate >= :startDate AND addedDate <= :endDate ORDER BY addedDate ASC")
    suspend fun getPurchasesByDateRange(startDate: Long, endDate: Long): List<Purchase>

    @Query("SELECT DATE(addedDate/1000, 'unixepoch') as date, COUNT(*) as count, SUM(totalAmount) as total FROM purchases WHERE addedDate >= :startDate GROUP BY date ORDER BY date ASC")
    suspend fun getDailyPurchaseTotals(startDate: Long): List<DailyTotal>
}

data class DailyTotal(
    val date: String,
    val count: Int,
    val total: Double
)