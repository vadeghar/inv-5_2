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
}