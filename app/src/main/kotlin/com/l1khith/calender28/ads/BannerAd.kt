package com.l1khith.calender28.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.l1khith.calender28.billing.RevenueCatManager
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.utils.Constants
import kotlinx.coroutines.delay

private const val BANNER_AD_UNIT_ID =
    "ca-app-pub-2924141814856423/6252757545"

@Composable
fun BannerAd(modifier: Modifier = Modifier) {

    // STEP 1: Check Pro status
    val isPremium by RevenueCatManager.isPremium
        .collectAsStateWithLifecycle()
    val isProActive by SubscriptionManager.isProActive
        .collectAsStateWithLifecycle()
    val isPro = isPremium || isProActive

    // STEP 2: Pro users → return 0dp Box, no ad, no request
    if (isPro) {
        Box(modifier = modifier.height(0.dp))
        return
    }

    var isReady by remember { mutableStateOf(false) }

    // STEP 3: Defer banner creation 200ms to avoid blocking first frame
    LaunchedEffect(Unit) {
        delay(200)
        isReady = true
    }

    // STEP 4: Show placeholder while not ready
    if (!isReady) {
        Box(modifier = modifier.fillMaxWidth().height(50.dp))
        return
    }

    // STEP 5 & 6: Render AdView safely on UI thread (AdMob SDK requires Main UI thread)
    AndroidView(
        modifier = modifier.fillMaxWidth().height(50.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { adView ->
            adView.destroy()
        }
    )
}
