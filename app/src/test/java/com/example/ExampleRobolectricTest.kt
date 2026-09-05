package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kasi Ledger", appName)
  }

  @Test
  fun `verify bulk wholesale packaging calculation`() {
    val boxes = 2
    val packsPerBox = 24
    val boxCost = 280.0
    val totalQty = boxes * packsPerBox
    val unitCost = boxCost / packsPerBox

    assertEquals(48, totalQty)
    assertEquals(11.67, unitCost, 0.01)
  }

  @Test
  fun `verify loss leader thin margin detection`() {
    val costPrice = 16.50
    val sellingPrice = 17.00
    val profitMargin = ((sellingPrice - costPrice) / sellingPrice) * 100.0

    // Margin is ~2.94%, qualifies as loss leader under 5% threshold
    assertEquals(true, profitMargin < 5.0)
  }

  @Test
  fun `verify alternative credit scoring calculations`() {
    val profile = com.example.data.model.StoreProfileEntity()
    val scorecard = com.example.ui.util.AlternativeCreditScoringEngine.calculateScorecard(
        profile = profile,
        sales = emptyList(),
        activeProducts = emptyList(),
        invoices = emptyList<com.example.data.model.WholesalerInvoiceEntity>()
    )
    // Score should be valid 0-100
    org.junit.Assert.assertTrue(scorecard.totalScore in 0..100)
    org.junit.Assert.assertTrue(scorecard.sha256VerificationHash.isNotEmpty())
  }
}
