package com.l1khith.calender28.billing

import android.content.Context
import com.l1khith.calender28.repository.UserPreferencesRepository
import com.l1khith.calender28.repository.createDataStore
import com.l1khith.calender28.utils.PlatformUtils
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private var prefsRepo: UserPreferencesRepository? = null
    private var isConfigured = false
    private var devTapCount = 0
    private var lastDevTapTime = 0L

    private fun getRepo(context: Context): UserPreferencesRepository {
        return UserPreferencesRepository.getInstance(context)
    }

    fun initDataStore(context: Context, scope: CoroutineScope) {
        try {
            val repo = getRepo(context)
            scope.launch(Dispatchers.Default) {
                repo.isProUser.collect { isPro ->
                    _isProActive.value = isPro
                }
            }
        } catch (e: Exception) {
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

    fun configure(context: Context, apiKey: String, entitlementId: String = "pro") {
        if (apiKey.isBlank()) {
            _isProActive.value = false
            return
        }
        try {
            Purchases.configure(PurchasesConfiguration.Builder(context, apiKey).build())
            isConfigured = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

