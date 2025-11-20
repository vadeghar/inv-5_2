package com.example.inv_5.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.inv_5.data.model.Expense
import java.util.Date

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC, expenseId DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC, expenseId DESC")
    suspend fun getAllExpensesList(): List<Expense>

    @Query("SELECT * FROM expenses WHERE expenseId = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE expenseCategory LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR expenseType LIKE '%' || :query || '%' ORDER BY expenseDate DESC")
    fun searchExpenses(query: String): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate ORDER BY expenseDate DESC")
    suspend fun getExpensesByDateRange(startDate: Date, endDate: Date): List<Expense>

    @Query("SELECT * FROM expenses WHERE expenseType = :type ORDER BY expenseDate DESC")
    suspend fun getExpensesByType(type: String): List<Expense>

    @Query("SELECT SUM(1) FROM expenses WHERE expenseCategory = :category")
    suspend fun getExpenseCountByCategory(category: String): Int
}
