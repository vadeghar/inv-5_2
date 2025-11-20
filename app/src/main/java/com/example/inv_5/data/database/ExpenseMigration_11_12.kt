package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns to expenses table
        database.execSQL("ALTER TABLE expenses ADD COLUMN totalAmount REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE expenses ADD COLUMN cgstAmount REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE expenses ADD COLUMN sgstAmount REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE expenses ADD COLUMN igstAmount REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE expenses ADD COLUMN gstRate REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE expenses ADD COLUMN paymentMode TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE expenses ADD COLUMN paymentStatus TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE expenses ADD COLUMN paymentDate INTEGER")
        database.execSQL("ALTER TABLE expenses ADD COLUMN paidAmount REAL NOT NULL DEFAULT 0.0")
    }
}
