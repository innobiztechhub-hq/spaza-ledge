package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.TopProductStat
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.StoreProfileEntity
import com.example.ui.components.ProductSkeletonItem
import com.example.ui.components.ProductThumbnailView
import com.example.ui.components.QuickTooltip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.CloudySky
import com.example.ui.theme.CloudySkySoft
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderMedium
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSecondary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceElevated
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBlueDeep
import com.example.ui.theme.OceanGradient
import com.example.ui.theme.SkyPillGradient
import com.example.ui.util.ShopPdfExporter
import java.io.File

@Composable
fun DashboardScreen(
    profile: StoreProfileEntity,
    activeProducts: List<ProductEntity>,
    allSales: List<SaleEntity>,
    topSellingStats: List<TopProductStat>,
    isLoading: Boolean = false,
    currencySymbol: String = "R",
    onNavigateToPos: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onOpenProfileDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Total stock valuation on shelves
    val activeAssetWorth = activeProducts.sumOf { it.quantity * it.costPrice }
    val retailInventoryValue = activeProducts.sumOf { it.quantity * it.price }

    // Today's Sales
    val now = System.currentTimeMillis()
    val todayStart = now - (now % 86_400_000L)
    val todaySales = allSales.filter { it.timestamp >= todayStart }
    val todayRevenue = todaySales.sumOf { it.totalAmount }

    // Everyday staples (Bread, Maize, Milk - high foot traffic essentials)
    val potentialLossLeaders = activeProducts.filter { it.isLossLeader }

    // Slow moving items (items sitting longer on shelves)
    val soldProductIds = topSellingStats.map { it.productId }.toSet()
    val lowSellers = activeProducts.filter { it.id !in soldProductIds || (topSellingStats.find { s -> s.productId == it.id }?.totalSold ?: 0) <= 2 }

    // Low Stock Alert Count
    val lowStockCount = activeProducts.count { it.quantity <= it.lowStockThreshold }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Friendly Clean Welcome Header (Bespoke Combo Gradient Hero Banner)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CloudySky.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    OceanBlueDeep,
                                    OceanBlueDark,
                                    OceanBlue,
                                    Color(0xFF3B86B7)
                                )
                            )
                        )
                ) {
                    // Soft decorative ambient accents
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 45.dp, y = (-40).dp)
                            .clip(CircleShape)
                            .background(CloudySky.copy(alpha = 0.12f))
                    )
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = (-20).dp, y = 30.dp)
                            .clip(CircleShape)
                            .background(CloudySky.copy(alpha = 0.08f))
                    )

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Good day, ${profile.ownerName.split(" ").firstOrNull() ?: "Shopkeeper"}! 👋",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Everything looks ready for selling today.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            // Location chip (frosted white / cloudy sky style with white text)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = profile.location.split(",").first().trim(),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Big Friendly Action Buttons for Shop Owners
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .clickable { onNavigateToPos() }
                                    .testTag("dashboard_pos_shortcut"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.PointOfSale,
                                        contentDescription = null,
                                        tint = OceanBlueDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Sell Items",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = OceanBlueDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                    .clickable { onNavigateToAddProduct() }
                                    .testTag("dashboard_add_item_shortcut"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Add Item",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Three Clear Core Numbers (Simple Everyday Wording)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Stock on Shelves Full Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ImmersiveSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "TOTAL STOCK VALUE ON SHELVES",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", activeAssetWorth)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                text = "${activeProducts.size} item types in stock (Retail value: $currencySymbol${String.format("%,.2f", retailInventoryValue)})",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = ImmersiveTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(OceanGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Split Row: Today's Money & Low Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Money Made Today
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_card_today_revenue"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderMedium)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Money Made Today",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ImmersiveTextSecondary
                                )
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$currencySymbol${String.format("%.2f", todayRevenue)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                            Text(
                                text = "${todaySales.size} sales completed",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = ImmersiveTextSecondary
                            )
                        }
                    }

                    // Low Stock Alert
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_card_low_stock"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (lowStockCount > 0) AccentRose.copy(alpha = 0.4f) else ImmersiveBorderMedium)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Items Running Low",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ImmersiveTextSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (lowStockCount > 0) AccentRose else OceanBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$lowStockCount items",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (lowStockCount > 0) AccentRose else ImmersiveTextPrimary
                            )
                            Text(
                                text = if (lowStockCount > 0) "Needs more stock" else "Stock is healthy",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = if (lowStockCount > 0) AccentRose else OceanBlue
                            )
                        }
                    }
                }
            }
        }

        // 5. Section: Best Selling Items (What Customers Buy Most)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Best Selling Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                    Text(
                        text = "What your customers are buying most",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = ImmersiveTextSecondary
                    )
                }

                TextButton(onClick = onNavigateToAnalytics) {
                    Text("All Reports", fontSize = 13.sp, color = ImmersivePrimary, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = ImmersivePrimary)
                }
            }
        }

        if (isLoading) {
            items(3) {
                ProductSkeletonItem()
            }
        } else {
            val topItemsToShow = if (topSellingStats.isNotEmpty()) {
                topSellingStats.take(4)
            } else {
                activeProducts.take(3).map {
                    TopProductStat(it.id, it.name, it.category, 6, it.price * 6)
                }
            }

            items(topItemsToShow) { stat ->
                val matchingProduct = activeProducts.find { it.id == stat.productId }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ImmersiveSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductThumbnailView(
                                category = stat.category,
                                imageUri = matchingProduct?.imageUri,
                                size = 44.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = stat.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${stat.category} • ${stat.totalSold} sold",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ImmersiveTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$currencySymbol${String.format("%.2f", stat.totalRevenue)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmersivePrimary
                            )
                            Text(
                                text = "Sales Made",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = ImmersiveTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 6. Section: Everyday Essentials (Bread, Milk, Maize - Fast Daily Drivers)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "Everyday Essentials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Text(
                    text = "Daily staples like bread and milk that bring customers through your doors",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = ImmersiveTextSecondary
                )
            }
        }

        if (potentialLossLeaders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ImmersiveSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "All your products have steady profit margins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ImmersiveTextSecondary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(potentialLossLeaders.take(3)) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductThumbnailView(
                                category = product.category,
                                imageUri = product.imageUri,
                                size = 44.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Cost: $currencySymbol${String.format("%.2f", product.costPrice)} • Sells for: $currencySymbol${String.format("%.2f", product.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = ImmersiveTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CloudySkySoft
                        ) {
                            Text(
                                text = "Traffic Staple",
                                color = OceanBlueDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Section: Slow Selling Items (Needs attention)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "Slow Selling Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Text(
                    text = "Items sitting longer on shelves that you might want to promote or discount",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = ImmersiveTextSecondary
                )
            }
        }

        if (lowSellers.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ImmersiveSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Great news! All your items are moving steadily.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OceanBlue,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(lowSellers.take(2)) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderMedium)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductThumbnailView(
                                category = product.category,
                                imageUri = product.imageUri,
                                size = 44.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${product.quantity} left on shelf • Sells for $currencySymbol${String.format("%.2f", product.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = ImmersiveTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CloudySkySoft
                        ) {
                            Text(
                                text = "Still in stock",
                                color = OceanBlueDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
