package com.example.inv_5.ui.sales

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.Sale
import com.example.inv_5.data.entities.SaleItem
import com.example.inv_5.data.repository.ActivityLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AddSaleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getInstance(application)
    private val saleDao = db.saleDao()
    private val saleItemDao = db.saleItemDao()
    private val productDao = db.productDao()
    private val activityLogRepo = ActivityLogRepository(application)

    // helper to find products by barcode
    suspend fun findProductsByBarcode(barcode: String) = productDao.getByBarcode(barcode)

    // expose save status so UI can observe progress / errors
    val saveStatus = MutableLiveData<String?>()

    fun saveSale(sale: Sale, saleItems: List<SaleItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            saveStatus.postValue("saving")
            try {
                db.withTransaction {
                    saleDao.insertSale(sale)
                    val now = Date()

                    saleItems.forEach { item ->
                        // ensure item references correct sale id
                        item.saleId = sale.id

                        // Product matching: find product by barcode and MRP
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
                            // Deduct stock for sale
                            val newQuantity = existing.quantityOnHand - item.quantity
                            if (newQuantity < 0) {
                                Log.w("AddSaleVM", "Warning: Stock going negative for product ${existing.id}, current: ${existing.quantityOnHand}, sale qty: ${item.quantity}")
                            }
                            val updated = existing.copy(
                                quantityOnHand = newQuantity,
                                updatedDt = now
                            )
                            productDao.updateProduct(updated)
                            item.productId = existing.id
                            Log.i("AddSaleVM", "Updated product id=${existing.id} for barcode='${item.productBarcode}' mrp=${item.mrp} — qtyDeducted=${item.quantity}, newStock=$newQuantity")
                        } else {
                            // Product not found - log warning, but still save the sale item
                            Log.w("AddSaleVM", "Product not found for barcode='${item.productBarcode}' mrp=${item.mrp} — sale item will be saved without stock adjustment")
                            item.productId = "" // No product link
                        }

                        saleItemDao.insertSaleItem(item)
                    }
                }

                // Log activity
                val isUpdate = saleDao.getById(sale.id) != null
                if (isUpdate) {
                    activityLogRepo.logSaleUpdated(sale)
                } else {
                    activityLogRepo.logSaleAdded(sale)
                }

                saveStatus.postValue("success")
            } catch (ex: Exception) {
                Log.e("AddSaleVM", "saveSale failed", ex)
                saveStatus.postValue("error: ${ex.message}")
            }
        }
    }
}
