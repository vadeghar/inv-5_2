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

    @Query("SELECT * FROM sales WHERE customerName LIKE '%' || :searchQuery || '%' OR customerPhone LIKE '%' || :searchQuery || '%' ORDER BY addedDate DESC LIMIT :limit OFFSET :offset")
    suspend fun searchSales(searchQuery: String, limit: Int, offset: Int): List<Sale>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Sale?

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getCount(): Int
}
