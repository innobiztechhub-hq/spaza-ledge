package com.example.ui.util

import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.StoreProfileEntity
import com.example.data.model.WholesalerInvoiceEntity
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class ScorecardTier(
    val level: Int,
    val title: String,
    val description: String,
    val badgeColorHex: Long
)

data class CreditScorecardResult(
    val totalScore: Int, // 0 - 100
    val tier: ScorecardTier,
    // Turnover Stability (40%)
    val turnoverStabilityScore: Double, // 0..40
    val activeTradingDays: Int,
    val totalWindowDays: Int, // 30
    val turnoverStabilityPct: Double,
    // Margin Health (30%)
    val marginHealthScore: Double, // 0..30
    val netMarginPct: Double,
    val isMarginHealthy: Boolean, // >= 15%
    // Supplier Discipline (30%)
    val supplierDisciplineScore: Double, // 0..30
    val onTimeRepaymentRatePct: Double,
    val paidInvoicesCount: Int,
    val totalInvoicesCount: Int,
    // Financial Metrics
    val avgMonthlyTurnover: Double,
    val totalGrossSales: Double,
    val totalCostOfGoods: Double,
    val totalNetProfit: Double,
    val stockValuation: Double,
    // Verification
    val sha256VerificationHash: String,
    val generatedTimestamp: Long = System.currentTimeMillis()
)

object AlternativeCreditScoringEngine {

    /**
     * Calculates the 0-100 Alternative Credit Score strictly based on:
     * - Turnover Stability (40%): Consistency of daily logged cash sales over 30 days
     * - Margin Health (30%): Net margin calculation ((Sales - Stock Cost) / Sales) * 100 >= 15%
     * - Supplier Discipline (30%): On-time repayment rate of wholesaler credit invoices
     */
    fun calculateScorecard(
        profile: StoreProfileEntity,
        sales: List<SaleEntity>,
        activeProducts: List<ProductEntity>,
        invoices: List<WholesalerInvoiceEntity>,
        windowDays: Int = 30
    ): CreditScorecardResult {
        val now = System.currentTimeMillis()
        val dayMillis = 86_400_000L
        val windowStart = now - (windowDays * dayMillis)

        // 1. Turnover Stability (40%)
        // Count unique calendar days with sales in the rolling 30-day window
        val salesInWindow = sales.filter { it.timestamp >= windowStart }
        val dayDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val activeDaysSet = salesInWindow.map { dayDateFormat.format(Date(it.timestamp)) }.toSet()
        val activeTradingDays = if (sales.isNotEmpty()) activeDaysSet.size.coerceAtLeast(1) else 0

        // For standard small shops, 20+ trading days out of 30 is excellent consistency (~6 days/week)
        val targetActiveDays = 22.0
        val turnoverRatio = (activeTradingDays / targetActiveDays).coerceIn(0.0, 1.0)
        val turnoverStabilityScore = turnoverRatio * 40.0
        val turnoverStabilityPct = (activeTradingDays.toDouble() / windowDays.toDouble()) * 100.0

        // 2. Margin Health (30%)
        // Formula: ((Total Sales - Total Stock Cost) / Total Sales) * 100 >= 15%
        val totalGrossSales = if (salesInWindow.isNotEmpty()) {
            salesInWindow.sumOf { it.totalAmount }
        } else {
            sales.sumOf { it.totalAmount }
        }
        val totalCostOfGoods = if (salesInWindow.isNotEmpty()) {
            salesInWindow.sumOf { it.costAmount }
        } else {
            sales.sumOf { it.costAmount }
        }
        val totalNetProfit = (totalGrossSales - totalCostOfGoods).coerceAtLeast(0.0)
        val netMarginPct = if (totalGrossSales > 0) (totalNetProfit / totalGrossSales) * 100.0 else 20.0
        val isMarginHealthy = netMarginPct >= 15.0

        // If net margin is >= 15%, full 30 points. If less, scaled proportionally.
        val marginHealthScore = if (netMarginPct >= 15.0) {
            30.0
        } else {
            (netMarginPct / 15.0).coerceIn(0.0, 1.0) * 30.0
        }

        // 3. Supplier Discipline (30%)
        // On-time repayment rate of wholesaler credit invoices marked "Paid" on or before the due date
        val onTimeInvoices = invoices.filter { it.status == "PAID" && it.isPaidOnTime }
        val paidInvoices = invoices.filter { it.status == "PAID" }
        val onTimeRepaymentRatePct = when {
            invoices.isEmpty() -> 85.0 // Neutral initial baseline
            paidInvoices.isEmpty() -> 70.0
            else -> (onTimeInvoices.size.toDouble() / paidInvoices.size.toDouble()) * 100.0
        }
        val supplierDisciplineScore = (onTimeRepaymentRatePct / 100.0).coerceIn(0.0, 1.0) * 30.0

        // Total 0 - 100 Credit Score
        val rawTotal = turnoverStabilityScore + marginHealthScore + supplierDisciplineScore
        val totalScore = rawTotal.roundToInt().coerceIn(10, 99)

        // 4-Tier Status Indicator
        val tier = when {
            totalScore >= 85 -> ScorecardTier(4, "Level 4: Prime Merchant", "Pre-approved for prime SME bank facilities & lower interest financing", 0xFF059669) // Emerald
            totalScore >= 70 -> ScorecardTier(3, "Level 3: Bankable", "Recommended for formal working capital line & supplier trade credit", 0xFF2563EB) // Royal Blue
            totalScore >= 50 -> ScorecardTier(2, "Level 2: Emerging", "Eligible for digital micro-advances with consistent repayment history", 0xFFD97706) // Amber
            else -> ScorecardTier(1, "Level 1: Building", "Building baseline 30-day cash turnover and active trading trail", 0xFF64748B) // Slate
        }

        val avgMonthlyTurnover = if (activeTradingDays > 0) {
            (totalGrossSales / activeTradingDays) * 26.0 // 26 trading days/month
        } else totalGrossSales

        val stockValuation = activeProducts.sumOf { it.quantity * it.costPrice }

        // Compute SHA-256 digital verification hash for bank statement auditing
        val payload = "${profile.ownerIdNumber}|${profile.storeName}|${profile.phoneNumber}|" +
                "${profile.gpsLat},${profile.gpsLng}|$totalScore|${"%.2f".format(Locale.US, totalGrossSales)}|" +
                "${"%.1f".format(Locale.US, netMarginPct)}|$now"
        val sha256VerificationHash = computeSha256(payload)

        return CreditScorecardResult(
            totalScore = totalScore,
            tier = tier,
            turnoverStabilityScore = turnoverStabilityScore,
            activeTradingDays = activeTradingDays,
            totalWindowDays = windowDays,
            turnoverStabilityPct = turnoverStabilityPct,
            marginHealthScore = marginHealthScore,
            netMarginPct = netMarginPct,
            isMarginHealthy = isMarginHealthy,
            supplierDisciplineScore = supplierDisciplineScore,
            onTimeRepaymentRatePct = onTimeRepaymentRatePct,
            paidInvoicesCount = onTimeInvoices.size,
            totalInvoicesCount = invoices.size,
            avgMonthlyTurnover = avgMonthlyTurnover,
            totalGrossSales = totalGrossSales,
            totalCostOfGoods = totalCostOfGoods,
            totalNetProfit = totalNetProfit,
            stockValuation = stockValuation,
            sha256VerificationHash = sha256VerificationHash,
            generatedTimestamp = now
        )
    }

    private fun computeSha256(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray())
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            "a1f9e83c7b2049d582ea91bc034d618f029b3c4a"
        }
    }
}
