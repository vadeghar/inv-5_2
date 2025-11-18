package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inv_5.data.entities.Sale

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Query("SELECT * FROM sales ORDER BY addedDate DESC LIMIT :limit OFFSET :offset")
    suspend fun listSales(limit: Int, offset: Int): List<Sale>

    @Query("SELECT * FROM sales ORDER BY addedDate DESC")
    suspend fun listAll(): List<Sale>

    @Query("SELECT * FROM sales WHERE customerName LIKE '%' || :searchQuery || '%' OR customerPhone LIKE '%' || :searchQuery || '%' ORDER BY addedDate DESC LIMIT :limit OFFSET :offset")
    suspend fun searchSales(searchQuery: String, limit: Int, offset: Int): List<Sale>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Sale?

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getCount(): Int

    // Dashboard queries
    @Query("SELECT * FROM sales ORDER BY addedDate DESC LIMIT :limit")
    suspend fun getRecentSales(limit: Int = 5): List<Sale>

    @Query("SELECT COUNT(*) FROM sales WHERE DATE(addedDate/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodaySaleCount(): Int

    @Query("SELECT COUNT(*) FROM sales WHERE DATE(addedDate/1000, 'unixepoch') >= DATE('now', '-7 days')")
    suspend fun getWeekSaleCount(): Int

    @Query("SELECT COUNT(*) FROM sales WHERE strftime('%Y-%m', addedDate/1000, 'unixepoch') = strftime('%Y-%m', 'now')")
    suspend fun getMonthSaleCount(): Int

    @Query("SELECT SUM(totalAmount) FROM sales WHERE DATE(addedDate/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodaySaleValue(): Double?

    @Query("SELECT * FROM sales WHERE addedDate >= :startDate AND addedDate <= :endDate ORDER BY addedDate ASC")
    suspend fun getSalesByDateRange(startDate: Long, endDate: Long): List<Sale>

    @Query("SELECT DATE(addedDate/1000, 'unixepoch') as date, COUNT(*) as count, SUM(totalAmount) as total FROM sales WHERE addedDate >= :startDate GROUP BY date ORDER BY date ASC")
    suspend fun getDailySaleTotals(startDate: Long): List<DailySaleTotal>
}

data class DailySaleTotal(
    val date: String,
    val count: Int,
    val total: Double
)
