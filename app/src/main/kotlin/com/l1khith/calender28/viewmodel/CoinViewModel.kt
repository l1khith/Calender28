package com.l1khith.calender28.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.data.CoinTransactionEntity
import com.l1khith.calender28.repository.CoinRepository
import com.l1khith.calender28.repository.CoinRepositoryImpl
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
    application: Application,
    private val coinRepository: CoinRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        CoinRepositoryImpl(application.applicationContext)
    )

    val coinBalance: StateFlow<Int> = coinRepository.coinBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentTransactions: StateFlow<List<CoinTransactionEntity>> = coinRepository.recentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isProActive: StateFlow<Boolean> = SubscriptionManager.isProActive

    private val _uiEvent = MutableSharedFlow<CoinUiEvent>()
    val uiEvent: SharedFlow<CoinUiEvent> = _uiEvent.asSharedFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    fun redeemPromoCode(code: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
}
