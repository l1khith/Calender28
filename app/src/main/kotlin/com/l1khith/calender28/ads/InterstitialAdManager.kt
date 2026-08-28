package com.l1khith.calender28.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"

    // Official Google AdMob Test Interstitial Ad Unit ID
    private const val DEFAULT_TEST_AD_UNIT_ID = com.l1khith.calender28.utils.Constants.TEST_ADMOB_INTERSTITIAL_ID

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) return

        isLoading = true
        val adUnitId = System.getProperty("ADMOB_INTERSTITIAL_UNIT_ID") ?: DEFAULT_TEST_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "AdMob Interstitial Ad loaded successfully.")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Failed to load AdMob Interstitial: ${loadAdError.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun isAdLoaded(): Boolean = interstitialAd != null

    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdUnavailable: () -> Unit = {}
    ) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    interstitialAd = null
                    loadAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    loadAd(activity)
                    onAdUnavailable()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad displayed full screen.")
                }
            }
            ad.show(activity)
        } else {
            loadAd(activity)
            onAdUnavailable()
        }
    }
}
