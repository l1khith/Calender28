package com.l1khith.calender28

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.service.MidnightRolloverWorker
import com.l1khith.calender28.service.NotificationHelper
import com.l1khith.calender28.utils.CalendarContentObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MatrixApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        try {
            MobileAds.initialize(this)
        } catch (_: Exception) {}

        SubscriptionManager.initDataStore(this, applicationScope)
        val revenueCatApiKey = BuildConfig.REVENUECAT_API_KEY
        if (revenueCatApiKey.isNotBlank()) {
            SubscriptionManager.configure(this, revenueCatApiKey)
        }

        com.l1khith.calender28.security.AppLockManager.init(this, applicationScope)

        NotificationHelper(this).createNotificationChannels()
        MidnightRolloverWorker.scheduleNextMidnightRollover(this)
        CalendarContentObserver.register(this)
    }
}
