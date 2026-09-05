package com.example.data.repository

import com.example.data.dao.CategoryStat
import com.example.data.dao.PosDao
import com.example.data.dao.TopProductStat
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StoreProfileEntity
import com.example.data.model.WholesalerInvoiceEntity
import kotlinx.coroutines.flow.Flow

class PosRepository(private val posDao: PosDao) {

    val allProducts: Flow<List<ProductEntity>> = posDao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = posDao.getActiveProducts()
    val nonBarcodedProducts: Flow<List<ProductEntity>> = posDao.getNonBarcodedProducts()
    val allSales: Flow<List<SaleEntity>> = posDao.getAllSales()
    val allSaleItems: Flow<List<SaleItemEntity>> = posDao.getAllSaleItems()
    val topSellingProducts: Flow<List<TopProductStat>> = posDao.getTopSellingProducts()
    val categoryStats: Flow<List<CategoryStat>> = posDao.getCategorySalesStats()
    val storeProfile: Flow<StoreProfileEntity?> = posDao.getStoreProfile()
    val allWholesalerInvoices: Flow<List<WholesalerInvoiceEntity>> = posDao.getAllWholesalerInvoices()

    suspend fun getProductCount(): Int = posDao.getProductCount()

    fun getSalesSince(sinceTimestamp: Long): Flow<List<SaleEntity>> =
        posDao.getSalesSince(sinceTimestamp)

    suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        posDao.getProductByBarcode(barcode)

    suspend fun getProductById(id: Int): ProductEntity? =
        posDao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity): Long =
        posDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) =
        posDao.updateProduct(product)

    suspend fun toggleProductOnSale(id: Int, isOnSale: Boolean) =
        posDao.toggleProductOnSale(id, isOnSale)

    suspend fun adjustStock(id: Int, delta: Int) =
        posDao.adjustProductStock(id, delta)

    suspend fun deleteProduct(id: Int) =
        posDao.deleteProductById(id)

    suspend fun executeSale(sale: SaleEntity, items: List<SaleItemEntity>): Long =
        posDao.executeSale(sale, items)

    suspend fun saveStoreProfile(profile: StoreProfileEntity) =
        posDao.saveStoreProfile(profile)

    suspend fun insertWholesalerInvoice(invoice: WholesalerInvoiceEntity) =
        posDao.insertWholesalerInvoice(invoice)

    suspend fun markInvoicePaid(id: Int, isPaidOnTime: Boolean = true) =
        posDao.markInvoicePaid(id, isPaidOnTime = isPaidOnTime)
}
