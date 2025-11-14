package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object IndexMigrations {
    
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add indices for foreign keys to improve query performance
            database.execSQL("CREATE INDEX IF NOT EXISTS index_purchases_supplierId ON purchases(supplierId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sales_customerId ON sales(customerId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_purchase_items_purchaseId ON purchase_items(purchaseId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_purchase_items_productId ON purchase_items(productId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sale_items_saleId ON sale_items(saleId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sale_items_productId ON sale_items(productId)")
        }
    }
}
