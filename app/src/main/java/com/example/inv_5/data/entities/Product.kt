package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String,
    val name: String,
    val mrp: Double,
    val salePrice: Double,
    val barCode: String,
    val category: String
    // new fields will be added via migration; keep data class fields in sync with DB version
    , val quantityOnHand: Int = 0
    , val reorderPoint: Int = 1
    , val maximumStockLevel: Int = 5
    , val isActive: Boolean = true
    , val addedDt: Date? = null
    , val updatedDt: Date? = null
)