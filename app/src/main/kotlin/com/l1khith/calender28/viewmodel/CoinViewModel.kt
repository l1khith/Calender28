package com.l1khith.calender28.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.ads.RewardedAdManager
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.data.CoinTransactionEntity
import com.l1khith.calender28.repository.CoinRepository
import com.l1khith.calender28.repository.CoinRepositoryImpl
import com.l1khith.calender28.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CoinUiEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : CoinUiEvent
    data class CoinsEarned(val amount: Int, val message: String) : CoinUiEvent
    object PremiumUnlocked : CoinUiEvent
}

class CoinViewModel(
    private val coinRepository: CoinRepository,
    private val rewardedAdManager: RewardedAdManager = RewardedAdManager
) : androidx.lifecycle.ViewModel() {

    constructor(
        application: Application,
        coinRepository: CoinRepository,
        rewardedAdManager: RewardedAdManager = RewardedAdManager
    ) : this(coinRepository, rewardedAdManager) {
        loadRewardedAd(application)
    }

    constructor(application: Application) : this(
        application,
        CoinRepositoryImpl(application.applicationContext),
        RewardedAdManager
    )

    val coinBalance: StateFlow<Int> = coinRepository.coinBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentTransactions: StateFlow<List<CoinTransactionEntity>> = coinRepository.recentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isProActive: StateFlow<Boolean> = SubscriptionManager.isProActive

    private val _uiEvent = MutableSharedFlow<CoinUiEvent>(extraBufferCapacity = 64)
    val uiEvent: SharedFlow<CoinUiEvent> = _uiEvent.asSharedFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    // --- Rewarded Video Combo State Machine ---
    val isRewardedAdReady: StateFlow<Boolean> = rewardedAdManager.isAdReady
    val isAdLoading: StateFlow<Boolean> = rewardedAdManager.isLoading

    private val _isComboActive = MutableStateFlow(false)
    val isComboActive: StateFlow<Boolean> = _isComboActive.asStateFlow()

    private val _comboRemainingSeconds = MutableStateFlow(0)
    val comboRemainingSeconds: StateFlow<Int> = _comboRemainingSeconds.asStateFlow()

    private var lastAdCompletedTimestamp: Long = 0L
    private var comboTickerJob: Job? = null

    /**
     * Triggered when a rewarded ad completes successfully.
     * Implements strict 2-step combo state machine:
     * - Ad 1 (Base): +10 CalCoins, starts 60s combo window.
     * - Ad 2 (Combo Bonus): If completed within 60s, awards +20 CalCoins and immediately resets.
     * - If >60s elapse, expires and resets back to base state (+10).
     */
    fun onRewardedAdCompleted() {
        val now = System.currentTimeMillis()
        val isCombo = _isComboActive.value && (now - lastAdCompletedTimestamp <= Constants.REWARDED_AD_COMBO_WINDOW_MS)
        val rewardAmount = if (isCombo) Constants.REWARD_REWARDED_AD_COMBO else Constants.REWARD_REWARDED_AD_BASE

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = coinRepository.rewardAdWatch(coins = rewardAmount, isCombo = isCombo)
                _uiEvent.emit(
                    CoinUiEvent.CoinsEarned(
                        amount = result.coinsAwarded,
                        message = if (isCombo) "🔥 Combo Bonus! +${result.coinsAwarded} CalCoins awarded!" else "🎬 +${result.coinsAwarded} CalCoins awarded!"
                    )
                )
            } catch (e: Exception) {
                _uiEvent.emit(CoinUiEvent.ShowMessage("Failed to award coins: ${e.message}", isError = true))
            }
        }

        if (isCombo) {
            // Ad 2 completed: Reset combo back to base state!
            resetCombo()
        } else {
            // Ad 1 completed: Start 60s countdown ticker!
            lastAdCompletedTimestamp = now
            startComboTicker(Constants.REWARDED_AD_COMBO_WINDOW_SECONDS)
        }
    }

    /**
     * Starts a live 1-second countdown ticker for the combo duration.
     */
    fun startComboTicker(seconds: Int) {
        comboTickerJob?.cancel()
        _isComboActive.value = true
        _comboRemainingSeconds.value = seconds

        comboTickerJob = viewModelScope.launch(Dispatchers.Default) {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _comboRemainingSeconds.value = remaining
            }
            resetCombo()
        }
    }

    /**
     * Resets combo eligibility back to base state.
     */
    fun resetCombo() {
        comboTickerJob?.cancel()
        comboTickerJob = null
        lastAdCompletedTimestamp = 0L
        _isComboActive.value = false
        _comboRemainingSeconds.value = 0
    }

    /**
     * Displays the rewarded ad via [RewardedAdManager].
     */
    fun showRewardedAd(activity: Activity) {
        if (!rewardedAdManager.isAdLoaded()) {
            _uiEvent.tryEmit(CoinUiEvent.ShowMessage("Video ad is still loading. Please wait a moment...", isError = false))
            rewardedAdManager.loadAd(activity.applicationContext)
            return
        }

        rewardedAdManager.show(
            activity = activity,
            onRewardEarned = {
                onRewardedAdCompleted()
            },
            onDismissed = {
                // Ad dismissed callback
            },
            onError = { error ->
                viewModelScope.launch {
                    _uiEvent.emit(CoinUiEvent.ShowMessage("Failed to show video ad: $error", isError = true))
                }
            }
        )
    }

    /**
     * Preloads rewarded video ad in background.
     */
    fun loadRewardedAd(context: Context) {
        rewardedAdManager.loadAd(context)
    }

    fun redeemPromoCode(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = coinRepository.redeemPromoCode(code)
            result.onSuccess { reward ->
                _uiEvent.emit(CoinUiEvent.CoinsEarned(reward.coinsAwarded, reward.message))
            }.onFailure { error ->
                _uiEvent.emit(CoinUiEvent.ShowMessage(error.message ?: "Failed to redeem code", isError = true))
            }
        }
    }

    fun purchasePremiumWithCoins() {
        if (_isPurchasing.value) return
        _isPurchasing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = coinRepository.buyPremiumWithCoins()
                result.onSuccess {
                    _uiEvent.emit(CoinUiEvent.PremiumUnlocked)
                    _uiEvent.emit(CoinUiEvent.ShowMessage("🎉 Premium unlocked successfully with 1,500 CalCoins!"))
                }.onFailure { error ->
                    _uiEvent.emit(CoinUiEvent.ShowMessage(error.message ?: "Could not unlock Premium", isError = true))
                }
            } finally {
                _isPurchasing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        comboTickerJob?.cancel()
    }
}
