package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    val id: String,
    val name: String,
    val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val isActive: Boolean = true,
    val addedDt: Date,
    val updatedDt: Date
)
