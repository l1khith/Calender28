package com.l1khith.calender28.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.l1khith.calender28.Calender28Application
import com.l1khith.calender28.billing.RevenueCatManager
import com.l1khith.calender28.billing.SubscriptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val INTERSTITIAL_AD_UNIT_ID =
    "ca-app-pub-2924141814856423/8473570084"

object InterstitialAdManager {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    /**
     * Load an interstitial ad. Call this AFTER onboarding or after a
     * successful habit completion — NEVER during app launch.
     *
     * SAFE to call multiple times — will skip if already loading.
     */
    suspend fun loadAd(context: Context) {
        // Switch to Main thread: AdMob SDK enforces Preconditions.checkMainThread()
        withContext(Dispatchers.Main) {
            // Check isLoading INSIDE the Main dispatcher to avoid race conditions
            if (isLoading) return@withContext

            // CRITICAL: Skip entirely for Pro users
            if (RevenueCatManager.isPremium.value || SubscriptionManager.isProActive.value) {
                interstitialAd = null
                return@withContext
            }

            isLoading = true
            InterstitialAd.load(
                context.applicationContext,  // ← Use application context
                INTERSTITIAL_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoading = false
                        android.util.Log.d("InterstitialAdManager", "AdMob interstitial loaded successfully: $INTERSTITIAL_AD_UNIT_ID")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoading = false
                        android.util.Log.e("InterstitialAdManager", "AdMob interstitial failed to load: code=${error.code}, message=${error.message}")
                    }
                }
            )
        }
    }

    /**
     * Show the loaded interstitial. Must be called from MAIN thread.
     * Callback runs whether ad shows, fails, or is dismissed.
     */
    fun show(activity: Activity? = null, onDismiss: () -> Unit = {}) {
        // Skip entirely for Pro users
        if (RevenueCatManager.isPremium.value || SubscriptionManager.isProActive.value) {
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            // No ad loaded — proceed immediately
            onDismiss()
            return
        }

        val targetActivity = activity ?: Calender28Application.currentActivity?.get()
        if (targetActivity == null || targetActivity.isFinishing || targetActivity.isDestroyed) {
            onDismiss()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onDismiss()
            }
        }
        ad.show(targetActivity)
    }

    fun show(onDismiss: () -> Unit) {
        show(null, onDismiss)
    }

    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdUnavailable: () -> Unit = {}
    ) {
        if (interstitialAd != null) {
            show(activity, onAdDismissed)
        } else {
            onAdUnavailable()
        }
    }

    fun isAdLoaded(): Boolean {
        val isPro = RevenueCatManager.isPremium.value || SubscriptionManager.isProActive.value
        return interstitialAd != null && !isPro
    }

    /**
     * Call from hosting Activity's onPause.
     * Prevents leaks if user backgrounds the app while ad is loaded.
     */
    fun clear() {
        interstitialAd = null
    }
}
