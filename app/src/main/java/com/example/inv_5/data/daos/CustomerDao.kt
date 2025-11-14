package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.inv_5.data.entities.Customer

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: String)

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Customer?

    @Query("""
        SELECT * FROM customers 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR contactPerson LIKE '%' || :searchQuery || '%'
        OR phone LIKE '%' || :searchQuery || '%'
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchCustomers(searchQuery: String, limit: Int, offset: Int): List<Customer>

    @Query("SELECT * FROM customers WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActiveCustomers(): List<Customer>

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getTotalCount(): Int

    @Query("""
        SELECT COUNT(*) FROM customers 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR contactPerson LIKE '%' || :searchQuery || '%'
        OR phone LIKE '%' || :searchQuery || '%'
    """)
    suspend fun getSearchCount(searchQuery: String): Int
}
