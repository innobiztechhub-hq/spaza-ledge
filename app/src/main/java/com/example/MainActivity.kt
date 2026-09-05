package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StoreProfileEntity
import com.example.ui.PosNavTab
import com.example.ui.PosViewModel
import com.example.ui.components.QuickTooltip
import com.example.ui.screens.AddProductScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ManageProductsScreen
import com.example.ui.screens.MerchantProfilePortalScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.StoreProfileDialog
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.CloudySky
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderMedium
import com.example.ui.theme.ImmersiveHeroGradient
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSecondary
import com.example.ui.theme.ImmersiveSecondaryContainer
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanGradient

class MainActivity : ComponentActivity() {

    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                PosMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PosMainApp(viewModel: PosViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val nonBarcodedProducts by viewModel.nonBarcodedProducts.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle()
    val topSellingProducts by viewModel.topSellingProducts.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val wholesalerInvoices by viewModel.wholesalerInvoices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    var showProfileDialog by remember { mutableStateOf(false) }

    val defaultProfile = storeProfile ?: StoreProfileEntity(
        storeName = "Mabena's Express Mart",
        ownerName = "Sipho Ndlovu",
        location = "Section 4, Soweto",
        storeType = "Spaza / Convenience Store",
        currencySymbol = "R",
        isOnboarded = true
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            // Immersive Top App Bar with Store Branding & Settings
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ImmersiveSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectTab(PosNavTab.PROFILE_PORTAL) }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OceanGradient)
                                .border(1.dp, CloudySky, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = "Kasi Ledger",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = defaultProfile.storeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${defaultProfile.storeType} • ${defaultProfile.location}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = ImmersiveTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    QuickTooltip(tooltipText = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode") {
                        IconButton(
                            onClick = { viewModel.toggleDarkTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = if (isDarkTheme) CloudySky else OceanBlue
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = ImmersiveSurface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderMedium)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OceanBlue,
                        selectedTextColor = OceanBlueDark,
                        indicatorColor = CloudySky.copy(alpha = 0.65f),
                        unselectedIconColor = ImmersiveTextMuted,
                        unselectedTextColor = ImmersiveTextMuted
                    )

                    // 1. Dashboard
                    NavigationBarItem(
                        selected = currentTab == PosNavTab.DASHBOARD,
                        onClick = { viewModel.selectTab(PosNavTab.DASHBOARD) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == PosNavTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    // 2. Analytics
                    NavigationBarItem(
                        selected = currentTab == PosNavTab.ANALYTICS,
                        onClick = { viewModel.selectTab(PosNavTab.ANALYTICS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == PosNavTab.ANALYTICS) Icons.Default.BarChart else Icons.Outlined.BarChart,
                                contentDescription = "Reports & PDF"
                            )
                        },
                        label = { Text("Reports", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_analytics")
                    )

                    // 3. Add Product (Middle Nav Button)
                    NavigationBarItem(
                        selected = currentTab == PosNavTab.ADD_PRODUCT,
                        onClick = { viewModel.selectTab(PosNavTab.ADD_PRODUCT) },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ImmersivePrimaryContainer)
                                    .border(1.dp, ImmersiveBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Product",
                                    tint = ImmersivePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = { Text("Add Item", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_add_product")
                    )

                    // 4. Manage Products
                    NavigationBarItem(
                        selected = currentTab == PosNavTab.MANAGE_PRODUCTS,
                        onClick = { viewModel.selectTab(PosNavTab.MANAGE_PRODUCTS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == PosNavTab.MANAGE_PRODUCTS) Icons.Default.Inventory2 else Icons.Outlined.Inventory2,
                                contentDescription = "Stock"
                            )
                        },
                        label = { Text("Stock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_manage")
                    )

                    // 5. POS Checkout
                    NavigationBarItem(
                        selected = currentTab == PosNavTab.POS_CHECKOUT,
                        onClick = { viewModel.selectTab(PosNavTab.POS_CHECKOUT) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == PosNavTab.POS_CHECKOUT) Icons.Default.PointOfSale else Icons.Outlined.PointOfSale,
                                contentDescription = "Sell"
                            )
                        },
                        label = { Text("Sell", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("nav_pos")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp) // Mobile responsive constraint for tablets
            ) {
                Crossfade(targetState = currentTab, label = "screen_transition") { tab ->
                    when (tab) {
                        PosNavTab.DASHBOARD -> DashboardScreen(
                            profile = defaultProfile,
                            activeProducts = activeProducts,
                            allSales = allSales,
                            topSellingStats = topSellingProducts,
                            isLoading = isLoading,
                            currencySymbol = defaultProfile.currencySymbol,
                            onNavigateToPos = { viewModel.selectTab(PosNavTab.POS_CHECKOUT) },
                            onNavigateToAddProduct = { viewModel.selectTab(PosNavTab.ADD_PRODUCT) },
                            onNavigateToAnalytics = { viewModel.selectTab(PosNavTab.ANALYTICS) },
                            onOpenProfileDialog = { viewModel.selectTab(PosNavTab.PROFILE_PORTAL) }
                        )

                        PosNavTab.ANALYTICS -> AnalyticsScreen(
                            sales = allSales,
                            categoryStats = categoryStats,
                            activeProducts = activeProducts,
                            currencySymbol = defaultProfile.currencySymbol,
                            profile = defaultProfile,
                            onNavigateToProfilePortal = { viewModel.selectTab(PosNavTab.PROFILE_PORTAL) }
                        )

                        PosNavTab.ADD_PRODUCT -> AddProductScreen(
                            currencySymbol = defaultProfile.currencySymbol,
                            existingProducts = allProducts,
                            onProductAdded = { newProduct ->
                                viewModel.addProduct(newProduct)
                            }
                        )

                        PosNavTab.MANAGE_PRODUCTS -> ManageProductsScreen(
                            products = allProducts,
                            currencySymbol = defaultProfile.currencySymbol,
                            onToggleOnSale = { prodId, isOnSale ->
                                viewModel.toggleProductOnSale(prodId, isOnSale)
                            },
                            onAdjustStock = { prodId, delta ->
                                viewModel.adjustProductStock(prodId, delta)
                            },
                            onDeleteProduct = { prodId ->
                                viewModel.deleteProduct(prodId)
                            }
                        )

                        PosNavTab.POS_CHECKOUT -> PosScreen(
                            activeProducts = activeProducts,
                            nonBarcodedProducts = nonBarcodedProducts,
                            currencySymbol = defaultProfile.currencySymbol,
                            onProcessSale = { sale, items ->
                                viewModel.executeSale(sale, items)
                            }
                        )

                        PosNavTab.PROFILE_PORTAL -> MerchantProfilePortalScreen(
                            profile = defaultProfile,
                            sales = allSales,
                            activeProducts = activeProducts,
                            wholesalerInvoices = wholesalerInvoices,
                            onSaveProfile = { updatedProfile ->
                                viewModel.saveStoreProfile(updatedProfile)
                            },
                            onAddInvoice = { newInvoice ->
                                viewModel.addWholesalerInvoice(newInvoice)
                            },
                            onMarkInvoicePaid = { invoiceId, isPaidOnTime ->
                                viewModel.markInvoicePaid(invoiceId, isPaidOnTime)
                            },
                            onBackClick = { viewModel.selectTab(PosNavTab.DASHBOARD) }
                        )
                    }
                }
            }
        }
    }

    // Store Profile Setup / Edit Dialog
    if (showProfileDialog) {
        StoreProfileDialog(
            initialProfile = defaultProfile,
            onSave = { updatedProfile ->
                viewModel.saveStoreProfile(updatedProfile)
                showProfileDialog = false
            },
            onDismiss = { showProfileDialog = false }
        )
    }
}
