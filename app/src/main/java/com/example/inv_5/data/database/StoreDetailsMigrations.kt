package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object StoreDetailsMigrations {
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS store_details (
                    id INTEGER NOT NULL PRIMARY KEY,
                    store_name TEXT NOT NULL,
                    caption TEXT,
                    address TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    owner TEXT
                )
                """.trimIndent()
            )
        }
    }
}
