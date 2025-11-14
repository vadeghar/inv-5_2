package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migration from version 5 -> 6 to add suppliers table
 * Room migration from version 6 -> 7 to add supplierId to purchases table
 */
object SupplierMigrations {
    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create suppliers table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS suppliers (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    contactPerson TEXT,
                    phone TEXT,
                    email TEXT,
                    address TEXT,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    addedDt INTEGER NOT NULL,
                    updatedDt INTEGER NOT NULL
                )
            """.trimIndent())
        }
    }
    
    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add supplierId column to purchases table
            database.execSQL("ALTER TABLE purchases ADD COLUMN supplierId TEXT")
            // Create index for the foreign key
            database.execSQL("CREATE INDEX IF NOT EXISTS index_purchases_supplierId ON purchases(supplierId)")
        }
    }
}
