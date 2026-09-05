package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductThumbnailView
import com.example.ui.components.QuickTooltip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSecondary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun ManageProductsScreen(
    products: List<ProductEntity>,
    currencySymbol: String = "R",
    onToggleOnSale: (Int, Boolean) -> Unit,
    onAdjustStock: (Int, Int) -> Unit,
    onDeleteProduct: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: On Shelf (Selling), 1: Put Away (Hidden)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }

    val filteredProducts = products.filter { product ->
        val matchesTab = if (selectedTab == 0) product.isOnSale else !product.isOnSale
        val matchesCategory = selectedCategory == "All" || product.category == selectedCategory
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                product.barcode.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesCategory && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Header (Simple Everyday Wording)
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "Store Stock & Items",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = ImmersiveTextPrimary
            )
            Text(
                text = "Add stock, change selling status, or remove items.",
                style = MaterialTheme.typography.bodyMedium,
                color = ImmersiveTextSecondary
            )
        }

        // On Shelf vs Put Away Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ImmersiveSurface,
            contentColor = ImmersivePrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
        ) {
            val activeCount = products.count { it.isOnSale }
            val archivedCount = products.count { !it.isOnSale }

            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Sell,
                            contentDescription = null,
                            tint = if (selectedTab == 0) ImmersivePrimary else ImmersiveTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "On Shelf ($activeCount)",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) ImmersivePrimary else ImmersiveTextSecondary
                        )
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = null,
                            tint = if (selectedTab == 1) ImmersivePrimary else ImmersiveTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hidden Away ($archivedCount)",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) ImmersivePrimary else ImmersiveTextSecondary
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, category or barcode...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ImmersivePrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ImmersivePrimaryContainer,
                        selectedLabelColor = ImmersivePrimary,
                        containerColor = ImmersiveSurface,
                        labelColor = ImmersiveTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) ImmersivePrimary else ImmersiveBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product List
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = ImmersiveTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (selectedTab == 0) "No items found on shelf" else "No hidden items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ImmersiveTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductManagementCard(
                        product = product,
                        currencySymbol = currencySymbol,
                        onToggleOnSale = { onToggleOnSale(product.id, it) },
                        onAdjustStock = { delta -> onAdjustStock(product.id, delta) },
                        onDeleteClick = { productToDelete = product }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    // Delete Confirmation Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = ImmersiveSurface,
            title = { Text("Delete ${prod.name}?", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove this item from your shop list?", color = ImmersiveTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProduct(prod.id)
                        productToDelete = null
                    }
                ) {
                    Text("Delete Item", color = AccentRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Keep Item", color = ImmersiveTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ProductManagementCard(
    product: ProductEntity,
    currencySymbol: String,
    onToggleOnSale: (Boolean) -> Unit,
    onAdjustStock: (Int) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manage_product_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ImmersiveSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Picture + Name + Category + Toggle switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item Picture / Icon
                    ProductThumbnailView(
                        category = product.category,
                        imageUri = product.imageUri,
                        size = 50.dp,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (product.isOnSale) ImmersiveTextPrimary else ImmersiveTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = ImmersiveSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(text = "•", color = ImmersiveTextSecondary)
                            Text(
                                text = if (product.isNonBarcoded) "Fresh Item" else product.barcode,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = ImmersiveTextSecondary
                            )
                        }
                    }
                }

                // Friendly On Sale Toggle
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = product.isOnSale,
                        onCheckedChange = { onToggleOnSale(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ImmersivePrimary,
                            uncheckedTrackColor = ImmersiveBorder
                        ),
                        modifier = Modifier.testTag("sale_toggle_${product.id}")
                    )
                    Text(
                        text = if (product.isOnSale) "Selling" else "Hidden",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (product.isOnSale) ImmersivePrimary else ImmersiveTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Margins Info (Everyday Wording)
            val profitPerItem = product.price - product.costPrice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currencySymbol${String.format("%.2f", product.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmersivePrimary
                        )
                        Text(
                            text = " selling price",
                            style = MaterialTheme.typography.bodySmall,
                            color = ImmersiveTextSecondary
                        )
                    }
                    Text(
                        text = "Cost: $currencySymbol${String.format("%.2f", product.costPrice)} • Profit: $currencySymbol${String.format("%.2f", profitPerItem)} (${String.format("%.0f", product.profitMargin)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        color = if (profitPerItem >= 0) AccentEmerald else AccentRose
                    )
                }

                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (product.isLossLeader) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "Staple Item",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (product.isBulk) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ImmersivePrimaryContainer
                        ) {
                            Text(
                                text = "Box (${product.bulkPackSize}pk)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmersivePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Count & Fast +/- Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "On shelf: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = ImmersiveTextSecondary
                        )
                        Text(
                            text = "${product.quantity} items",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (product.quantity <= product.lowStockThreshold) AccentRose else ImmersiveTextPrimary
                        )
                        if (product.quantity <= product.lowStockThreshold) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Low Stock",
                                tint = AccentRose,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onAdjustStock(-1) },
                            enabled = product.quantity > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus 1", tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = { onAdjustStock(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus 1", tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                        }

                        if (product.isBulk && product.bulkPackSize > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ImmersivePrimaryContainer,
                                modifier = Modifier.clickable { onAdjustStock(product.bulkPackSize) }
                            ) {
                                Text(
                                    text = "+1 Box",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersivePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = AccentRose.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
