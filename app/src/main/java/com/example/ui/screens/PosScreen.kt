package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.ProductThumbnailView
import com.example.ui.components.QuickTooltip
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandNavy800
import com.example.ui.theme.CloudySky
import com.example.ui.theme.CloudySkySoft
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderMedium
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSecondary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTertiary
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBlueDeep
import com.example.ui.theme.OceanGradient
import com.example.ui.theme.SkyPillGradient

data class CartItem(
    val product: ProductEntity,
    var quantity: Int
) {
    val subtotal: Double
        get() = product.price * quantity

    val totalCost: Double
        get() = product.costPrice * quantity
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    activeProducts: List<ProductEntity>,
    nonBarcodedProducts: List<ProductEntity>,
    currencySymbol: String = "R",
    onProcessSale: (SaleEntity, List<SaleItemEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Shopping Cart State
    val cartItems = remember { mutableStateListOf<CartItem>() }

    // Search and Non-barcoded selection
    var nonBarcodedSearchQuery by remember { mutableStateOf("") }
    var isFruitDropdownExpanded by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var completedSaleReceipt by remember { mutableStateOf<Pair<SaleEntity, List<SaleItemEntity>>?>(null) }
    var scanNotification by remember { mutableStateOf<String?>(null) }

    val subtotal by remember {
        derivedStateOf { cartItems.sumOf { it.subtotal } }
    }
    val totalCost by remember {
        derivedStateOf { cartItems.sumOf { it.totalCost } }
    }
    val totalItemsCount by remember {
        derivedStateOf { cartItems.sumOf { it.quantity } }
    }

    // Fast Barcode addition logic
    fun addProductToCart(product: ProductEntity) {
        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val current = cartItems[existingIndex]
            if (current.quantity < product.quantity) {
                cartItems[existingIndex] = current.copy(quantity = current.quantity + 1)
                scanNotification = "Added 1x ${product.name}"
            } else {
                scanNotification = "Max inventory reached for ${product.name}"
            }
        } else {
            if (product.quantity > 0) {
                cartItems.add(CartItem(product = product, quantity = 1))
                scanNotification = "Added ${product.name}"
            } else {
                scanNotification = "Out of stock: ${product.name}"
            }
        }
    }

    fun handleBarcodeScanned(barcode: String) {
        val foundProduct = activeProducts.firstOrNull { it.barcode.equals(barcode, ignoreCase = true) }
        if (foundProduct != null) {
            addProductToCart(foundProduct)
        } else {
            scanNotification = "Barcode $barcode not found in active inventory"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // POS Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Sell Items",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Text(
                    text = "Scan barcode or tap items to add to customer basket",
                    style = MaterialTheme.typography.bodySmall,
                    color = ImmersiveTextSecondary
                )
            }

            QuickTooltip(tooltipText = "Open Camera Barcode Scanner") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(OceanGradient)
                        .clickable { showScannerDialog = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("pos_scan_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan", fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                    }
                }
            }
        }

        // Notification Banner
        AnimatedVisibility(visible = scanNotification != null) {
            scanNotification?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CloudySkySoft,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CloudySky)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = OceanBlueDark
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Non-barcoded item dropdown & search bar section (Fruits, Bakery, Loose Produce)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Non-Barcoded Produce & Quick Items",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${nonBarcodedProducts.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = ImmersiveSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar for quick locating
                OutlinedTextField(
                    value = nonBarcodedSearchQuery,
                    onValueChange = { nonBarcodedSearchQuery = it },
                    placeholder = { Text("Search fruits, vegetables, loose items...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ImmersivePrimary) },
                    trailingIcon = {
                        if (nonBarcodedSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { nonBarcodedSearchQuery = "" }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filtered Chips of Non-barcoded items
                val filteredProduce = nonBarcodedProducts.filter {
                    it.name.contains(nonBarcodedSearchQuery, ignoreCase = true) ||
                            it.category.contains(nonBarcodedSearchQuery, ignoreCase = true)
                }

                if (filteredProduce.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredProduce) { product ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ImmersiveSurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                                modifier = Modifier
                                    .clickable { addProductToCart(product) }
                                    .testTag("produce_${product.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProductThumbnailView(
                                        category = product.category,
                                        imageUri = product.imageUri,
                                        size = 36.dp,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ImmersiveTextPrimary
                                        )
                                        Text(
                                            text = "$currencySymbol${String.format("%.2f", product.price)} • ${product.quantity} in stock",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = ImmersiveSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(ImmersivePrimary.copy(alpha = 0.18f))
                                            .border(1.dp, ImmersivePrimary.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = ImmersivePrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Cart Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cart Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ImmersivePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current Checkout Basket",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                    }

                    if (cartItems.isNotEmpty()) {
                        TextButton(
                            onClick = { cartItems.clear() },
                            modifier = Modifier.testTag("pos_clear_cart")
                        ) {
                            Text("Clear", color = AccentRose, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cart Items List
                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = ImmersiveTextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No items in cart",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ImmersiveTextSecondary
                            )
                            Text(
                                text = "Scan barcode or tap non-barcoded produce above",
                                style = MaterialTheme.typography.bodySmall,
                                color = ImmersiveTextSecondary.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cartItems) { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ImmersiveSurfaceCard,
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
                                            category = item.product.category,
                                            imageUri = item.product.imageUri,
                                            size = 38.dp,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                            Text(
                                                text = item.product.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ImmersiveTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "$currencySymbol${String.format("%.2f", item.product.price)} each",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ImmersiveTextSecondary
                                            )
                                        }
                                    }

                                    // Stepper (+ / -)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val idx = cartItems.indexOf(item)
                                                if (item.quantity > 1) {
                                                    cartItems[idx] = item.copy(quantity = item.quantity - 1)
                                                } else {
                                                    cartItems.removeAt(idx)
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (item.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                tint = if (item.quantity == 1) AccentRose else ImmersiveTextPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ImmersiveTextPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                val idx = cartItems.indexOf(item)
                                                if (item.quantity < item.product.quantity) {
                                                    cartItems[idx] = item.copy(quantity = item.quantity + 1)
                                                }
                                            },
                                            enabled = item.quantity < item.product.quantity,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = if (item.quantity < item.product.quantity) ImmersivePrimary else ImmersiveTextSecondary.copy(alpha = 0.3f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "$currencySymbol${String.format("%.2f", item.subtotal)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ImmersivePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Subtotal & Checkout Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = ImmersiveSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal ($totalItemsCount items)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ImmersiveTextSecondary
                            )
                            Text(
                                text = "$currencySymbol${String.format("%.2f", subtotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (cartItems.isNotEmpty()) OceanGradient else Brush.horizontalGradient(listOf(CloudySkySoft, CloudySkySoft)))
                                .clickable(enabled = cartItems.isNotEmpty()) { showCheckoutDialog = true }
                                .testTag("pos_checkout_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PointOfSale, contentDescription = null, tint = if (cartItems.isNotEmpty()) Color.White else ImmersiveTextMuted)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Complete Checkout • $currencySymbol${String.format("%.2f", subtotal)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (cartItems.isNotEmpty()) Color.White else ImmersiveTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    // Barcode Scanner Dialog
    if (showScannerDialog) {
        BarcodeScannerModal(
            availableProducts = activeProducts,
            onBarcodeScanned = { barcode ->
                handleBarcodeScanned(barcode)
            },
            onDismiss = { showScannerDialog = false }
        )
    }

    // Checkout Modal Dialog
    if (showCheckoutDialog) {
        CheckoutProcessDialog(
            totalAmount = subtotal,
            totalCost = totalCost,
            currencySymbol = currencySymbol,
            cartItems = cartItems.toList(),
            onConfirmCheckout = { paymentMethod, amountPaid, change ->
                val saleNumber = "#SALE-${(1050..9999).random()}"
                val sale = SaleEntity(
                    saleNumber = saleNumber,
                    totalAmount = subtotal,
                    costAmount = totalCost,
                    paymentMethod = paymentMethod,
                    amountPaid = amountPaid,
                    changeAmount = change,
                    timestamp = System.currentTimeMillis(),
                    itemCount = totalItemsCount
                )
                val saleItems = cartItems.map {
                    SaleItemEntity(
                        saleId = 0,
                        productId = it.product.id,
                        productName = it.product.name,
                        barcode = it.product.barcode,
                        category = it.product.category,
                        unitPrice = it.product.price,
                        costPrice = it.product.costPrice,
                        quantity = it.quantity,
                        subtotal = it.subtotal
                    )
                }

                // Process sale optimistically in database & UI
                onProcessSale(sale, saleItems)
                completedSaleReceipt = Pair(sale, saleItems)
                cartItems.clear()
                showCheckoutDialog = false
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }

    // Digital Receipt Dialog after sale completion
    completedSaleReceipt?.let { (sale, items) ->
        ReceiptDialog(
            sale = sale,
            items = items,
            currencySymbol = currencySymbol,
            onDismiss = { completedSaleReceipt = null }
        )
    }
}

/**
 * Checkout Process Dialog with Payment method & Tendered Cash math
 */
@Composable
fun CheckoutProcessDialog(
    totalAmount: Double,
    totalCost: Double,
    currencySymbol: String = "R",
    cartItems: List<CartItem>,
    onConfirmCheckout: (paymentMethod: String, amountPaid: Double, change: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("CASH") }
    var cashTenderedText by remember { mutableStateOf(String.format("%.2f", totalAmount)) }

    val cashTendered = cashTenderedText.toDoubleOrNull() ?: totalAmount
    val changeAmount = (cashTendered - totalAmount).coerceAtLeast(0.0)

    val quickCashOptions = remember(totalAmount) {
        listOf(
            totalAmount,
            20.0,
            50.0,
            100.0,
            200.0
        ).filter { it >= totalAmount || it == totalAmount }.distinct().take(4)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Process Sale Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Text(
                    text = "Select payment type and record receipt",
                    style = MaterialTheme.typography.bodySmall,
                    color = ImmersiveTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Total Amount Due Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CloudySkySoft,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CloudySky)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amount Due:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OceanBlueDark
                        )
                        Text(
                            text = "$currencySymbol${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlueDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ImmersiveTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val methods = listOf(
                        Triple("CASH", "Cash", Icons.Default.LocalAtm),
                        Triple("CARD", "Card", Icons.Default.CreditCard),
                        Triple("MOBILE_PAY", "Mobile", Icons.Default.PhoneAndroid)
                    )

                    methods.forEach { (key, label, icon) ->
                        val isSelected = selectedMethod == key
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) ImmersivePrimary else ImmersiveSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ImmersivePrimary else ImmersiveBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = key }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) ImmersivePrimaryContainer else ImmersiveTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ImmersivePrimaryContainer else ImmersiveTextPrimary
                                )
                            }
                        }
                    }
                }

                if (selectedMethod == "CASH") {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Quick Cash Tendered:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ImmersiveTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickCashOptions.forEach { amount ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ImmersiveSurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { cashTenderedText = String.format("%.2f", amount) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$currencySymbol${String.format("%.0f", amount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ImmersiveSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cashTenderedText,
                        onValueChange = { cashTenderedText = it },
                        label = { Text("Tendered Cash ($currencySymbol)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Change Due:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "$currencySymbol${String.format("%.2f", changeAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selectedMethod != "CASH" || cashTendered >= totalAmount) OceanGradient else Brush.horizontalGradient(listOf(CloudySkySoft, CloudySkySoft)))
                            .clickable(enabled = selectedMethod != "CASH" || cashTendered >= totalAmount) {
                                val paid = if (selectedMethod == "CASH") cashTendered else totalAmount
                                val change = if (selectedMethod == "CASH") changeAmount else 0.0
                                onConfirmCheckout(selectedMethod, paid, change)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (selectedMethod != "CASH" || cashTendered >= totalAmount) Color.White else ImmersiveTextMuted)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm Sale", fontWeight = FontWeight.Bold, color = if (selectedMethod != "CASH" || cashTendered >= totalAmount) Color.White else ImmersiveTextMuted)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Receipt Dialog showing immediate post-sale confirmation & details
 */
@Composable
fun ReceiptDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    currencySymbol: String = "R",
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderMedium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(OceanGradient)
                        .border(1.dp, CloudySky, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Sale Captured Successfully!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Text(
                    text = "Receipt ${sale.saleNumber} • Inventory updated",
                    style = MaterialTheme.typography.bodySmall,
                    color = ImmersiveTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ImmersiveSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderMedium)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.productName.take(18)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ImmersiveTextPrimary
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", item.subtotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ImmersiveTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = ImmersiveBorderMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Paid (${sale.paymentMethod}):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )
                            Text(
                                text = "$currencySymbol${String.format("%.2f", sale.totalAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = OceanBlueDark
                            )
                        }

                        if (sale.changeAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Change Given:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OceanBlue
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", sale.changeAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(OceanGradient)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Done / Next Customer", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}
