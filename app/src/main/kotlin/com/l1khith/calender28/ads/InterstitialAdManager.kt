package com.l1khith.calender28.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.l1khith.calender28.BuildConfig
import com.l1khith.calender28.billing.RevenueCatManager
import com.l1khith.calender28.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PROD_INTERSTITIAL_AD_UNIT_ID =
    "ca-app-pub-2924148184856423/8473570084"

object InterstitialAdManager {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preload(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            loadAd(context.applicationContext)
        }
    }

    suspend fun loadAd(context: Context) {
        if (isLoading) return
        withContext(Dispatchers.Main) {
            // Pro user → skip loading entirely
            val isPro = RevenueCatManager.isPremium.value || com.l1khith.calender28.billing.SubscriptionManager.isProActive.value
            if (isPro) {
                interstitialAd = null
                return@withContext
            }
            isLoading = true

            fun requestAd(unitId: String) {
                InterstitialAd.load(
                    context.applicationContext,
                    unitId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            isLoading = false
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            // In DEBUG builds, if the production unit returns NO_FILL (3) because it is new or not yet propagated on Google servers, fallback to sample test unit
                            if (BuildConfig.DEBUG && unitId != Constants.TEST_ADMOB_INTERSTITIAL_ID && error.code == AdRequest.ERROR_CODE_NO_FILL) {
                                requestAd(Constants.TEST_ADMOB_INTERSTITIAL_ID)
                            } else {
                                interstitialAd = null
                                isLoading = false
                            }
                        }
                    }
                )
            }

            requestAd(PROD_INTERSTITIAL_AD_UNIT_ID)
        }
    }

    fun show(activity: Activity? = null, onDismiss: () -> Unit = {}) {
        val isPro = RevenueCatManager.isPremium.value || com.l1khith.calender28.billing.SubscriptionManager.isProActive.value
        if (isPro) {
            onDismiss()
            return
        }
        val ad = interstitialAd
        if (ad != null && activity != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preload(activity)
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    preload(activity)
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            if (activity != null) preload(activity)
            onDismiss()
        }
    }

    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdUnavailable: () -> Unit = {}
    ) {
        val isPro = RevenueCatManager.isPremium.value || com.l1khith.calender28.billing.SubscriptionManager.isProActive.value
        if (isPro) {
            onAdDismissed()
            return
        }
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preload(activity)
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    preload(activity)
                    onAdUnavailable()
                }
            }
            ad.show(activity)
        } else {
            preload(activity)
            onAdUnavailable()
        }
    }

    fun isAdLoaded(): Boolean {
        val isPro = RevenueCatManager.isPremium.value || com.l1khith.calender28.billing.SubscriptionManager.isProActive.value
        return interstitialAd != null && !isPro
    }

    fun clear() {
        interstitialAd = null
    }
}
