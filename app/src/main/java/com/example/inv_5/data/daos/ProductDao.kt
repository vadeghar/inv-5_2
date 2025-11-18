package com.example.inv_5.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.inv_5.data.entities.Product

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("SELECT * FROM products WHERE barCode = :barCode AND mrp = :mrp LIMIT 1")
    suspend fun getByBarcodeAndMrp(barCode: String, mrp: Double): Product?

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Product?

    @Query("SELECT * FROM products WHERE mrp = :mrp AND name = :name LIMIT 1")
    suspend fun getByMrpAndName(mrp: Double, name: String): Product?

    @Query("SELECT * FROM products WHERE barCode = :barCode")
    suspend fun getByBarcode(barCode: String): List<Product>

    @Query("UPDATE products SET quantityOnHand = :newQuantity WHERE id = :id")
    suspend fun updateQuantity(id: String, newQuantity: Int)

    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR barCode LIKE '%' || :searchQuery || '%' 
        OR CAST(mrp AS TEXT) LIKE '%' || :searchQuery || '%'
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchProducts(searchQuery: String, limit: Int, offset: Int): List<Product>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getTotalCount(): Int

    @Query("""
        SELECT COUNT(*) FROM products 
        WHERE name LIKE '%' || :searchQuery || '%' 
        OR barCode LIKE '%' || :searchQuery || '%' 
        OR CAST(mrp AS TEXT) LIKE '%' || :searchQuery || '%'
    """)
    suspend fun getSearchCount(searchQuery: String): Int

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun listAll(): List<Product>

    // Dashboard queries
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getTotalProducts(): Int

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun getActiveProducts(): Int

    @Query("SELECT COUNT(*) FROM products WHERE quantityOnHand <= reorderPoint AND reorderPoint > 0")
    suspend fun getLowStockCount(): Int

    @Query("SELECT COUNT(*) FROM products WHERE quantityOnHand = 0")
    suspend fun getOutOfStockCount(): Int

    @Query("SELECT SUM(quantityOnHand * salePrice) FROM products WHERE quantityOnHand > 0")
    suspend fun getTotalInventoryValue(): Double?

    @Query("""
        SELECT * FROM products 
        WHERE quantityOnHand > 0 
        ORDER BY quantityOnHand * salePrice DESC 
        LIMIT :limit
    """)
    suspend fun getTopProductsByValue(limit: Int = 5): List<Product>
}