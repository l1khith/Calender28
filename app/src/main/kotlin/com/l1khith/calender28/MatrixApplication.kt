package com.l1khith.calender28

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.service.MidnightRolloverWorker
import com.l1khith.calender28.service.NotificationHelper
import com.l1khith.calender28.utils.CalendarContentObserver
import com.l1khith.calender28.di.AppContainer
import com.l1khith.calender28.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

import kotlinx.coroutines.launch

class MatrixApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // These just launch async coroutines — safe on main thread
        SubscriptionManager.initDataStore(this, applicationScope)
        com.l1khith.calender28.security.AppLockManager.init(this, applicationScope)
        com.l1khith.calender28.utils.AppSettingsManager.init(this, applicationScope)

        // Heavy SDK inits moved to background thread
        applicationScope.launch(Dispatchers.IO) {
            try {
                MobileAds.initialize(this@MatrixApplication)
            } catch (_: Exception) {}

            val revenueCatApiKey = BuildConfig.REVENUECAT_API_KEY
            if (revenueCatApiKey.isNotBlank()) {
                SubscriptionManager.configure(this@MatrixApplication, revenueCatApiKey)
            }

            NotificationHelper(this@MatrixApplication).createNotificationChannels()
            MidnightRolloverWorker.scheduleNextMidnightRollover(this@MatrixApplication)
            CalendarContentObserver.register(this@MatrixApplication)
        }
    }
}
