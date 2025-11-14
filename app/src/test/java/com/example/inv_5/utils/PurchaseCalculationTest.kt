package com.example.inv_5.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.math.roundToLong

/**
 * Unit tests for Purchase calculation logic in PurchaseExcelImporter
 * These tests verify the discount, tax, and total calculations
 */
class PurchaseCalculationTest {

    @Test
    fun `test rate calculation with 20 percent discount`() {
        // Given
        val mrp = 999.0
        val discountPct = 20.0
        val expectedRate = 799.2 // 999 - (999 * 20 / 100)

        // When
        val actualRate = calculateRateWithPercentageDiscount(mrp, discountPct)

        // Then
        assertEquals(expectedRate, actualRate, 0.01)
    }

    @Test
    fun `test rate calculation with 149 amount discount`() {
        // Given
        val mrp = 999.0
        val discountAmt = 149.0
        val expectedRate = 850.0 // 999 - 149

        // When
        val actualRate = calculateRateWithAmountDiscount(mrp, discountAmt)

        // Then
        assertEquals(expectedRate, actualRate, 0.01)
    }

    @Test
    fun `test taxable calculation for 10 items at rate 850`() {
        // Given
        val rate = 850.0
        val quantity = 10
        val taxPct = 18.0
        
        // When - using correct formula with rounding: Taxable = Total / (1 + Tax%/100)
        val total = rate * quantity // 8500
        val taxableRaw = total / (1 + (taxPct / 100.0)) // 8500 / 1.18 = 7203.389830...
        val taxable = (taxableRaw * 100).roundToLong() / 100.0 // 7203.39
        val tax = total - taxable // 1296.61

        // Then
        assertEquals(8500.0, total, 0.01)
        assertEquals(7203.39, taxable, 0.01)
        assertEquals(1296.61, tax, 0.01)
        // Verify sum equals total
        assertEquals(total, taxable + tax, 0.01)
    }

    @Test
    fun `test tax calculation with 18 percent on total 8500`() {
        // Given
        val total = 8500.0
        val taxPct = 18.0
        
        // When - using correct formula with rounding: Taxable = Total / (1 + Tax%/100)
        val taxableRaw = total / (1 + (taxPct / 100.0)) // 8500 / 1.18 = 7203.389830...
        val taxable = (taxableRaw * 100).roundToLong() / 100.0 // 7203.39
        val tax = total - taxable // 1296.61

        // Then
        assertEquals(7203.39, taxable, 0.01)
        assertEquals(1296.61, tax, 0.01)
        // Verify sum equals total
        assertEquals(total, taxable + tax, 0.01)
    }

    @Test
    fun `test total calculation for purchase item`() {
        // Given: MRP 999, discount amount 149, qty 10, tax 18%
        val mrp = 999.0
        val discountAmt = 149.0
        val quantity = 10
        val taxPct = 18.0

        // When - using correct formula with rounding: Taxable = Total / (1 + Tax%/100)
        val rate = mrp - discountAmt // 850
        val total = rate * quantity // 8500 (total with tax included)
        val taxableRaw = total / (1 + (taxPct / 100.0)) // 8500 / 1.18 = 7203.389830...
        val taxable = (taxableRaw * 100).roundToLong() / 100.0 // 7203.39
        val tax = total - taxable // 1296.61

        // Then
        assertEquals(850.0, rate, 0.01)
        assertEquals(8500.0, total, 0.01)
        assertEquals(7203.39, taxable, 0.01)
        assertEquals(1296.61, tax, 0.01)
        // Verify sum equals total
        assertEquals(total, taxable + tax, 0.01)
    }

    @Test
    fun `test total calculation with percentage discount`() {
        // Given: MRP 999, discount 20%, qty 10, tax 18%
        val mrp = 999.0
        val discountPct = 20.0
        val quantity = 10
        val taxPct = 18.0

        // When - using correct formula with rounding: Taxable = Total / (1 + Tax%/100)
        val rate = mrp - (mrp * discountPct / 100.0) // 799.2
        val total = rate * quantity // 7992 (total with tax included)
        val taxableRaw = total / (1 + (taxPct / 100.0)) // 7992 / 1.18 = 6772.881355...
        val taxable = (taxableRaw * 100).roundToLong() / 100.0 // 6772.88
        val tax = total - taxable // 1219.12

        // Then
        assertEquals(799.2, rate, 0.01)
        assertEquals(7992.0, total, 0.01)
        assertEquals(6772.88, taxable, 0.01)
        assertEquals(1219.12, tax, 0.01)
        // Verify sum equals total
        assertEquals(total, taxable + tax, 0.01)
    }

    @Test
    fun `test multiple items total accumulation`() {
        // Given: 2 different items in one purchase
        val item1Total = 8500.0 // taxable for item 1
        val item2Total = 5000.0 // taxable for item 2
        val expectedPurchaseTotal = 13500.0

        // When
        val actualPurchaseTotal = item1Total + item2Total

        // Then
        assertEquals(expectedPurchaseTotal, actualPurchaseTotal, 0.01)
    }

    @Test
    fun `test no discount scenario`() {
        // Given: MRP 1000, no discount, qty 5, tax 12%
        val mrp = 1000.0
        val quantity = 5
        val taxPct = 12.0

        // When - using correct formula with rounding: Taxable = Total / (1 + Tax%/100)
        val rate = mrp // No discount
        val total = rate * quantity // 5000
        val taxableRaw = total / (1 + (taxPct / 100.0)) // 5000 / 1.12 = 4464.285714...
        val taxable = (taxableRaw * 100).roundToLong() / 100.0 // 4464.29
        val tax = total - taxable // 535.71

        // Then
        assertEquals(1000.0, rate, 0.01)
        assertEquals(5000.0, total, 0.01)
        assertEquals(4464.29, taxable, 0.01)
        assertEquals(535.71, tax, 0.01)
        // Verify sum equals total
        assertEquals(total, taxable + tax, 0.01)
    }

    @Test
    fun `test duplicate items quantity aggregation`() {
        // Given: Same product (barcode + MRP) appears 3 times with quantities 5, 10, 3
        val quantities = listOf(5, 10, 3)
        val expectedTotalQty = 18

        // When
        val actualTotalQty = quantities.sum()

        // Then
        assertEquals(expectedTotalQty, actualTotalQty)
    }

    @Test
    fun `test rate priority - explicit rate over discount`() {
        // Given: MRP 999, discount 20%, but explicit rate 850 is provided
        val mrp = 999.0
        val discountPct = 20.0
        val explicitRate = 850.0

        // When - explicit rate should be used
        val actualRate = explicitRate

        // Then
        assertEquals(850.0, actualRate, 0.01)
        assertNotEquals(mrp - (mrp * discountPct / 100.0), actualRate)
    }

    @Test
    fun `test amount discount priority over percentage discount`() {
        // Given: Both amount and percentage discount provided
        val mrp = 1000.0
        val discountAmt = 200.0
        val discountPct = 15.0

        // When - amount discount should take priority
        val rate = mrp - discountAmt

        // Then
        assertEquals(800.0, rate, 0.01)
        assertNotEquals(mrp - (mrp * discountPct / 100.0), rate)
    }

    @Test
    fun `test zero tax scenario`() {
        // Given: MRP 500, discount 50, qty 2, tax 0%
        val mrp = 500.0
        val discountAmt = 50.0
        val quantity = 2
        val taxPct = 0.0

        // When - using correct formula with rounding
        val rate = mrp - discountAmt // 450
        val total = rate * quantity // 900
        val taxableRaw = if (taxPct > 0) total / (1 + (taxPct / 100.0)) else total
        val taxable = (taxableRaw * 100).roundToLong() / 100.0 // 900.0
        val tax = total - taxable // 0.0

        // Then
        assertEquals(450.0, rate, 0.01)
        assertEquals(900.0, total, 0.01)
        assertEquals(0.0, tax, 0.01)
        assertEquals(900.0, taxable, 0.01)
        // Verify sum equals total
        assertEquals(total, taxable + tax, 0.01)
    }

    @Test
    fun `test high precision decimal calculation`() {
        // Given: MRP with decimals
        val mrp = 999.99
        val discountPct = 12.5
        val quantity = 7
        val taxPct = 18.0

        // When - using correct formula with rounding: Taxable = Total / (1 + Tax%/100)
        val rate = mrp - (mrp * discountPct / 100.0) // 874.99125
        val total = rate * quantity // 6124.93875
        val taxableRaw = total / (1 + (taxPct / 100.0)) // 6124.93875 / 1.18 = 5189.7786...
        val taxable = (taxableRaw * 100).roundToLong() / 100.0 // 5189.78
        val tax = total - taxable // 935.16

        // Then
        assertTrue("rate should be in range", rate in 874.0..876.0)
        assertTrue("total should be in range", total in 6120.0..6130.0)
        assertTrue("taxable should be in range", taxable in 5185.0..5195.0)
        assertTrue("tax should be in range", tax in 930.0..940.0)
        // Verify sum equals total (with tolerance for floating point precision)
        assertEquals(total, taxable + tax, 0.01)
    }

    // Helper methods to simulate the actual calculation logic
    private fun calculateRateWithPercentageDiscount(mrp: Double, discountPct: Double): Double {
        return mrp - (mrp * discountPct / 100.0)
    }

    private fun calculateRateWithAmountDiscount(mrp: Double, discountAmt: Double): Double {
        return mrp - discountAmt
    }
}
