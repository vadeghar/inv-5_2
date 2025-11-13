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

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteBySaleId(saleId: String)
}
