package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object PurchaseMigrations {
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add updatedDate column to purchases table
            database.execSQL("ALTER TABLE purchases ADD COLUMN updatedDate INTEGER")
        }
    }
}