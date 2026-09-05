package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val barcode: String = "",
    val name: String,
    val category: String,
    val price: Double,
    val costPrice: Double,
    val quantity: Int,
    val isBulk: Boolean = false,
    val bulkPackSize: Int = 1,
    val bulkCostPrice: Double = 0.0,
    val isOnSale: Boolean = true, // Simple toggle for active selling vs archived
    val isNonBarcoded: Boolean = false, // e.g. fruit, loose produce
    val unit: String = "unit",
    val lowStockThreshold: Int = 5,
    val imageUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profitMargin: Double
        get() = if (price > 0) ((price - costPrice) / price) * 100.0 else 0.0

    val isLossLeader: Boolean
        get() = profitMargin < 5.0 // Margin under 5% or negative, staple driver
}

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val saleNumber: String,
    val totalAmount: Double,
    val costAmount: Double,
    val paymentMethod: String = "CASH", // CASH, CARD, MOBILE_PAY
    val amountPaid: Double,
    val changeAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val itemCount: Int
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val saleId: Int,
    val productId: Int,
    val productName: String,
    val barcode: String,
    val category: String,
    val unitPrice: Double,
    val costPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

@Entity(tableName = "store_profile")
data class StoreProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val storeName: String = "Mabena's Express Mart",
    val ownerName: String = "Sipho Ndlovu",
    val ownerIdNumber: String = "8904125289081",
    val phoneNumber: String = "+27 82 555 0192",
    val location: String = "Section 4, Soweto, Gauteng",
    val gpsLat: Double = -26.2485,
    val gpsLng: Double = 27.8540,
    val storeType: String = "Spaza / Convenience Store",
    val currencySymbol: String = "R",
    val calculatedCreditScore: Int = 82,
    val isOnboarded: Boolean = true
)

@Entity(tableName = "wholesaler_invoices")
data class WholesalerInvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supplierName: String,
    val invoiceNumber: String,
    val amount: Double,
    val invoiceDate: Long = System.currentTimeMillis(),
    val dueDate: Long,
    val paidDate: Long? = null,
    val isPaidOnTime: Boolean = true,
    val status: String = "PAID" // PAID, PENDING, OVERDUE
)
