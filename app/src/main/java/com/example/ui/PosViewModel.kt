package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.dao.CategoryStat
import com.example.data.dao.TopProductStat
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StoreProfileEntity
import com.example.data.model.WholesalerInvoiceEntity
import com.example.data.repository.PosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PosNavTab {
    DASHBOARD,
    ANALYTICS,
    ADD_PRODUCT,
    MANAGE_PRODUCTS,
    POS_CHECKOUT,
    PROFILE_PORTAL
}

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PosRepository
    private val database: AppDatabase

    val allProducts: StateFlow<List<ProductEntity>>
    val activeProducts: StateFlow<List<ProductEntity>>
    val nonBarcodedProducts: StateFlow<List<ProductEntity>>
    val allSales: StateFlow<List<SaleEntity>>
    val topSellingProducts: StateFlow<List<TopProductStat>>
    val categoryStats: StateFlow<List<CategoryStat>>
    val storeProfile: StateFlow<StoreProfileEntity?>
    val wholesalerInvoices: StateFlow<List<WholesalerInvoiceEntity>>

    private val _currentTab = MutableStateFlow(PosNavTab.DASHBOARD)
    val currentTab: StateFlow<PosNavTab> = _currentTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        database = AppDatabase.getInstance(application)
        repository = PosRepository(database.posDao())

        // Guarantee mockups and starter inventory are loaded if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.getProductCount()
            if (count == 0) {
                database.populateInitialData()
            }
        }

        allProducts = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeProducts = repository.activeProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        nonBarcodedProducts = repository.nonBarcodedProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSales = repository.allSales.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        topSellingProducts = repository.topSellingProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        categoryStats = repository.categoryStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        storeProfile = repository.storeProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        wholesalerInvoices = repository.allWholesalerInvoices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectTab(tab: PosNavTab) {
        _currentTab.value = tab
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun toggleProductOnSale(productId: Int, isOnSale: Boolean) {
        viewModelScope.launch {
            repository.toggleProductOnSale(productId, isOnSale)
        }
    }

    fun adjustProductStock(productId: Int, delta: Int) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta)
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun executeSale(sale: SaleEntity, items: List<SaleItemEntity>) {
        viewModelScope.launch {
            repository.executeSale(sale, items)
        }
    }

    fun saveStoreProfile(profile: StoreProfileEntity) {
        viewModelScope.launch {
            repository.saveStoreProfile(profile)
        }
    }

    fun addWholesalerInvoice(invoice: WholesalerInvoiceEntity) {
        viewModelScope.launch {
            repository.insertWholesalerInvoice(invoice)
        }
    }

    fun markInvoicePaid(id: Int, isPaidOnTime: Boolean = true) {
        viewModelScope.launch {
            repository.markInvoicePaid(id, isPaidOnTime = isPaidOnTime)
        }
    }

    fun restoreDemoData() {
        viewModelScope.launch(Dispatchers.IO) {
            database.populateInitialData()
        }
    }
}
