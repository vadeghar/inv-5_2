package com.example.inv_5.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "stock_adjustments")
data class StockAdjustment(
    @PrimaryKey
    val id: String,
    
    // Product reference
    val productId: String,
    val productName: String,
    val barcode: String,
    
    // Adjustment details
    val previousQuantity: Int,
    val newQuantity: Int,
    val difference: Int, // Can be positive or negative
    
    // Reason and notes
    val reason: AdjustmentReason,
    val notes: String?,
    
    // Metadata
    val adjustedBy: String = "Admin",
    val adjustmentDate: Date = Date(),
    val timestamp: Date = Date()
) {
    enum class AdjustmentReason(val displayName: String) {
        PHYSICAL_COUNT("Physical Count"),
        DAMAGE("Damage/Loss"),
        THEFT("Theft"),
        CORRECTION("System Correction"),
        EXPIRED("Expired"),
        RETURN_TO_SUPPLIER("Return to Supplier"),
        OTHER("Other")
    }
}
