package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PosDao
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StoreProfileEntity
import com.example.data.model.WholesalerInvoiceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StoreProfileEntity::class,
        WholesalerInvoiceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quick_pos_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate starter store inventory and sample sales
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).populateInitialData()
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).populateInitialData()
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun populateInitialData() {
        val dao = posDao()

        // 1. Initial Store Profile with full Identity Anchoring (KYC Module)
        val initialProfile = StoreProfileEntity(
            id = 1,
            storeName = "Mabena's Express Mart",
            ownerName = "Sipho Ndlovu",
            ownerIdNumber = "8904125289081",
            phoneNumber = "+27 82 555 0192",
            location = "Section 4, Soweto, Gauteng",
            gpsLat = -26.2485,
            gpsLng = 27.8540,
            storeType = "Spaza / Convenience Store",
            currencySymbol = "R",
            calculatedCreditScore = 84,
            isOnboarded = true
        )
        dao.saveStoreProfile(initialProfile)

        // 2. Initial Products with realistic prices, bulk packaging, non-barcoded fresh items, and loss leaders
        val products = listOf(
            ProductEntity(
                id = 1,
                barcode = "6001007011234",
                name = "Simba Potato Chips (Mexican Chilli 120g)",
                category = "Snacks",
                price = 18.50,
                costPrice = 13.00,
                quantity = 48,
                isBulk = true,
                bulkPackSize = 24,
                bulkCostPrice = 312.00, // 24 * 13
                isOnSale = true,
                isNonBarcoded = false,
                unit = "pack",
                lowStockThreshold = 10,
                imageUri = "preset:chips"
            ),
            ProductEntity(
                id = 2,
                barcode = "6001007055678",
                name = "Albany Superior Sliced White Bread 700g",
                category = "Bakery",
                price = 17.00,
                costPrice = 16.50, // Loss leader staple: very thin margin to drive store foot traffic
                quantity = 35,
                isBulk = false,
                isOnSale = true,
                isNonBarcoded = false,
                unit = "loaf",
                lowStockThreshold = 8,
                imageUri = "preset:bread"
            ),
            ProductEntity(
                id = 3,
                barcode = "5449000000996",
                name = "Coca-Cola Original Taste 500ml",
                category = "Beverages",
                price = 15.00,
                costPrice = 10.50,
                quantity = 60,
                isBulk = true,
                bulkPackSize = 24,
                bulkCostPrice = 252.00,
                isOnSale = true,
                isNonBarcoded = false,
                unit = "bottle",
                lowStockThreshold = 12,
                imageUri = "preset:coke"
            ),
            ProductEntity(
                id = 4,
                barcode = "NB-BANANA-001",
                name = "Fresh Bananas (Golden Ripe)",
                category = "Fresh Produce",
                price = 3.50,
                costPrice = 1.80,
                quantity = 75,
                isBulk = false,
                isOnSale = true,
                isNonBarcoded = true, // Fruit with no barcode!
                unit = "item",
                lowStockThreshold = 15,
                imageUri = "preset:banana"
            ),
            ProductEntity(
                id = 5,
                barcode = "NB-APPLE-002",
                name = "Crisp Red Royal Gala Apple",
                category = "Fresh Produce",
                price = 4.00,
                costPrice = 2.10,
                quantity = 65,
                isBulk = false,
                isOnSale = true,
                isNonBarcoded = true, // Fruit with no barcode!
                unit = "item",
                lowStockThreshold = 12,
                imageUri = "preset:apple"
            ),
            ProductEntity(
                id = 6,
                barcode = "6001299003321",
                name = "Clover Fresh Full Cream Milk 2L",
                category = "Dairy",
                price = 36.00,
                costPrice = 31.00,
                quantity = 22,
                isBulk = false,
                isOnSale = true,
                isNonBarcoded = false,
                unit = "bottle",
                lowStockThreshold = 6,
                imageUri = "preset:milk"
            ),
            ProductEntity(
                id = 7,
                barcode = "6001087342019",
                name = "White Star Super Maize Meal 2.5kg",
                category = "Staples",
                price = 38.00,
                costPrice = 37.00, // Loss leader essential staple
                quantity = 28,
                isBulk = false,
                isOnSale = true,
                isNonBarcoded = false,
                unit = "bag",
                lowStockThreshold = 5,
                imageUri = "preset:maize"
            ),
            ProductEntity(
                id = 8,
                barcode = "6001085112003",
                name = "Sunlight Dishwashing Liquid 750ml",
                category = "Household",
                price = 34.00,
                costPrice = 24.50,
                quantity = 18,
                isBulk = false,
                isOnSale = true,
                isNonBarcoded = false,
                unit = "bottle",
                lowStockThreshold = 4,
                imageUri = "preset:soap"
            ),
            ProductEntity(
                id = 9,
                barcode = "6009802821102",
                name = "Score Energy Drink Passion 500ml",
                category = "Beverages",
                price = 13.00,
                costPrice = 8.20,
                quantity = 40,
                isBulk = true,
                bulkPackSize = 24,
                bulkCostPrice = 196.80,
                isOnSale = true,
                isNonBarcoded = false,
                unit = "can",
                lowStockThreshold = 10,
                imageUri = "preset:energy"
            ),
            ProductEntity(
                id = 10,
                barcode = "6001065001222",
                name = "Beacon Sparkles Candy Roll",
                category = "Confectionery",
                price = 5.00,
                costPrice = 2.50,
                quantity = 8, // Low seller / stagnant
                isOnSale = true,
                isNonBarcoded = false,
                unit = "roll",
                lowStockThreshold = 15,
                imageUri = "preset:candy"
            ),
            ProductEntity(
                id = 11,
                barcode = "6009900112233",
                name = "Winter Knit Woolen Beanie",
                category = "General",
                price = 50.00,
                costPrice = 30.00,
                quantity = 4,
                isOnSale = false, // Archived item to test toggle & revenue exclusion!
                isNonBarcoded = false,
                unit = "unit",
                lowStockThreshold = 2,
                imageUri = "preset:beanie"
            )
        )
        dao.insertProducts(products)

        // 3. Populate historical sample sales across past days for analytics charts
        val now = System.currentTimeMillis()
        val dayMillis = 86_400_000L

        val sampleSales = listOf(
            // Today
            Pair(
                SaleEntity(
                    saleNumber = "#SALE-1048",
                    totalAmount = 50.50,
                    costAmount = 40.00,
                    paymentMethod = "CASH",
                    amountPaid = 60.00,
                    changeAmount = 9.50,
                    timestamp = now - 3_600_000L,
                    itemCount = 3
                ),
                listOf(
                    SaleItemEntity(saleId = 0, productId = 1, productName = "Simba Potato Chips (Mexican Chilli 120g)", barcode = "6001007011234", category = "Snacks", unitPrice = 18.50, costPrice = 13.00, quantity = 1, subtotal = 18.50),
                    SaleItemEntity(saleId = 0, productId = 2, productName = "Albany Superior Sliced White Bread 700g", barcode = "6001007055678", category = "Bakery", unitPrice = 17.00, costPrice = 16.50, quantity = 1, subtotal = 17.00),
                    SaleItemEntity(saleId = 0, productId = 3, productName = "Coca-Cola Original Taste 500ml", barcode = "5449000000996", category = "Beverages", unitPrice = 15.00, costPrice = 10.50, quantity = 1, subtotal = 15.00)
                )
            ),
            Pair(
                SaleEntity(
                    saleNumber = "#SALE-1049",
                    totalAmount = 22.00,
                    costAmount = 12.60,
                    paymentMethod = "CARD",
                    amountPaid = 22.00,
                    changeAmount = 0.0,
                    timestamp = now - 1_800_000L,
                    itemCount = 3
                ),
                listOf(
                    SaleItemEntity(saleId = 0, productId = 4, productName = "Fresh Bananas (Golden Ripe)", barcode = "NB-BANANA-001", category = "Fresh Produce", unitPrice = 3.50, costPrice = 1.80, quantity = 2, subtotal = 7.00),
                    SaleItemEntity(saleId = 0, productId = 3, productName = "Coca-Cola Original Taste 500ml", barcode = "5449000000996", category = "Beverages", unitPrice = 15.00, costPrice = 10.50, quantity = 1, subtotal = 15.00)
                )
            ),
            // Yesterday
            Pair(
                SaleEntity(
                    saleNumber = "#SALE-1046",
                    totalAmount = 108.50,
                    costAmount = 86.50,
                    paymentMethod = "CASH",
                    amountPaid = 120.00,
                    changeAmount = 11.50,
                    timestamp = now - dayMillis,
                    itemCount = 4
                ),
                listOf(
                    SaleItemEntity(saleId = 0, productId = 6, productName = "Clover Fresh Full Cream Milk 2L", barcode = "6001299003321", category = "Dairy", unitPrice = 36.00, costPrice = 31.00, quantity = 1, subtotal = 36.00),
                    SaleItemEntity(saleId = 0, productId = 7, productName = "White Star Super Maize Meal 2.5kg", barcode = "6001087342019", category = "Staples", unitPrice = 38.00, costPrice = 37.00, quantity = 1, subtotal = 38.00),
                    SaleItemEntity(saleId = 0, productId = 1, productName = "Simba Potato Chips (Mexican Chilli 120g)", barcode = "6001007011234", category = "Snacks", unitPrice = 18.50, costPrice = 13.00, quantity = 1, subtotal = 18.50),
                    SaleItemEntity(saleId = 0, productId = 2, productName = "Albany Superior Sliced White Bread 700g", barcode = "6001007055678", category = "Bakery", unitPrice = 17.00, costPrice = 16.50, quantity = 1, subtotal = 17.00)
                )
            ),
            // 2 Days ago
            Pair(
                SaleEntity(
                    saleNumber = "#SALE-1044",
                    totalAmount = 87.00,
                    costAmount = 62.50,
                    paymentMethod = "MOBILE_PAY",
                    amountPaid = 87.00,
                    changeAmount = 0.0,
                    timestamp = now - (dayMillis * 2),
                    itemCount = 4
                ),
                listOf(
                    SaleItemEntity(saleId = 0, productId = 8, productName = "Sunlight Dishwashing Liquid 750ml", barcode = "6001085112003", category = "Household", unitPrice = 34.00, costPrice = 24.50, quantity = 1, subtotal = 34.00),
                    SaleItemEntity(saleId = 0, productId = 9, productName = "Score Energy Drink Passion 500ml", barcode = "6009802821102", category = "Beverages", unitPrice = 13.00, costPrice = 8.20, quantity = 2, subtotal = 26.00),
                    SaleItemEntity(saleId = 0, productId = 2, productName = "Albany Superior Sliced White Bread 700g", barcode = "6001007055678", category = "Bakery", unitPrice = 17.00, costPrice = 16.50, quantity = 1, subtotal = 17.00)
                )
            ),
            // 3 Days ago
            Pair(
                SaleEntity(
                    saleNumber = "#SALE-1041",
                    totalAmount = 74.00,
                    costAmount = 53.00,
                    paymentMethod = "CASH",
                    amountPaid = 100.00,
                    changeAmount = 26.00,
                    timestamp = now - (dayMillis * 3),
                    itemCount = 4
                ),
                listOf(
                    SaleItemEntity(saleId = 0, productId = 1, productName = "Simba Potato Chips (Mexican Chilli 120g)", barcode = "6001007011234", category = "Snacks", unitPrice = 18.50, costPrice = 13.00, quantity = 2, subtotal = 37.00),
                    SaleItemEntity(saleId = 0, productId = 3, productName = "Coca-Cola Original Taste 500ml", barcode = "5449000000996", category = "Beverages", unitPrice = 15.00, costPrice = 10.50, quantity = 2, subtotal = 30.00),
                    SaleItemEntity(saleId = 0, productId = 5, productName = "Crisp Red Royal Gala Apple", barcode = "NB-APPLE-002", category = "Fresh Produce", unitPrice = 4.00, costPrice = 2.10, quantity = 1, subtotal = 4.00)
                )
            ),
            // 4 Days ago
            Pair(
                SaleEntity(
                    saleNumber = "#SALE-1038",
                    totalAmount = 91.00,
                    costAmount = 73.50,
                    paymentMethod = "CASH",
                    amountPaid = 100.00,
                    changeAmount = 9.00,
                    timestamp = now - (dayMillis * 4),
                    itemCount = 4
                ),
                listOf(
                    SaleItemEntity(saleId = 0, productId = 6, productName = "Clover Fresh Full Cream Milk 2L", barcode = "6001299003321", category = "Dairy", unitPrice = 36.00, costPrice = 31.00, quantity = 1, subtotal = 36.00),
                    SaleItemEntity(saleId = 0, productId = 2, productName = "Albany Superior Sliced White Bread 700g", barcode = "6001007055678", category = "Bakery", unitPrice = 17.00, costPrice = 16.50, quantity = 1, subtotal = 17.00),
                    SaleItemEntity(saleId = 0, productId = 7, productName = "White Star Super Maize Meal 2.5kg", barcode = "6001087342019", category = "Staples", unitPrice = 38.00, costPrice = 37.00, quantity = 1, subtotal = 38.00)
                )
            ),
            // 6 Days ago
            Pair(
                SaleEntity(
                    saleNumber = "#SALE-1033",
                    totalAmount = 62.50,
                    costAmount = 45.00,
                    paymentMethod = "CARD",
                    amountPaid = 62.50,
                    changeAmount = 0.0,
                    timestamp = now - (dayMillis * 6),
                    itemCount = 3
                ),
                listOf(
                    SaleItemEntity(saleId = 0, productId = 1, productName = "Simba Potato Chips (Mexican Chilli 120g)", barcode = "6001007011234", category = "Snacks", unitPrice = 18.50, costPrice = 13.00, quantity = 2, subtotal = 37.00),
                    SaleItemEntity(saleId = 0, productId = 9, productName = "Score Energy Drink Passion 500ml", barcode = "6009802821102", category = "Beverages", unitPrice = 13.00, costPrice = 8.20, quantity = 1, subtotal = 13.00),
                    SaleItemEntity(saleId = 0, productId = 4, productName = "Fresh Bananas (Golden Ripe)", barcode = "NB-BANANA-001", category = "Fresh Produce", unitPrice = 3.50, costPrice = 1.80, quantity = 3, subtotal = 10.50)
                )
            )
        )

        for ((sale, items) in sampleSales) {
            dao.executeSale(sale, items)
        }

        // 4. Seed Wholesaler Credit Invoices (Supplier Discipline - 30% weighting)
        val initialInvoices = listOf(
            WholesalerInvoiceEntity(
                supplierName = "Metro Cash & Carry Crown Mines",
                invoiceNumber = "MCC-2026-9912",
                amount = 3450.00,
                invoiceDate = now - (dayMillis * 25),
                dueDate = now - (dayMillis * 10),
                paidDate = now - (dayMillis * 11), // 1 day before due date -> On time!
                isPaidOnTime = true,
                status = "PAID"
            ),
            WholesalerInvoiceEntity(
                supplierName = "Tiger Brands FMCG Depot",
                invoiceNumber = "TGR-88214",
                amount = 1820.00,
                invoiceDate = now - (dayMillis * 20),
                dueDate = now - (dayMillis * 6),
                paidDate = now - (dayMillis * 7), // On time!
                isPaidOnTime = true,
                status = "PAID"
            ),
            WholesalerInvoiceEntity(
                supplierName = "Albany Bakeries Distribution",
                invoiceNumber = "ALB-3104",
                amount = 940.00,
                invoiceDate = now - (dayMillis * 14),
                dueDate = now - (dayMillis * 3),
                paidDate = now - (dayMillis * 3), // Paid on due date -> On time!
                isPaidOnTime = true,
                status = "PAID"
            ),
            WholesalerInvoiceEntity(
                supplierName = "Premier FMCG Milling",
                invoiceNumber = "PRM-5520",
                amount = 2100.00,
                invoiceDate = now - (dayMillis * 8),
                dueDate = now + (dayMillis * 5),
                paidDate = null,
                isPaidOnTime = false,
                status = "PENDING"
            )
        )
        dao.insertWholesalerInvoices(initialInvoices)
    }
}
