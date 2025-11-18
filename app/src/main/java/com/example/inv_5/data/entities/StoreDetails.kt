package com.example.inv_5.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_details")
data class StoreDetails(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "store_name")
    val storeName: String,
    val caption: String?,
    val address: String,
    val phone: String,
    val owner: String?
)
