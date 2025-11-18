package com.example.inv_5.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .addMigrations(
                    ProductMigrations.MIGRATION_1_2,
                    PurchaseItemMigrations.MIGRATION_2_3,
                    PurchaseMigrations.MIGRATION_3_4,
                    SaleMigrations.MIGRATION_4_5,
                    SupplierMigrations.MIGRATION_5_6,
                    SupplierMigrations.MIGRATION_6_7,
                    CustomerMigrations.MIGRATION_7_8,
                    CustomerMigrations.MIGRATION_8_9,
                    StoreDetailsMigrations.MIGRATION_9_10
                )
                .build()
        }
        return instance!!
    }
}