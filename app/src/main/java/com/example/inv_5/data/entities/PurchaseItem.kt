package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PurchaseItem(
    @PrimaryKey
    val id: String,
    var purchaseId: String,
    var productId: String,
    // snapshot fields added to capture product info at time of purchase
    val productBarcode: String = "",
    val productName: String = "",
    val productSalePrice: Double = 0.0,
    val hsn: String,
    val mrp: Double,
    val discountAmount: Double,
    val discountPercentage: Double,
    val rate: Double,
    val quantity: Int,
    val taxable: Double,
    val tax: Double,
    val total: Double
)