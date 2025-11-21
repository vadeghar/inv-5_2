package com.example.inv_5.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inv_5.data.entities.StockAdjustment
import java.util.Date

@Dao
interface StockAdjustmentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adjustment: StockAdjustment)
    
    @Query("SELECT * FROM stock_adjustments ORDER BY adjustmentDate DESC")
    fun getAllAdjustmentsLive(): LiveData<List<StockAdjustment>>
    
    @Query("SELECT * FROM stock_adjustments ORDER BY adjustmentDate DESC")
    suspend fun getAllAdjustments(): List<StockAdjustment>
    
    @Query("SELECT * FROM stock_adjustments WHERE productId = :productId ORDER BY adjustmentDate DESC")
    suspend fun getAdjustmentsByProduct(productId: String): List<StockAdjustment>
    
    @Query("""
        SELECT * FROM stock_adjustments 
        WHERE adjustmentDate BETWEEN :startDate AND :endDate 
        ORDER BY adjustmentDate DESC
    """)
    suspend fun getAdjustmentsByDateRange(startDate: Date, endDate: Date): List<StockAdjustment>
    
    @Query("SELECT * FROM stock_adjustments WHERE id = :id")
    suspend fun getAdjustmentById(id: String): StockAdjustment?
    
    @Query("SELECT COUNT(*) FROM stock_adjustments")
    suspend fun getAdjustmentCount(): Int
    
    @Query("SELECT SUM(ABS(difference)) FROM stock_adjustments WHERE difference < 0")
    suspend fun getTotalNegativeAdjustments(): Int?
    
    @Query("SELECT SUM(difference) FROM stock_adjustments WHERE difference > 0")
    suspend fun getTotalPositiveAdjustments(): Int?
    
    @Query("DELETE FROM stock_adjustments WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM stock_adjustments")
    suspend fun deleteAll()
}
