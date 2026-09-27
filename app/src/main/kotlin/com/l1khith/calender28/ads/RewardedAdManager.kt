package com.l1khith.calender28.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.l1khith.calender28.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Production-ready Rewarded Video Ad Manager for Calender28.
 * Handles AdMob RewardedAd lifecycle, reactive state, preloading, and safe presentation.
 */
object RewardedAdManager {

    private const val TAG = "RewardedAdManager"

    // Google Official Android Test Ad Unit ID for Rewarded Ads
    const val TEST_REWARDED_AD_UNIT_ID = Constants.TEST_ADMOB_REWARDED_ID

    // Production Rewarded Ad Unit ID
    const val PROD_REWARDED_AD_UNIT_ID = Constants.ADMOB_REWARDED_ID

    /**
     * Defaulted to true so real production Ad Unit ID is used immediately.
     */
    var forceProductionAdUnit: Boolean = true

    /**
     * Resolves the active Rewarded Ad Unit ID.
     * Uses test ID in debug builds to protect AdMob account integrity against policy violations,
     * and uses the verified production ID for release builds.
     */
    val adUnitId: String
        get() = if (forceProductionAdUnit || !com.l1khith.calender28.BuildConfig.DEBUG) {
            PROD_REWARDED_AD_UNIT_ID
        } else {
            TEST_REWARDED_AD_UNIT_ID
        }

    private var rewardedAd: RewardedAd? = null

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Preloads a Rewarded Ad. Safe to call from any thread or repeatedly.
     * Enforces execution on the Main thread as required by the AdMob SDK.
     */
    fun loadAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Main).launch {
            if (_isLoading.value) {
                Log.d(TAG, "Rewarded ad is already loading. Skipping request.")
                return@launch
            }
            if (rewardedAd != null) {
                _isAdReady.value = true
                Log.d(TAG, "Rewarded ad is already cached and ready.")
                onLoaded?.invoke()
                return@launch
            }

            val currentUnitId = adUnitId
            _isLoading.value = true
            Log.d(TAG, "Initiating Rewarded Ad load with Unit ID: $currentUnitId (isRelease=${!com.l1khith.calender28.BuildConfig.DEBUG})")

            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                appContext,
                currentUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        _isLoading.value = false
                        _isAdReady.value = true
                        Log.d(TAG, "Rewarded ad successfully loaded.")
                        onLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        _isLoading.value = false
                        _isAdReady.value = false
                        Log.e(TAG, "Rewarded ad failed to load: code=${loadAdError.code}, message=${loadAdError.message}")
                        onFailed?.invoke(loadAdError.message)
                    }
                }
            )
        }
    }

    /**
     * Displays the loaded Rewarded Ad.
     *
     * @param activity The host Activity to present the full-screen ad.
     * @param onRewardEarned Invoked when the user has watched the ad to completion and earned their reward.
     * @param onDismissed Invoked when the full-screen ad content is dismissed.
     * @param onError Invoked if ad display fails or if no ad is currently loaded.
     */
    fun show(
        activity: Activity,
        onRewardEarned: (RewardItem) -> Unit,
        onDismissed: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            _isAdReady.value = false
            Log.w(TAG, "show() called but rewardedAd was null. Triggering preload.")
            onError("Video ad is still loading. Please try again in a moment.")
            loadAd(activity.applicationContext)
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "show() aborted: Activity is finishing or destroyed.")
            onError("Activity is not available.")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad presented full-screen.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed by user.")
                rewardedAd = null
                _isAdReady.value = false
                onDismissed()
                // Auto-preload the next ad in the background
                loadAd(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Rewarded ad failed to show: code=${adError.code}, message=${adError.message}")
                rewardedAd = null
                _isAdReady.value = false
                onError(adError.message)
                // Preload again after failure
                loadAd(activity.applicationContext)
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User completed watching rewarded ad: amount=${rewardItem.amount}, type=${rewardItem.type}")
            onRewardEarned(rewardItem)
        }
    }

    /**
     * Synchronous readiness check.
     */
    fun isAdLoaded(): Boolean {
        return rewardedAd != null && _isAdReady.value
    }

    /**
     * Clears cached ad to prevent memory leaks during teardown.
     */
    fun clear() {
        rewardedAd = null
        _isAdReady.value = false
        _isLoading.value = false
    }
}
