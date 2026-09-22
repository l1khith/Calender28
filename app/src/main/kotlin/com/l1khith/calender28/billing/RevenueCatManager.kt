package com.l1khith.calender28.billing

import android.app.Activity
import android.content.Context
import android.util.Log
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

    private const val TAG = "RevenueCatManager"

    // EXACT ID from RevenueCat dashboard
    const val ENTITLEMENT_ID = "calender28_pro"

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        if (!Purchases.isConfigured) {
            Log.w(TAG, "init called but Purchases is not configured yet")
            return
        }
        initialized = true

        Purchases.sharedInstance.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { info ->
                val active = info.entitlements[ENTITLEMENT_ID]?.isActive == true
                Log.d(TAG, "Customer info updated: isPremium=$active")
                _isPremium.value = active
            }

        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                val active = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                Log.d(TAG, "Initial customer info received: isPremium=$active")
                _isPremium.value = active
            }

            override fun onError(error: PurchasesError) {
                Log.w(TAG, "Error getting initial customer info: ${error.message} (code: ${error.code})")
            }
        })
    }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        if (!Purchases.isConfigured) {
            Log.w(TAG, "refresh skipped: Purchases not configured")
            return@withContext _isPremium.value
        }
        try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            val active = info.entitlements[ENTITLEMENT_ID]?.isActive == true
            _isPremium.value = active
            Log.d(TAG, "Refreshed customer info: isPremium=$active")
            active
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh customer info: ${e.message}", e)
            _isPremium.value
        }
    }

    suspend fun getOfferings(): Offerings? = withContext(Dispatchers.IO) {
        if (!Purchases.isConfigured) {
            Log.e(TAG, "getOfferings failed: Purchases is not configured. Check REVENUECAT_API_KEY.")
            return@withContext null
        }
        try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            Log.d(
                TAG,
                "Fetched offerings successfully: current=${offerings.current?.identifier}, " +
                        "allKeys=${offerings.all.keys}, " +
                        "currentPackageCount=${offerings.current?.availablePackages?.size ?: 0}"
            )
            offerings
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch offerings from RevenueCat: ${e.message}", e)
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
        withContext(Dispatchers.Main) {
            if (!Purchases.isConfigured) {
                val errorMsg = "Billing system is not configured. Please check your network or app configuration."
                Log.e(TAG, errorMsg)
                return@withContext PurchaseResult.Error(errorMsg)
            }
            try {
                Log.d(TAG, "Initiating purchase for package: ${pkg.identifier}, product: ${pkg.product.id}")
                val result = Purchases.sharedInstance.awaitPurchase(
                    com.revenuecat.purchases.PurchaseParams
                        .Builder(activity, pkg)
                        .build()
                )
                val active = result.customerInfo
                    .entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPremium.value = active
                Log.d(TAG, "Purchase completed: isPremium=$active")
                if (active) PurchaseResult.Success
                else PurchaseResult.Error("Purchase processed, but Pro entitlement was not granted. Please restore purchases or contact support.")
            } catch (e: PurchasesTransactionException) {
                if (e.userCancelled) {
                    Log.d(TAG, "Purchase cancelled by user")
                    PurchaseResult.Cancelled
                } else {
                    Log.e(TAG, "Purchase transaction error: ${e.message} (code: ${e.code})", e)
                    PurchaseResult.Error(e.message ?: "Purchase failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during purchase: ${e.message}", e)
                PurchaseResult.Error(e.message ?: "Unknown error occurred during purchase")
            }
        }

    suspend fun restore(): PurchaseResult = withContext(Dispatchers.IO) {
        if (!Purchases.isConfigured) {
            val errorMsg = "Billing system is not configured. Please check your network or app configuration."
            Log.e(TAG, errorMsg)
            return@withContext PurchaseResult.Error(errorMsg)
        }
        try {
            Log.d(TAG, "Restoring purchases...")
            val info = Purchases.sharedInstance.awaitRestore()
            val active = info.entitlements[ENTITLEMENT_ID]?.isActive == true
            _isPremium.value = active
            Log.d(TAG, "Purchases restored: isPremium=$active")
            if (active) PurchaseResult.Success
            else PurchaseResult.Error("No active Pro subscription found for this Google Play account.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore purchases: ${e.message}", e)
            PurchaseResult.Error(e.message ?: "Restore failed")
        }
    }
}
