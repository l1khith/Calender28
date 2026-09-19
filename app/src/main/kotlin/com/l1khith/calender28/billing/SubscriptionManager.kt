package com.l1khith.calender28.billing

import android.app.Activity
import android.content.Context
import com.l1khith.calender28.repository.UserPreferencesRepository
import com.l1khith.calender28.utils.PlatformUtils
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SubscriptionManager {

    private val _isProActive = MutableStateFlow(false)
    val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private var isConfigured = false
    private var isDataStoreInitialized = false
    private var devTapCount = 0
    private var lastDevTapTime = 0L

    private fun getRepo(context: Context): UserPreferencesRepository {
        return UserPreferencesRepository.getInstance(context.applicationContext)
    }

    fun initDataStore(context: Context, scope: CoroutineScope) {
        if (isDataStoreInitialized) return
        isDataStoreInitialized = true
        try {
            val repo = getRepo(context)
            scope.launch(Dispatchers.Default) {
                repo.isProUser.collect { isPro ->
                    _isProActive.value = isPro
                }
            }
        } catch (e: Exception) {
            isDataStoreInitialized = false
            e.printStackTrace()
        }
    }

    fun onDevModeTap(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastDevTapTime > 2000) devTapCount = 0
        lastDevTapTime = now
        devTapCount++
        if (devTapCount >= 5) {
            devTapCount = 0
            _isProActive.value = !_isProActive.value
            return true
        }
        return false
    }

    fun isDevModeActive(): Boolean = _isProActive.value && !isConfigured

    fun toggleProMode(context: Context, scope: CoroutineScope) {
        val newStatus = !_isProActive.value
        _isProActive.value = newStatus

        scope.launch(Dispatchers.Default) {
            try {
                val repo = getRepo(context)
                repo.updateIsProUser(newStatus)

                withContext(Dispatchers.Main.immediate) {
                    val message = if (newStatus) {
                        "Pro Mode Activated (Testing Phase)"
                    } else {
                        "Pro Mode Deactivated (Testing Phase)"
                    }
                    PlatformUtils.showToast(context, message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleProMode(scope: CoroutineScope) {
        val newStatus = !_isProActive.value
        _isProActive.value = newStatus
    }

    fun setProActive(isPro: Boolean) {
        _isProActive.value = isPro
    }

    fun configure(context: Context, apiKey: String, entitlementId: String = "calender28_pro") {
        if (apiKey.isBlank() || isConfigured) return

        try {
            val configuration = PurchasesConfiguration.Builder(context.applicationContext, apiKey)
                .build()
            Purchases.configure(configuration)
            isConfigured = true

            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                checkEntitlements(customerInfo, entitlementId, context)
            }

            // Initial customer info refresh
            refreshCustomerInfo(context, entitlementId)
            fetchOfferings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Stable scope for background work that outlives individual callbacks
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun checkEntitlements(customerInfo: CustomerInfo, entitlementId: String, context: Context) {
        val hasPro = customerInfo.entitlements[entitlementId]?.isActive == true ||
                     customerInfo.entitlements["calender28_pro"]?.isActive == true ||
                     customerInfo.entitlements["pro"]?.isActive == true ||
                     customerInfo.entitlements["premium"]?.isActive == true

        if (hasPro) {
            _isProActive.value = true
            persistenceScope.launch {
                try {
                    getRepo(context).updateIsProUser(true)
                } catch (_: Exception) {}
            }
        }
    }

    fun refreshCustomerInfo(context: Context, entitlementId: String = "pro") {
        if (!isConfigured) return
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                checkEntitlements(customerInfo, entitlementId, context)
            }

            override fun onError(error: PurchasesError) {
                _errorMessage.value = error.message
            }
        })
    }

    fun fetchOfferings() {
        if (!isConfigured) return
        _isLoading.value = true
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                _isLoading.value = false
                _offerings.value = offerings
            }

            override fun onError(error: PurchasesError) {
                _isLoading.value = false
                _errorMessage.value = error.message
            }
        })
    }

    fun purchasePackage(
        activity: Activity,
        rcPackage: Package,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!isConfigured) {
            onError("Purchases SDK is not configured")
            return
        }

        _isLoading.value = true
        val params = PurchaseParams.Builder(activity, rcPackage).build()
        Purchases.sharedInstance.purchase(params, object : PurchaseCallback {
            override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                _isLoading.value = false
                checkEntitlements(customerInfo, "pro", activity)
                onSuccess()
            }

            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                _isLoading.value = false
                if (!userCancelled) {
                    _errorMessage.value = error.message
                    onError(error.message)
                }
            }
        })
    }

    fun restorePurchases(context: Context, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        if (!isConfigured) {
            onError("Purchases SDK is not configured")
            return
        }

        _isLoading.value = true
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isLoading.value = false
                checkEntitlements(customerInfo, "pro", context)
                onSuccess()
            }

            override fun onError(error: PurchasesError) {
                _isLoading.value = false
                _errorMessage.value = error.message
                onError(error.message)
            }
        })
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
