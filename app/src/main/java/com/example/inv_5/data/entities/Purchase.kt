package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey
    val id: String,
    val vendor: String,
    val totalAmount: Double,
    val invoiceNo: String,
    val invoiceDate: Date,
    val addedDate: Date,
    val updatedDate: Date? = null,
    val totalQty: Int,
    val totalTaxable: Double,
    val status: String
)