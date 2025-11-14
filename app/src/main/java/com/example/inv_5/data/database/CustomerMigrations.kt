package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migration from version 7 -> 8 to add customers table
 * Room migration from version 8 -> 9 to add customerId to sales table
 */
object CustomerMigrations {
    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create customers table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS customers (
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
    
    val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add customerId column to sales table
            database.execSQL("ALTER TABLE sales ADD COLUMN customerId TEXT")
            // Create index for the foreign key
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sales_customerId ON sales(customerId)")
        }
    }
}
