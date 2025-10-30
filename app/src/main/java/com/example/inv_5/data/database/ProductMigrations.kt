package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migration from version 1 -> 2 to add product inventory/metadata columns
 */
object ProductMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add columns with sensible defaults
            database.execSQL("ALTER TABLE products ADD COLUMN quantityOnHand INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE products ADD COLUMN reorderPoint INTEGER NOT NULL DEFAULT 1")
            database.execSQL("ALTER TABLE products ADD COLUMN maximumStockLevel INTEGER NOT NULL DEFAULT 5")
            database.execSQL("ALTER TABLE products ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            // Dates stored as integer timestamps (nullable)
            database.execSQL("ALTER TABLE products ADD COLUMN addedDt INTEGER")
            database.execSQL("ALTER TABLE products ADD COLUMN updatedDt INTEGER")
        }
    }
}
