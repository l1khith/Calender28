package com.l1khith.calender28.billing

import android.app.Activity
import android.content.Context
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

object RevenueCatManager {

    // EXACT ID from RevenueCat dashboard
    const val ENTITLEMENT_ID = "calender28_pro"

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        Purchases.sharedInstance.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { info ->
                val active = info.entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPremium.value = active
                SubscriptionManager.setProActive(active)
            }

        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                val active = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPremium.value = active
                SubscriptionManager.setProActive(active)
            }

            override fun onError(error: PurchasesError) {
                // silently ignore — cached state remains
            }
        })
    }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            val active = info.entitlements[ENTITLEMENT_ID]?.isActive == true
            _isPremium.value = active
            SubscriptionManager.setProActive(active)
            active
        } catch (e: Exception) {
            _isPremium.value
        }
    }

    suspend fun getOfferings(): Offerings? = withContext(Dispatchers.IO) {
        try {
            Purchases.sharedInstance.awaitOfferings()
        } catch (e: Exception) {
            null
        }
    }

    fun observePremium(): Flow<Boolean> = isPremium

    sealed class PurchaseResult {
        object Success : PurchaseResult()
        object Cancelled : PurchaseResult()
        data class Error(val message: String) : PurchaseResult()
    }

    suspend fun purchase(activity: Activity, pkg: Package): PurchaseResult =
        withContext(Dispatchers.IO) {
            try {
                val result = Purchases.sharedInstance.awaitPurchase(
                    com.revenuecat.purchases.PurchaseParams
                        .Builder(activity, pkg)
                        .build()
                )
                val active = result.customerInfo
                    .entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPremium.value = active
                SubscriptionManager.setProActive(active)
                if (active) PurchaseResult.Success
                else PurchaseResult.Error("Entitlement not granted")
            } catch (e: PurchasesTransactionException) {
                if (e.userCancelled) PurchaseResult.Cancelled
                else PurchaseResult.Error(e.message ?: "Purchase failed")
            } catch (e: Exception) {
                PurchaseResult.Error(e.message ?: "Unknown error")
            }
        }

    suspend fun restore(): PurchaseResult = withContext(Dispatchers.IO) {
        try {
            val info = Purchases.sharedInstance.awaitRestore()
            val active = info.entitlements[ENTITLEMENT_ID]?.isActive == true
            _isPremium.value = active
            SubscriptionManager.setProActive(active)
            if (active) PurchaseResult.Success
            else PurchaseResult.Error("No active subscription found")
        } catch (e: Exception) {
            PurchaseResult.Error(e.message ?: "Restore failed")
        }
    }
}
