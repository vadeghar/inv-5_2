package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create stock_adjustments table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `stock_adjustments` (
                `id` TEXT PRIMARY KEY NOT NULL,
                `productId` TEXT NOT NULL,
                `productName` TEXT NOT NULL,
                `barcode` TEXT NOT NULL,
                `previousQuantity` INTEGER NOT NULL,
                `newQuantity` INTEGER NOT NULL,
                `difference` INTEGER NOT NULL,
                `reason` TEXT NOT NULL,
                `notes` TEXT,
                `adjustedBy` TEXT NOT NULL,
                `adjustmentDate` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
        """)
    }
}
