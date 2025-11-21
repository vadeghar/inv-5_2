package com.example.inv_5.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.inv_5.data.dao.ActivityLogDao
import com.example.inv_5.data.dao.ExpenseCategoryDao
import com.example.inv_5.data.dao.ExpenseDao
import com.example.inv_5.data.dao.StockAdjustmentDao
import com.example.inv_5.data.daos.CustomerDao
import com.example.inv_5.data.daos.ProductDao
import com.example.inv_5.data.daos.PurchaseDao
import com.example.inv_5.data.daos.PurchaseItemDao
import com.example.inv_5.data.daos.SaleDao
import com.example.inv_5.data.daos.SaleItemDao
import com.example.inv_5.data.daos.StoreDetailsDao
import com.example.inv_5.data.daos.SupplierDao
import com.example.inv_5.data.entities.ActivityLog
import com.example.inv_5.data.entities.Customer
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.Purchase
import com.example.inv_5.data.entities.PurchaseItem
import com.example.inv_5.data.entities.Sale
import com.example.inv_5.data.entities.SaleItem
import com.example.inv_5.data.entities.StockAdjustment
import com.example.inv_5.data.entities.StoreDetails
import com.example.inv_5.data.entities.Supplier
import com.example.inv_5.data.model.Expense
import com.example.inv_5.data.model.ExpenseCategory

@Database(
    entities = [Product::class, Purchase::class, PurchaseItem::class, Sale::class, SaleItem::class, Supplier::class, Customer::class, StoreDetails::class, Expense::class, ExpenseCategory::class, ActivityLog::class, StockAdjustment::class],
    version = 14,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun supplierDao(): SupplierDao
    abstract fun customerDao(): CustomerDao
    abstract fun storeDetailsDao(): StoreDetailsDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
}