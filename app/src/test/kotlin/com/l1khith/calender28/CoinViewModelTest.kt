package com.l1khith.calender28

import com.l1khith.calender28.ads.RewardedAdManager
import com.l1khith.calender28.repository.CoinRepository
import com.l1khith.calender28.repository.CoinRewardResult
import com.l1khith.calender28.test.MainDispatcherRule
import com.l1khith.calender28.viewmodel.CoinViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CoinViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val coinRepository: CoinRepository = mock()

    private lateinit var viewModel: CoinViewModel

    @Before
    fun setUp() {
        whenever(coinRepository.coinBalance).thenReturn(flowOf(0))
        whenever(coinRepository.recentTransactions).thenReturn(flowOf(emptyList()))

        viewModel = CoinViewModel(
            coinRepository = coinRepository,
            rewardedAdManager = RewardedAdManager
        )
    }

    @Test
    fun `test initial combo state is inactive`() {
        assertFalse(viewModel.isComboActive.value)
        assertEquals(0, viewModel.comboRemainingSeconds.value)
    }

    @Test
    fun `test first rewarded ad triggers base reward and activates combo ticker`() = runTest {
        whenever(coinRepository.rewardAdWatch(eq(10), eq(false))).thenReturn(
            CoinRewardResult(
                coinsAwarded = 10,
                reasonName = "REWARDED_AD",
                newBalance = 10,
                message = "+10 CalCoins"
            )
        )

        viewModel.onRewardedAdCompleted()

        assertTrue(viewModel.isComboActive.value)
        assertEquals(60, viewModel.comboRemainingSeconds.value)
    }

    @Test
    fun `test second rewarded ad within 60s triggers combo bonus and immediately resets`() = runTest {
        whenever(coinRepository.rewardAdWatch(eq(10), eq(false))).thenReturn(
            CoinRewardResult(10, "REWARDED_AD", 10, "+10 CalCoins")
        )
        whenever(coinRepository.rewardAdWatch(eq(20), eq(true))).thenReturn(
            CoinRewardResult(20, "REWARDED_AD_COMBO", 30, "+20 CalCoins")
        )

        // Ad 1: Base
        viewModel.onRewardedAdCompleted()
        assertTrue(viewModel.isComboActive.value)

        // Ad 2: Combo (immediately within 60s)
        viewModel.onRewardedAdCompleted()

        // Combo must immediately reset to base
        assertFalse(viewModel.isComboActive.value)
        assertEquals(0, viewModel.comboRemainingSeconds.value)
    }

    @Test
    fun `test resetCombo resets state to base`() {
        viewModel.startComboTicker(60)
        assertTrue(viewModel.isComboActive.value)
        assertEquals(60, viewModel.comboRemainingSeconds.value)

        viewModel.resetCombo()

        assertFalse(viewModel.isComboActive.value)
        assertEquals(0, viewModel.comboRemainingSeconds.value)
    }
}
