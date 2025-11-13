package com.example.inv_5.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SaleMigrations {
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create sales table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `sales` (
                    `id` TEXT NOT NULL,
                    `customerName` TEXT NOT NULL,
                    `customerAddress` TEXT NOT NULL,
                    `customerPhone` TEXT NOT NULL,
                    `saleDate` INTEGER NOT NULL,
                    `addedDate` INTEGER NOT NULL,
                    `updatedDate` INTEGER,
                    `totalQty` INTEGER NOT NULL,
                    `totalTaxable` REAL NOT NULL,
                    `totalTax` REAL NOT NULL,
                    `totalAmount` REAL NOT NULL,
                    `status` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())

            // Create sale_items table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `sale_items` (
                    `id` TEXT NOT NULL,
                    `saleId` TEXT NOT NULL,
                    `productId` TEXT NOT NULL,
                    `productBarcode` TEXT NOT NULL,
                    `productName` TEXT NOT NULL,
                    `hsn` TEXT NOT NULL,
                    `mrp` REAL NOT NULL,
                    `salePrice` REAL NOT NULL,
                    `discountPercentage` REAL NOT NULL,
                    `quantity` INTEGER NOT NULL,
                    `taxPercentage` REAL NOT NULL,
                    `taxable` REAL NOT NULL,
                    `tax` REAL NOT NULL,
                    `total` REAL NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`saleId`) REFERENCES `sales`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())

            // Create index on sale_items.saleId for better query performance
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_saleId` ON `sale_items` (`saleId`)")
            
            // Create index on sale_items.productId for better query performance
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sale_items_productId` ON `sale_items` (`productId`)")
        }
    }
}
