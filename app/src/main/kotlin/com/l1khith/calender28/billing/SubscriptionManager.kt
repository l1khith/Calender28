package com.l1khith.calender28.billing

import android.content.Context
import com.l1khith.calender28.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * SubscriptionManager provides the app's effective Pro status.
 *
 * Architecture & Refund Protection Rules:
 * 1. RevenueCat is the ONLY source of truth for in-app / subscription Pro status.
 * 2. RevenueCat premium state is NEVER persisted to Room, DataStore, or SharedPreferences.
 * 3. CalCoins premium unlock (1,500 coins) is isolated in DataStore ("coin_premium_unlocked")
 *    and NEVER masquerades as a RevenueCat subscription.
 * 4. Effective Pro status is reactively derived via combine(RevenueCat, coinUnlock).
 * 5. Legacy "is_pro_user" DataStore key is purged on initialization to permanently prevent
 *    refund exploits on devices that cached previous purchases.
 */
object SubscriptionManager {

    private val _coinPremiumUnlocked = MutableStateFlow(false)
    val coinPremiumUnlocked: StateFlow<Boolean> = _coinPremiumUnlocked.asStateFlow()

    private val _isProActive = MutableStateFlow(false)
    val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private var isDataStoreInitialized = false

    private fun getRepo(context: Context): UserPreferencesRepository {
        return UserPreferencesRepository.getInstance(context.applicationContext)
    }

    fun initDataStore(context: Context, scope: CoroutineScope) {
        if (isDataStoreInitialized) return
        isDataStoreInitialized = true
        scope.launch(Dispatchers.Default) {
            try {
                val repo = getRepo(context)

                // Purge legacy persisted pro key to permanently eradicate old cached state
                repo.purgeLegacyProState()

                // Reactively combine RevenueCat's live status with legitimate CalCoins unlock
                combine(
                    RevenueCatManager.isPremium,
                    repo.coinPremiumUnlocked
                ) { rcPremium, coinUnlocked ->
                    _coinPremiumUnlocked.value = coinUnlocked
                    rcPremium || coinUnlocked
                }.collect { effectivePro ->
                    _isProActive.value = effectivePro
                }
            } catch (e: Exception) {
                isDataStoreInitialized = false
                e.printStackTrace()
            }
        }
    }

    @androidx.annotation.VisibleForTesting
    fun setProActiveForTest(isPro: Boolean) {
        _isProActive.value = isPro
    }
}
