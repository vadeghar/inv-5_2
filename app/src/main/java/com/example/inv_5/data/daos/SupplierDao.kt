package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.inv_5.data.entities.Supplier

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplier(id: String)

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Supplier?

    @Query("""
        SELECT * FROM suppliers 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR contactPerson LIKE '%' || :searchQuery || '%'
        OR phone LIKE '%' || :searchQuery || '%'
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchSuppliers(searchQuery: String, limit: Int, offset: Int): List<Supplier>

    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveSuppliers(): List<Supplier>

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getTotalCount(): Int

    @Query("""
        SELECT COUNT(*) FROM suppliers 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR contactPerson LIKE '%' || :searchQuery || '%'
        OR phone LIKE '%' || :searchQuery || '%'
    """)
    suspend fun getSearchCount(searchQuery: String): Int
}
