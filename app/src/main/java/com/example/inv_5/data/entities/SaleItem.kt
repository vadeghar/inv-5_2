package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItem(
    @PrimaryKey
    val id: String,
    var saleId: String,
    var productId: String,
    // snapshot fields to capture product info at time of sale
    val productBarcode: String = "",
    val productName: String = "",
    val hsn: String,
    val mrp: Double,
    val salePrice: Double,
    val discountPercentage: Double,
    val quantity: Int,
    val taxPercentage: Double,
    val taxable: Double,
    val tax: Double,
    val total: Double
)
