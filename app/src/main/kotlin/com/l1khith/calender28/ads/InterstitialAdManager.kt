package com.l1khith.calender28.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.l1khith.calender28.billing.RevenueCatManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val INTERSTITIAL_AD_UNIT_ID =
    "ca-app-pub-2924148184856423/8473570084"

object InterstitialAdManager {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

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
            InterstitialAd.load(
                context,
                INTERSTITIAL_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoading = false
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoading = false
                    }
                }
            )
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
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
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
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onAdUnavailable()
                }
            }
            ad.show(activity)
        } else {
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
