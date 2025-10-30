package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object PurchaseItemMigrations {
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add snapshot columns to purchase_items
            database.execSQL("ALTER TABLE purchase_items ADD COLUMN productBarcode TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE purchase_items ADD COLUMN productName TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE purchase_items ADD COLUMN productSalePrice REAL NOT NULL DEFAULT 0.0")
        }
    }
}
