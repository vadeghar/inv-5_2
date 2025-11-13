package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey
    val id: String,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String,
    val saleDate: Date,
    val addedDate: Date,
    val updatedDate: Date? = null,
    val totalQty: Int,
    val totalTaxable: Double,
    val totalTax: Double,
    val totalAmount: Double,
    val status: String
)
