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

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteByPurchaseId(purchaseId: String)
}