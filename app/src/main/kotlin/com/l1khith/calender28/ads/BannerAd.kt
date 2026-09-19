package com.l1khith.calender28.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.l1khith.calender28.BuildConfig
import com.l1khith.calender28.billing.RevenueCatManager
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.utils.Constants
import kotlinx.coroutines.delay

private const val BANNER_AD_UNIT_ID =
    "ca-app-pub-2924148184856423/6252757545"

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val isPremium by RevenueCatManager.isPremium.collectAsStateWithLifecycle()
    val isProActive by SubscriptionManager.isProActive.collectAsStateWithLifecycle()
    val isPro = isPremium || isProActive

    // Pro user → no ad, no request, zero height
    if (isPro) {
        Box(modifier = modifier.height(0.dp))
        return
    }

    var isReady by remember { mutableStateOf(false) }

    // Defer banner creation slightly to avoid blocking initial render
    LaunchedEffect(Unit) {
        delay(300)
        isReady = true
    }

    if (!isReady) {
        Box(modifier = modifier.height(50.dp))
        return
    }

    AndroidView(
        modifier = modifier.height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        if (BuildConfig.DEBUG && adUnitId != Constants.TEST_ADMOB_BANNER_ID && error.code == AdRequest.ERROR_CODE_NO_FILL) {
                            adUnitId = Constants.TEST_ADMOB_BANNER_ID
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
