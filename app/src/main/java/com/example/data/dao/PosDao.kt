package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StoreProfileEntity
import com.example.data.model.WholesalerInvoiceEntity
import kotlinx.coroutines.flow.Flow

data class TopProductStat(
    val productId: Int,
    val productName: String,
    val category: String,
    val totalSold: Int,
    val totalRevenue: Double
)

data class CategoryStat(
    val category: String,
    val totalSold: Int,
    val totalRevenue: Double
)

@Dao
interface PosDao {

    // --- Products ---
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isOnSale = 1 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isOnSale = 1 AND isNonBarcoded = 1 ORDER BY name ASC")
    fun getNonBarcodedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isOnSale = :isOnSale, updatedAt = :timestamp WHERE id = :id")
    suspend fun toggleProductOnSale(id: Int, isOnSale: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET quantity = quantity + :delta, updatedAt = :timestamp WHERE id = :id")
    suspend fun adjustProductStock(id: Int, delta: Int, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    // --- Sales ---
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getSalesSince(sinceTimestamp: Long): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Int): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItemEntity>>

    // Combined Atomic POS Sale Execution
    @Transaction
    suspend fun executeSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): Long {
        val saleId = insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId.toInt()) }
        insertSaleItems(itemsWithSaleId)

        // Decrement product inventory immediately in database
        for (item in items) {
            adjustProductStock(item.productId, -item.quantity)
        }
        return saleId
    }

    // --- Analytics Aggregations ---
    @Query("""
        SELECT productId, productName, category, SUM(quantity) as totalSold, SUM(subtotal) as totalRevenue
        FROM sale_items
        GROUP BY productId
        ORDER BY totalSold DESC
        LIMIT 10
    """)
    fun getTopSellingProducts(): Flow<List<TopProductStat>>

    @Query("""
        SELECT category, SUM(quantity) as totalSold, SUM(subtotal) as totalRevenue
        FROM sale_items
        GROUP BY category
        ORDER BY totalRevenue DESC
    """)
    fun getCategorySalesStats(): Flow<List<CategoryStat>>

    // --- Wholesaler Invoices (Supplier Discipline) ---
    @Query("SELECT * FROM wholesaler_invoices ORDER BY dueDate DESC")
    fun getAllWholesalerInvoices(): Flow<List<WholesalerInvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWholesalerInvoice(invoice: WholesalerInvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWholesalerInvoices(invoices: List<WholesalerInvoiceEntity>)

    @Update
    suspend fun updateWholesalerInvoice(invoice: WholesalerInvoiceEntity)

    @Query("UPDATE wholesaler_invoices SET status = 'PAID', paidDate = :paidDate, isPaidOnTime = :isPaidOnTime WHERE id = :id")
    suspend fun markInvoicePaid(id: Int, paidDate: Long = System.currentTimeMillis(), isPaidOnTime: Boolean = true)

    // --- Store Profile ---
    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    fun getStoreProfile(): Flow<StoreProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStoreProfile(profile: StoreProfileEntity)
}
