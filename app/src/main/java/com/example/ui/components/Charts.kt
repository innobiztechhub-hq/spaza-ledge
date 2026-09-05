package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.CategoryStat
import com.example.ui.theme.AccentRose
import com.example.ui.theme.CloudySky
import com.example.ui.theme.CloudySkySoft
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderMedium
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersivePrimaryContainer
import com.example.ui.theme.ImmersiveSecondary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBlueDeep
import com.example.ui.theme.OceanBlueLight

data class SalesDataPoint(
    val label: String,
    val amount: Double,
    val count: Int = 1
)

/**
 * Interactive Sales Trend Bar & Line Chart
 */
@Composable
fun SalesTrendChart(
    dataPoints: List<SalesDataPoint>,
    currencySymbol: String = "R",
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No sales recorded for this period yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedPoint by remember { mutableStateOf<SalesDataPoint?>(dataPoints.lastOrNull()) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
        selectedPoint = dataPoints.lastOrNull()
    }

    val maxAmount = remember(dataPoints) {
        (dataPoints.maxOfOrNull { it.amount } ?: 100.0).coerceAtLeast(50.0) * 1.15
    }

    val primaryColor = OceanBlue
    val cyanColor = CloudySky
    val gridLineColor = ImmersiveBorder

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected amount highlight banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = selectedPoint?.label ?: "Overview",
                    style = MaterialTheme.typography.labelSmall,
                    color = ImmersiveTextSecondary
                )
                Text(
                    text = "$currencySymbol${String.format("%.2f", selectedPoint?.amount ?: 0.0)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlueDark
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CloudySkySoft)
                    .border(1.dp, CloudySky, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${selectedPoint?.count ?: 0} transactions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = OceanBlueDark
                )
            }
        }

        // Canvas Bar & Area Chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val width = size.width
            val height = size.height
            val bottomPadding = 24.dp.toPx()
            val chartHeight = height - bottomPadding

            // Draw horizontal guide lines
            val steps = 3
            for (i in 0..steps) {
                val y = chartHeight * (1f - i.toFloat() / steps)
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            val barCount = dataPoints.size
            val slotWidth = width / barCount
            val barWidth = (slotWidth * 0.45f).coerceIn(12f, 36f)

            // Draw animated bars
            dataPoints.forEachIndexed { index, point ->
                val xCenter = slotWidth * index + slotWidth / 2f
                val ratio = (point.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                val barH = chartHeight * ratio * animProgress.value
                val isSelected = selectedPoint?.label == point.label

                val barBrush = if (isSelected) {
                    Brush.verticalGradient(
                        listOf(cyanColor, primaryColor)
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(primaryColor.copy(alpha = 0.7f), primaryColor.copy(alpha = 0.3f))
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(xCenter - barWidth / 2f, chartHeight - barH),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Selection dot on top
                if (isSelected && barH > 8f) {
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(xCenter, chartHeight - barH)
                    )
                    drawCircle(
                        color = cyanColor,
                        radius = 2.dp.toPx(),
                        center = Offset(xCenter, chartHeight - barH)
                    )
                }
            }
        }

        // Labels underneath
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dataPoints.forEach { point ->
                val isSelected = selectedPoint?.label == point.label
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) OceanBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { selectedPoint = point }
                )
            }
        }
    }
}

/**
 * Category Breakdown with proportional visual distribution
 */
@Composable
fun CategoryDistributionCard(
    categoryStats: List<CategoryStat>,
    currencySymbol: String = "R",
    modifier: Modifier = Modifier
) {
    val totalRevenue = categoryStats.sumOf { it.totalRevenue }.coerceAtLeast(1.0)
    val colorPalette = listOf(
        OceanBlue,
        OceanBlueLight,
        OceanBlueDeep,
        CloudySky,
        OceanBlueDark,
        Color(0xFF68A3C7),
        Color(0xFFB0CDDF)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sales by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Text(
                    text = "$currencySymbol${String.format("%.2f", totalRevenue)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OceanBlueDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Proportional Segmented Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                categoryStats.forEachIndexed { index, stat ->
                    val weight = (stat.totalRevenue / totalRevenue).toFloat().coerceAtLeast(0.01f)
                    val color = colorPalette[index % colorPalette.size]
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(14.dp)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Breakdown list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryStats.take(5).forEachIndexed { index, stat ->
                    val percentage = (stat.totalRevenue / totalRevenue * 100.0)
                    val color = colorPalette[index % colorPalette.size]

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stat.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${String.format("%.1f", percentage)}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$currencySymbol${String.format("%.2f", stat.totalRevenue)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Inventory Turnover Health & Velocity Gauge
 */
@Composable
fun InventoryTurnoverCard(
    turnoverRate: Double, // Times per month/period
    inventoryWorth: Double,
    costOfGoodsSold: Double,
    currencySymbol: String = "R",
    modifier: Modifier = Modifier
) {
    val healthStatus = when {
        turnoverRate >= 3.0 -> Pair("Fast Turnover (High Liquidity)", OceanBlue)
        turnoverRate >= 1.5 -> Pair("Optimal Turnover (Healthy)", OceanBlueLight)
        else -> Pair("Slow Velocity (Stagnant Capital)", AccentRose)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory Turnover",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(healthStatus.second.copy(alpha = 0.15f))
                        .border(1.dp, healthStatus.second.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = healthStatus.first,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = healthStatus.second
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${String.format("%.2f", turnoverRate)}x",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Stock rotation velocity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Cost of Sales: $currencySymbol${String.format("%.2f", costOfGoodsSold)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Asset Value: $currencySymbol${String.format("%.2f", inventoryWorth)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
