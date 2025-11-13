package com.example.inv_5.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.inv_5.data.daos.ProductDao
import com.example.inv_5.data.daos.PurchaseDao
import com.example.inv_5.data.daos.PurchaseItemDao
import com.example.inv_5.data.daos.SaleDao
import com.example.inv_5.data.daos.SaleItemDao
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.Purchase
import com.example.inv_5.data.entities.PurchaseItem
import com.example.inv_5.data.entities.Sale
import com.example.inv_5.data.entities.SaleItem

@Database(
    entities = [Product::class, Purchase::class, PurchaseItem::class, Sale::class, SaleItem::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
}