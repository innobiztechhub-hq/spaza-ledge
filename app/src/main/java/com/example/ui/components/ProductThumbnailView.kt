package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

data class PresetProductVisual(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val bgColors: List<Color>,
    val iconTint: Color,
    val drawableRes: Int? = null,
    val outsourcedUrl: String? = null
)

object ProductVisualRegistry {
    val presets = listOf(
        PresetProductVisual(
            key = "preset:chips",
            title = "Chips & Snacks",
            icon = Icons.Default.Fastfood,
            bgColors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)),
            iconTint = Color(0xFFD97706),
            drawableRes = R.drawable.img_product_chips,
            outsourcedUrl = null
        ),
        PresetProductVisual(
            key = "preset:bread",
            title = "Bread & Bakery",
            icon = Icons.Default.BakeryDining,
            bgColors = listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)),
            iconTint = Color(0xFFC2410C),
            drawableRes = R.drawable.img_product_bread,
            outsourcedUrl = null
        ),
        PresetProductVisual(
            key = "preset:coke",
            title = "Soft Drinks & Soda",
            icon = Icons.Default.LocalDrink,
            bgColors = listOf(Color(0xFFFEE2E2), Color(0xFFFECACA)),
            iconTint = Color(0xFFDC2626),
            drawableRes = R.drawable.img_product_coke,
            outsourcedUrl = null
        ),
        PresetProductVisual(
            key = "preset:energy",
            title = "Energy Drinks",
            icon = Icons.Default.Bolt,
            bgColors = listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE)),
            iconTint = Color(0xFF4338CA),
            drawableRes = null,
            outsourcedUrl = "https://images.unsplash.com/photo-1622543925917-763c34d1a86e?auto=format&fit=crop&w=300&q=80"
        ),
        PresetProductVisual(
            key = "preset:milk",
            title = "Milk & Dairy",
            icon = Icons.Default.LocalCafe,
            bgColors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
            iconTint = Color(0xFF0284C7),
            drawableRes = R.drawable.img_product_milk,
            outsourcedUrl = null
        ),
        PresetProductVisual(
            key = "preset:banana",
            title = "Fresh Bananas",
            icon = Icons.Default.BreakfastDining,
            bgColors = listOf(Color(0xFFFEF9C3), Color(0xFFFEF08A)),
            iconTint = Color(0xFFCA8A04),
            drawableRes = R.drawable.img_product_banana,
            outsourcedUrl = null
        ),
        PresetProductVisual(
            key = "preset:apple",
            title = "Fresh Apples",
            icon = Icons.Default.Spa,
            bgColors = listOf(Color(0xFFDCFCE7), Color(0xFFBBF7D0)),
            iconTint = Color(0xFF16A34A),
            drawableRes = null,
            outsourcedUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=300&q=80"
        ),
        PresetProductVisual(
            key = "preset:maize",
            title = "Maize & Grains",
            icon = Icons.Default.Grass,
            bgColors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)),
            iconTint = Color(0xFFB45309),
            drawableRes = null,
            outsourcedUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=300&q=80"
        ),
        PresetProductVisual(
            key = "preset:soap",
            title = "Household & Soap",
            icon = Icons.Default.CleaningServices,
            bgColors = listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4)),
            iconTint = Color(0xFF0D9488),
            drawableRes = null,
            outsourcedUrl = "https://images.unsplash.com/photo-1583947215259-38e31be8751f?auto=format&fit=crop&w=300&q=80"
        ),
        PresetProductVisual(
            key = "preset:candy",
            title = "Sweets & Candies",
            icon = Icons.Default.Cookie,
            bgColors = listOf(Color(0xFFFCE7F3), Color(0xFFFBCFE8)),
            iconTint = Color(0xFFDB2777),
            drawableRes = null,
            outsourcedUrl = "https://images.unsplash.com/photo-1582058091505-f87a2e55a40f?auto=format&fit=crop&w=300&q=80"
        ),
        PresetProductVisual(
            key = "preset:beanie",
            title = "General & Clothing",
            icon = Icons.Default.Checkroom,
            bgColors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
            iconTint = Color(0xFF475569),
            drawableRes = null,
            outsourcedUrl = null
        )
    )

    fun getVisualForProduct(category: String, imageUri: String?): PresetProductVisual {
        if (!imageUri.isNullOrBlank() && imageUri.startsWith("preset:")) {
            presets.find { it.key == imageUri }?.let { return it }
        }
        // Match by resource name
        if (!imageUri.isNullOrBlank() && imageUri.startsWith("res:")) {
            when (imageUri.removePrefix("res:")) {
                "img_product_chips" -> return presets[0]
                "img_product_bread" -> return presets[1]
                "img_product_coke" -> return presets[2]
                "img_product_milk" -> return presets[4]
                "img_product_banana" -> return presets[5]
            }
        }
        // Fallback by category
        val lowerCat = category.lowercase()
        return when {
            "snack" in lowerCat || "chip" in lowerCat -> presets[0]
            "bake" in lowerCat || "bread" in lowerCat -> presets[1]
            "bever" in lowerCat || "drink" in lowerCat || "soda" in lowerCat -> presets[2]
            "dair" in lowerCat || "milk" in lowerCat -> presets[4]
            "produce" in lowerCat || "fruit" in lowerCat || "banana" in lowerCat -> presets[5]
            "apple" in lowerCat -> presets[6]
            "staple" in lowerCat || "maize" in lowerCat || "grain" in lowerCat -> presets[7]
            "house" in lowerCat || "clean" in lowerCat || "soap" in lowerCat -> presets[8]
            "confec" in lowerCat || "candy" in lowerCat || "sweet" in lowerCat -> presets[9]
            else -> presets[10]
        }
    }
}

@Composable
fun ProductThumbnailView(
    category: String,
    imageUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val visual = ProductVisualRegistry.getVisualForProduct(category, imageUri)

    // Resolve local drawable resource ID if available
    val localDrawableRes: Int? = when {
        !imageUri.isNullOrBlank() && imageUri.startsWith("res:") -> {
            when (imageUri.removePrefix("res:")) {
                "img_product_chips" -> R.drawable.img_product_chips
                "img_product_bread" -> R.drawable.img_product_bread
                "img_product_coke" -> R.drawable.img_product_coke
                "img_product_banana" -> R.drawable.img_product_banana
                "img_product_milk" -> R.drawable.img_product_milk
                else -> visual.drawableRes
            }
        }
        visual.drawableRes != null -> visual.drawableRes
        else -> null
    }

    // Remote URL or user-chosen content/file URI
    val remoteOrContentModel: Any? = when {
        !imageUri.isNullOrBlank() && (imageUri.startsWith("content://") || imageUri.startsWith("file://")) -> {
            Uri.parse(imageUri)
        }
        !imageUri.isNullOrBlank() && (imageUri.startsWith("http://") || imageUri.startsWith("https://")) -> {
            imageUri
        }
        !visual.outsourcedUrl.isNullOrBlank() -> visual.outsourcedUrl
        else -> null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), shape)
            .background(Brush.linearGradient(visual.bgColors)),
        contentAlignment = Alignment.Center
    ) {
        when {
            localDrawableRes != null -> {
                Image(
                    painter = painterResource(id = localDrawableRes),
                    contentDescription = visual.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(shape)
                )
            }
            remoteOrContentModel != null -> {
                var loadFailed by remember(remoteOrContentModel) { mutableStateOf(false) }
                if (!loadFailed) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(remoteOrContentModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = visual.title,
                        contentScale = ContentScale.Crop,
                        onError = { loadFailed = true },
                        modifier = Modifier
                            .size(size)
                            .clip(shape)
                    )
                } else {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = visual.title,
                        tint = visual.iconTint,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
            else -> {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = visual.title,
                    tint = visual.iconTint,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    }
}
