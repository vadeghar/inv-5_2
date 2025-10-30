package com.example.inv_5.ui.purchases

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.Purchase
import com.example.inv_5.data.entities.PurchaseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AddPurchaseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getInstance(application)
    private val purchaseDao = db.purchaseDao()
    private val purchaseItemDao = db.purchaseItemDao()
    private val productDao = db.productDao()

    // helper to find products by barcode (may return multiple MRPs)
    suspend fun findProductsByBarcode(barcode: String) = productDao.getByBarcode(barcode)

    // expose save status so UI can observe progress / errors
    val saveStatus = MutableLiveData<String?>()

    fun savePurchase(purchase: Purchase, purchaseItems: List<PurchaseItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            saveStatus.postValue("saving")
            try {
                db.withTransaction {
                    purchaseDao.insertPurchase(purchase)
                    val now = Date()

                    purchaseItems.forEach { item ->
                        // ensure item references correct purchase id
                        item.purchaseId = purchase.id

                        // Product matching: prefer exact barcode+mrp, fallback to nearest mrp within tolerance, else create
                        var existing: Product? = null
                        val barcode = item.productBarcode ?: ""
                        if (barcode.isNotBlank()) {
                            existing = try {
                                productDao.getByBarcodeAndMrp(barcode, item.mrp)
                            } catch (e: Exception) {
                                null
                            }

                            if (existing == null) {
                                val candidates = productDao.getByBarcode(barcode)
                                if (candidates.isNotEmpty()) {
                                    existing = candidates.minByOrNull { kotlin.math.abs(it.mrp - item.mrp) }
                                    if (existing != null && kotlin.math.abs(existing.mrp - item.mrp) > 0.01) {
                                        // treat as non-match when difference > 0.01
                                        existing = null
                                    }
                                }
                            }
                        }

                        if (existing != null) {
                            val updated = existing.copy(
                                quantityOnHand = existing.quantityOnHand + item.quantity,
                                updatedDt = now
                            )
                            productDao.updateProduct(updated)
                            // ensure item.productId references the existing product
                            item.productId = existing.id
                            Log.i("AddPurchaseVM", "Matched existing product id=${existing.id} for barcode='${item.productBarcode}' mrp=${item.mrp} — qtyAdded=${item.quantity}")
                        } else {
                            val newProduct = Product(
                                id = System.currentTimeMillis().toString(),
                                name = if (item.productName.isNullOrBlank()) item.hsn ?: "" else item.productName,
                                mrp = item.mrp,
                                salePrice = item.productSalePrice ?: item.rate,
                                barCode = if (item.productBarcode.isNullOrBlank()) "" else item.productBarcode,
                                category = item.hsn ?: "",
                                quantityOnHand = item.quantity,
                                reorderPoint = 1,
                                maximumStockLevel = 5,
                                isActive = true,
                                addedDt = now,
                                updatedDt = now
                            )
                            productDao.insertProduct(newProduct)
                            item.productId = newProduct.id
                            Log.i("AddPurchaseVM", "Created new product id=${newProduct.id} for barcode='${item.productBarcode}' mrp=${item.mrp} — qty=${item.quantity}")
                        }

                        purchaseItemDao.insertPurchaseItem(item)
                    }
                }

                saveStatus.postValue("success")
            } catch (ex: Exception) {
                Log.e("AddPurchaseVM", "savePurchase failed", ex)
                saveStatus.postValue("error: ${ex.message}")
            }
        }
    }
}