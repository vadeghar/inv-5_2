package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create activity_log table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `activity_log` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `activityType` TEXT NOT NULL,
                `entityType` TEXT NOT NULL,
                `entityId` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `documentNumber` TEXT,
                `amount` REAL,
                `additionalInfo` TEXT,
                `timestamp` INTEGER NOT NULL,
                `metadata` TEXT
            )
        """)
        
        // Create index on timestamp for faster queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_activity_log_timestamp` 
            ON `activity_log`(`timestamp`)
        """)
        
        // Create index on entity type for filtering
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_activity_log_entityType` 
            ON `activity_log`(`entityType`)
        """)
    }
}
