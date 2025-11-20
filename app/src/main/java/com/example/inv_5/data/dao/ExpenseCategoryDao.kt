package com.example.inv_5.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.inv_5.data.model.ExpenseCategory

@Dao
interface ExpenseCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ExpenseCategory): Long

    @Update
    suspend fun update(category: ExpenseCategory)

    @Delete
    suspend fun delete(category: ExpenseCategory)

    @Query("SELECT * FROM expense_categories ORDER BY categoryName ASC")
    fun getAllCategories(): LiveData<List<ExpenseCategory>>

    @Query("SELECT * FROM expense_categories ORDER BY categoryName ASC")
    suspend fun getAllCategoriesList(): List<ExpenseCategory>

    @Query("SELECT * FROM expense_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): ExpenseCategory?

    @Query("SELECT * FROM expense_categories WHERE categoryName = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): ExpenseCategory?

    @Query("SELECT categoryName FROM expense_categories ORDER BY categoryName ASC")
    suspend fun getAllCategoryNames(): List<String>
}
