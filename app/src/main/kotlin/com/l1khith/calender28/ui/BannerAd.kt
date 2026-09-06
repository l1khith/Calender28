package com.l1khith.calender28.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val isProActive by com.l1khith.calender28.billing.SubscriptionManager.isProActive.collectAsStateWithLifecycle()
    if (isProActive) return

    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Defer AdView creation until splash transition finishes to prevent main-thread stutter
        delay(400)
        isReady = true
    }

    if (!isReady) {
        Box(modifier = modifier.fillMaxWidth().height(50.dp))
        return
    }

    val configuredAdUnitId = System.getProperty("ADMOB_BANNER_UNIT_ID") ?: com.l1khith.calender28.utils.Constants.TEST_ADMOB_BANNER_ID
    AndroidView(
        modifier = modifier.fillMaxWidth().height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = configuredAdUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

