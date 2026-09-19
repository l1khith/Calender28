package com.l1khith.calender28.billing

import com.l1khith.calender28.ads.InterstitialAdManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevenueCatManagerTest {

    @Test
    fun testEntitlementId_isStrictlyCalender28Pro() {
        assertEquals("calender28_pro", RevenueCatManager.ENTITLEMENT_ID)
    }

    @Test
    fun testInitialState_isNotPremium() {
        assertFalse(RevenueCatManager.isPremium.value)
    }

    @Test
    fun testPurchaseResult_success() {
        val result: RevenueCatManager.PurchaseResult = RevenueCatManager.PurchaseResult.Success
        assertTrue(result is RevenueCatManager.PurchaseResult.Success)
    }

    @Test
    fun testPurchaseResult_cancelled() {
        val result: RevenueCatManager.PurchaseResult = RevenueCatManager.PurchaseResult.Cancelled
        assertTrue(result is RevenueCatManager.PurchaseResult.Cancelled)
    }

    @Test
    fun testPurchaseResult_error() {
        val errorMsg = "Payment declined"
        val result: RevenueCatManager.PurchaseResult = RevenueCatManager.PurchaseResult.Error(errorMsg)
        assertTrue(result is RevenueCatManager.PurchaseResult.Error)
        assertEquals(errorMsg, (result as RevenueCatManager.PurchaseResult.Error).message)
    }

    @Test
    fun testInterstitialAdManager_clearsOnDismiss() {
        InterstitialAdManager.clear()
        assertFalse(InterstitialAdManager.isAdLoaded())
    }
}
