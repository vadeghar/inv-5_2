package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["supplierId"])]
)
data class Purchase(
    @PrimaryKey
    val id: String,
    val vendor: String,
    val supplierId: String? = null,  // Foreign key to suppliers table
    val totalAmount: Double,
    val invoiceNo: String,
    val invoiceDate: Date,
    val addedDate: Date,
    val updatedDate: Date? = null,
    val totalQty: Int,
    val totalTaxable: Double,
    val status: String
)