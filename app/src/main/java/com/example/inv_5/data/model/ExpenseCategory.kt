package com.example.inv_5.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_categories")
data class ExpenseCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String,
    val categoryType: String, // OPEX, CAPEX, MIXED
    val notes: String = ""
)
