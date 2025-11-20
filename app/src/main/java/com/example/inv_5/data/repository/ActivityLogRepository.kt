package com.example.inv_5.data.repository

import android.content.Context
import com.example.inv_5.data.dao.ActivityLogDao
import com.example.inv_5.data.database.DatabaseProvider
import com.example.inv_5.data.entities.ActivityLog
import com.example.inv_5.data.entities.Customer
import com.example.inv_5.data.entities.Purchase
import com.example.inv_5.data.entities.Sale
import com.example.inv_5.data.entities.Product
import com.example.inv_5.data.entities.Supplier
import com.example.inv_5.data.model.Expense
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class ActivityLogRepository(context: Context) {
    
    private val activityLogDao: ActivityLogDao = DatabaseProvider.getInstance(context).activityLogDao()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    // Activity Types
    object ActivityType {
        const val ADD = "ADD"
        const val EDIT = "EDIT"
        const val DELETE = "DELETE"
    }

    // Entity Types
    object EntityType {
        const val PURCHASE = "PURCHASE"
        const val SALE = "SALE"
        const val EXPENSE = "EXPENSE"
        const val PRODUCT = "PRODUCT"
        const val CUSTOMER = "CUSTOMER"
        const val SUPPLIER = "SUPPLIER"
        const val REPORT = "REPORT"
    }

    // Purchase Activity Logging
    suspend fun logPurchaseAdded(purchase: Purchase, totalAmount: Double) {
        val log = ActivityLog(
            activityType = ActivityType.ADD,
            entityType = EntityType.PURCHASE,
            entityId = purchase.id,
            description = "Purchase added from ${purchase.vendor}",
            documentNumber = purchase.invoiceNo,
            amount = totalAmount,
            additionalInfo = purchase.vendor,
            timestamp = purchase.addedDate
        )
        activityLogDao.insert(log)
    }

    suspend fun logPurchaseUpdated(purchase: Purchase, totalAmount: Double) {
        val log = ActivityLog(
            activityType = ActivityType.EDIT,
            entityType = EntityType.PURCHASE,
            entityId = purchase.id,
            description = "Purchase updated for ${purchase.vendor}",
            documentNumber = purchase.invoiceNo,
            amount = totalAmount,
            additionalInfo = purchase.vendor,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logPurchaseDeleted(purchaseId: String, invoiceNo: String, vendor: String) {
        val log = ActivityLog(
            activityType = ActivityType.DELETE,
            entityType = EntityType.PURCHASE,
            entityId = purchaseId,
            description = "Purchase deleted for $vendor",
            documentNumber = invoiceNo,
            additionalInfo = vendor,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Sale Activity Logging
    suspend fun logSaleAdded(sale: Sale) {
        val customerName = sale.customerName.ifEmpty { "Walk-in Customer" }
        val log = ActivityLog(
            activityType = ActivityType.ADD,
            entityType = EntityType.SALE,
            entityId = sale.id,
            description = "Sale to $customerName",
            documentNumber = "SALE-${sale.id.take(8)}",
            amount = sale.totalAmount,
            additionalInfo = customerName,
            timestamp = sale.addedDate
        )
        activityLogDao.insert(log)
    }

    suspend fun logSaleUpdated(sale: Sale) {
        val customerName = sale.customerName.ifEmpty { "Walk-in Customer" }
        val log = ActivityLog(
            activityType = ActivityType.EDIT,
            entityType = EntityType.SALE,
            entityId = sale.id,
            description = "Sale updated for $customerName",
            documentNumber = "SALE-${sale.id.take(8)}",
            amount = sale.totalAmount,
            additionalInfo = customerName,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logSaleDeleted(saleId: String, customerName: String) {
        val customer = customerName.ifEmpty { "Walk-in Customer" }
        val log = ActivityLog(
            activityType = ActivityType.DELETE,
            entityType = EntityType.SALE,
            entityId = saleId,
            description = "Sale deleted for $customer",
            documentNumber = "SALE-${saleId.take(8)}",
            additionalInfo = customer,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Expense Activity Logging
    suspend fun logExpenseAdded(expense: Expense) {
        val log = ActivityLog(
            activityType = ActivityType.ADD,
            entityType = EntityType.EXPENSE,
            entityId = expense.expenseId.toString(),
            description = "${expense.expenseCategory} expense added",
            documentNumber = "EXP-${expense.expenseId}",
            amount = expense.totalAmount,
            additionalInfo = expense.expenseCategory,
            timestamp = expense.expenseDate
        )
        activityLogDao.insert(log)
    }

    suspend fun logExpenseUpdated(expense: Expense) {
        val log = ActivityLog(
            activityType = ActivityType.EDIT,
            entityType = EntityType.EXPENSE,
            entityId = expense.expenseId.toString(),
            description = "${expense.expenseCategory} expense updated",
            documentNumber = "EXP-${expense.expenseId}",
            amount = expense.totalAmount,
            additionalInfo = expense.expenseCategory,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logExpenseDeleted(expenseId: Long, category: String, amount: Double) {
        val log = ActivityLog(
            activityType = ActivityType.DELETE,
            entityType = EntityType.EXPENSE,
            entityId = expenseId.toString(),
            description = "$category expense deleted",
            documentNumber = "EXP-$expenseId",
            amount = amount,
            additionalInfo = category,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Product Activity Logging
    suspend fun logProductAdded(product: Product) {
        val log = ActivityLog(
            activityType = ActivityType.ADD,
            entityType = EntityType.PRODUCT,
            entityId = product.id,
            description = "Product '${product.name}' added",
            documentNumber = product.barCode,
            additionalInfo = product.name,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logProductUpdated(product: Product) {
        val log = ActivityLog(
            activityType = ActivityType.EDIT,
            entityType = EntityType.PRODUCT,
            entityId = product.id,
            description = "Product '${product.name}' updated",
            documentNumber = product.barCode,
            additionalInfo = product.name,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logProductDeleted(productId: String, productName: String, barcode: String) {
        val log = ActivityLog(
            activityType = ActivityType.DELETE,
            entityType = EntityType.PRODUCT,
            entityId = productId,
            description = "Product '$productName' deleted",
            documentNumber = barcode,
            additionalInfo = productName,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Customer Activity Logging
    suspend fun logCustomerAdded(customer: Customer) {
        val log = ActivityLog(
            activityType = ActivityType.ADD,
            entityType = EntityType.CUSTOMER,
            entityId = customer.id,
            description = "Customer '${customer.name}' added",
            documentNumber = customer.phone,
            additionalInfo = customer.name,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logCustomerUpdated(customer: Customer) {
        val log = ActivityLog(
            activityType = ActivityType.EDIT,
            entityType = EntityType.CUSTOMER,
            entityId = customer.id,
            description = "Customer '${customer.name}' updated",
            documentNumber = customer.phone,
            additionalInfo = customer.name,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logCustomerDeleted(customerId: String, customerName: String, phoneNumber: String) {
        val log = ActivityLog(
            activityType = ActivityType.DELETE,
            entityType = EntityType.CUSTOMER,
            entityId = customerId,
            description = "Customer '$customerName' deleted",
            documentNumber = phoneNumber,
            additionalInfo = customerName,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Supplier Activity Logging
    suspend fun logSupplierAdded(supplier: Supplier) {
        val log = ActivityLog(
            activityType = ActivityType.ADD,
            entityType = EntityType.SUPPLIER,
            entityId = supplier.id,
            description = "Supplier '${supplier.name}' added",
            documentNumber = supplier.phone,
            additionalInfo = supplier.name,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logSupplierUpdated(supplier: Supplier) {
        val log = ActivityLog(
            activityType = ActivityType.EDIT,
            entityType = EntityType.SUPPLIER,
            entityId = supplier.id,
            description = "Supplier '${supplier.name}' updated",
            documentNumber = supplier.phone,
            additionalInfo = supplier.name,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    suspend fun logSupplierDeleted(supplierId: String, supplierName: String, phoneNumber: String) {
        val log = ActivityLog(
            activityType = ActivityType.DELETE,
            entityType = EntityType.SUPPLIER,
            entityId = supplierId,
            description = "Supplier '$supplierName' deleted",
            documentNumber = phoneNumber,
            additionalInfo = supplierName,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Report Activity Logging
    suspend fun logReportGenerated(reportType: String, reportName: String, additionalInfo: String? = null) {
        val log = ActivityLog(
            activityType = "GENERATE",
            entityType = EntityType.REPORT,
            entityId = reportType,
            description = "Report '$reportName' generated",
            documentNumber = reportType,
            additionalInfo = additionalInfo,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Generic method for custom activities
    suspend fun logCustomActivity(
        activityType: String,
        entityType: String,
        entityId: String,
        description: String,
        documentNumber: String? = null,
        amount: Double? = null,
        additionalInfo: String? = null
    ) {
        val log = ActivityLog(
            activityType = activityType,
            entityType = entityType,
            entityId = entityId,
            description = description,
            documentNumber = documentNumber,
            amount = amount,
            additionalInfo = additionalInfo,
            timestamp = Date()
        )
        activityLogDao.insert(log)
    }

    // Query methods
    suspend fun getRecentActivities(limit: Int = 5) = activityLogDao.getRecentActivities(limit)
    
    suspend fun getAllActivities() = activityLogDao.getAllActivitiesList()
    
    fun getAllActivitiesLive() = activityLogDao.getAllActivities()
    
    fun searchActivities(query: String) = activityLogDao.searchActivities(query)
    
    suspend fun getActivitiesByEntityType(entityType: String) = 
        activityLogDao.getActivitiesByEntityType(entityType)
    
    suspend fun getActivitiesByActivityType(activityType: String) = 
        activityLogDao.getActivitiesByActivityType(activityType)
    
    suspend fun getActivitiesByDateRange(startDate: Date, endDate: Date) = 
        activityLogDao.getActivitiesByDateRange(startDate, endDate)
    
    suspend fun deleteOldActivities(cutoffDate: Date) = activityLogDao.deleteOldActivities(cutoffDate)
    
    suspend fun getActivityCount() = activityLogDao.getActivityCount()
}
