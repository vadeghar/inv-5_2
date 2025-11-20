package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "activity_log",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["entityType"])
    ]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Type of activity: ADD, EDIT, DELETE
    val activityType: String,
    
    // Entity type: PURCHASE, SALE, EXPENSE, PRODUCT, CUSTOMER, SUPPLIER, etc.
    val entityType: String,
    
    // ID of the entity that was affected
    val entityId: String,
    
    // Human-readable description
    val description: String,
    
    // Document number or reference (invoice no, expense id, etc.)
    val documentNumber: String? = null,
    
    // Associated amount (if applicable)
    val amount: Double? = null,
    
    // Additional info (customer/supplier/category name, etc.)
    val additionalInfo: String? = null,
    
    // Timestamp when activity occurred
    val timestamp: Date = Date(),
    
    // Optional metadata in JSON format for future extensibility
    val metadata: String? = null
)
