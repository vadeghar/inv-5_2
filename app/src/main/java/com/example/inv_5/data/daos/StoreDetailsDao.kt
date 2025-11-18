package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.inv_5.data.entities.StoreDetails

@Dao
interface StoreDetailsDao {

    @Query("SELECT * FROM store_details LIMIT 1")
    suspend fun getStoreDetails(): StoreDetails?

    @Query("SELECT EXISTS(SELECT 1 FROM store_details)")
    suspend fun hasStoreDetails(): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(storeDetails: StoreDetails)
}
