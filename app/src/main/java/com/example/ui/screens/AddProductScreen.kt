package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.ProductThumbnailView
import com.example.ui.components.ProductVisualRegistry
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.CloudySky
import com.example.ui.theme.CloudySkySoft
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderMedium
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBlueDeep
import com.example.ui.theme.OceanBlueLight
import com.example.ui.theme.OceanGradient
import com.example.ui.theme.OceanSkyGradient
import com.example.ui.theme.SkyPillGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    currencySymbol: String = "R",
    existingProducts: List<ProductEntity>,
    onProductAdded: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Snacks") }
    var sellingPriceText by remember { mutableStateOf("") }
    var costPriceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    var isBulk by remember { mutableStateOf(false) }
    var bulkBoxesText by remember { mutableStateOf("1") }
    var bulkPackSizeText by remember { mutableStateOf("24") }
    var bulkBoxCostText by remember { mutableStateOf("") }
    var isNonBarcoded by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("unit") }

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var successBanner by remember { mutableStateOf<String?>(null) }

    // Android 13+ standard Photo Picker (Zero permissions needed)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri.toString()
            }
        }
    )

    val categories = listOf(
        "Snacks",
        "Beverages",
        "Fresh Produce",
        "Bakery",
        "Dairy",
        "Staples",
        "Household",
        "Confectionery",
        "General"
    )

    // Calculate bulk quantity & unit cost automatically when bulk option is enabled
    val bulkPackSize = bulkPackSizeText.toIntOrNull() ?: 24
    val bulkBoxes = bulkBoxesText.toIntOrNull() ?: 1
    val calculatedBulkTotalQty = bulkBoxes * bulkPackSize
    val bulkBoxCost = bulkBoxCostText.toDoubleOrNull() ?: 0.0
    val calculatedUnitCost = if (bulkPackSize > 0 && bulkBoxCost > 0) bulkBoxCost / bulkPackSize else null

    fun resetForm() {
        name = ""
        barcode = ""
        category = "Snacks"
        sellingPriceText = ""
        costPriceText = ""
        quantityText = "1"
        selectedImageUri = null
        isBulk = false
        bulkBoxesText = "1"
        bulkPackSizeText = "24"
        bulkBoxCostText = ""
        isNonBarcoded = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ImmersiveBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Heading (Simple Everyday Wording)
        Column {
            Text(
                text = "Add New Item",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = ImmersiveTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Enter details, take a photo, and set your selling price.",
                style = MaterialTheme.typography.bodyMedium,
                color = ImmersiveTextSecondary
            )
        }

        // Success Confirmation Banner
        AnimatedVisibility(visible = successBanner != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CloudySkySoft,
                border = androidx.compose.foundation.BorderStroke(1.dp, CloudySky),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = OceanBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = successBanner ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlueDark
                    )
                }
            }
        }

        // ==========================================
        // 1. PRODUCT PICTURE SECTION
        // ==========================================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Item Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview box of selected image or preset
                    ProductThumbnailView(
                        category = category,
                        imageUri = selectedImageUri,
                        size = 72.dp,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CloudySkySoft,
                                contentColor = OceanBlueDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CloudySky),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_product_choose_photo_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedImageUri?.startsWith("content:") == true) "Change Photo" else "Choose Photo",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (selectedImageUri != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentRose)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset to default icon", color = AccentRose, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Or tap a quick preset picture:",
                    style = MaterialTheme.typography.labelSmall,
                    color = ImmersiveTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal scrollable row of preset visuals
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductVisualRegistry.presets.forEach { preset ->
                        val isSelected = selectedImageUri == preset.key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CloudySkySoft else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) OceanBlue else ImmersiveBorder
                            ),
                            modifier = Modifier.clickable {
                                selectedImageUri = preset.key
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ProductThumbnailView(
                                    category = "",
                                    imageUri = preset.key,
                                    size = 26.dp,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                Text(
                                    text = preset.title.split("&").first().trim(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) OceanBlueDark else ImmersiveTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. ITEM BASIC INFORMATION
        // ==========================================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Item Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    placeholder = { Text("e.g. White Bread 700g or Simba Chips") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_input_name"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Barcode Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode Number") },
                            placeholder = { Text("Scan or type number") },
                            enabled = !isNonBarcoded,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_product_input_barcode"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isNonBarcoded) OceanGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(CloudySky, CloudySky)))
                                .clickable(enabled = !isNonBarcoded) { showScannerDialog = true }
                                .padding(horizontal = 16.dp)
                                .testTag("add_product_scan_barcode_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // No barcode checkbox (e.g., loose fruits, bread, eggs)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isNonBarcoded = !isNonBarcoded
                            if (isNonBarcoded) {
                                barcode = "NB-${name.take(4).uppercase().ifEmpty { "ITEM" }}-${(100..999).random()}"
                            } else {
                                barcode = ""
                            }
                        }
                    ) {
                        Checkbox(
                            checked = isNonBarcoded,
                            onCheckedChange = { checked ->
                                isNonBarcoded = checked
                                if (checked) {
                                    barcode = "NB-${name.take(4).uppercase().ifEmpty { "ITEM" }}-${(100..999).random()}"
                                } else {
                                    barcode = ""
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = OceanBlue)
                        )
                        Text(
                            text = "This item has no barcode (like loose fruits, bread, or eggs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ImmersiveTextSecondary
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. PRICING & PROFIT HELPER
        // ==========================================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Prices & Profit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selling Price (What customer pays)
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Selling Price ($currencySymbol)") },
                        placeholder = { Text("e.g. 18.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_product_input_selling_price"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Cost Price (What you paid supplier)
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Cost Price ($currencySymbol)") },
                        placeholder = { Text("e.g. 13.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_product_input_cost_price"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Friendly Profit Calculator Banner
                val sellP = sellingPriceText.toDoubleOrNull() ?: 0.0
                val costP = costPriceText.toDoubleOrNull() ?: 0.0
                if (sellP > 0) {
                    val profitPerItem = sellP - costP
                    val marginPercent = (profitPerItem / sellP) * 100.0

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (profitPerItem >= 0) CloudySkySoft else Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (profitPerItem >= 0) CloudySky else Color(0xFFFECACA)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (profitPerItem >= 0) "Your Profit Per Item" else "Selling at a Loss",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (profitPerItem >= 0) OceanBlueDark else AccentRose,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", profitPerItem)} per item",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (profitPerItem >= 0) OceanBlue else AccentRose
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (profitPerItem >= 0) CloudySky else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", marginPercent)}% profit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (profitPerItem >= 0) OceanBlueDark else AccentRose,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Initial Quantity on Shelf
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("How Many Put on Shelf?") },
                    placeholder = { Text("e.g. 24") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_input_quantity"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // ==========================================
        // 4. BULK WHOLESALE BOX CALCULATOR
        // ==========================================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isBulk = !isBulk }
                ) {
                    Checkbox(
                        checked = isBulk,
                        onCheckedChange = { isBulk = it },
                        colors = CheckboxDefaults.colors(checkedColor = OceanBlue)
                    )
                    Column {
                        Text(
                            text = "Did you buy this in a whole box or crate?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                        Text(
                            text = "We will calculate cost per single item automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ImmersiveTextSecondary
                        )
                    }
                }

                AnimatedVisibility(visible = isBulk) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = bulkBoxesText,
                                onValueChange = { bulkBoxesText = it },
                                label = { Text("Boxes Bought") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = bulkPackSizeText,
                                onValueChange = { bulkPackSizeText = it },
                                label = { Text("Items in Box") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = bulkBoxCostText,
                            onValueChange = {
                                bulkBoxCostText = it
                                val costVal = it.toDoubleOrNull()
                                val packVal = bulkPackSizeText.toIntOrNull() ?: 24
                                if (costVal != null && packVal > 0) {
                                    costPriceText = String.format("%.2f", costVal / packVal)
                                }
                            },
                            label = { Text("Cost Per Box ($currencySymbol)") },
                            placeholder = { Text("e.g. 240.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CloudySkySoft,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CloudySky),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Total items to add: $calculatedBulkTotalQty units" +
                                        (if (calculatedUnitCost != null) " (Cost: $currencySymbol${String.format("%.2f", calculatedUnitCost)} each)" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = OceanBlueDark,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. SAVE ITEM ACTION BUTTON
        // ==========================================
        val isFormValid = name.isNotBlank() &&
                (sellingPriceText.toDoubleOrNull() ?: 0.0) > 0

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isFormValid) OceanGradient
                    else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFCBDDE9), Color(0xFFCBDDE9)))
                )
                .clickable(enabled = isFormValid) {
                    val price = sellingPriceText.toDoubleOrNull() ?: 0.0
                    val cost = costPriceText.toDoubleOrNull() ?: 0.0
                    val qty = if (isBulk) calculatedBulkTotalQty else (quantityText.toIntOrNull() ?: 1)

                    val newProduct = ProductEntity(
                        name = name.trim(),
                        barcode = barcode.trim().ifEmpty { "NB-${name.take(4).uppercase()}-${(100..999).random()}" },
                        category = category,
                        price = price,
                        costPrice = cost,
                        quantity = qty,
                        isBulk = isBulk,
                        bulkPackSize = bulkPackSize,
                        bulkCostPrice = bulkBoxCost,
                        isOnSale = true,
                        isNonBarcoded = isNonBarcoded,
                        unit = unit,
                        imageUri = selectedImageUri,
                        updatedAt = System.currentTimeMillis()
                    )

                    onProductAdded(newProduct)
                    successBanner = "Successfully added '${newProduct.name}' to shelf!"
                    resetForm()
                }
                .testTag("add_product_submit_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isFormValid) Color.White else OceanBlueDark.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Item to Shop",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isFormValid) Color.White else OceanBlueDark.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Barcode Scanner Camera Modal
    if (showScannerDialog) {
        BarcodeScannerModal(
            availableProducts = existingProducts,
            onBarcodeScanned = { detectedBarcode ->
                barcode = detectedBarcode
                showScannerDialog = false
            },
            onDismiss = { showScannerDialog = false }
        )
    }
}
