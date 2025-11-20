package com.example.inv_5.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0,
    val expenseDate: Date,
    val expenseCategory: String,
    val expenseType: String, // CAPEX, OPEX, MIXED
    val description: String,
    val totalAmount: Double,
    
    // Taxation (if applicable)
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val gstRate: Double = 0.0,
    
    // Payment Info
    val paymentMode: String = "", // Cash, Card, UPI, Bank Transfer, etc.
    val paymentStatus: String = "", // Paid, Pending, Partial
    val paymentDate: Date? = null,
    val paidAmount: Double = 0.0
)
