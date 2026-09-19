package com.l1khith.calender28

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.l1khith.calender28.billing.RevenueCatManager
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.di.AppContainer
import com.l1khith.calender28.di.DefaultAppContainer
import com.l1khith.calender28.service.MidnightRolloverWorker
import com.l1khith.calender28.service.NotificationHelper
import com.l1khith.calender28.utils.CalendarContentObserver
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Calender28Application : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // AdMob — configure test devices and initialize
        val testDeviceIds = listOf(
            "30C817764033E417985FB2473D74A061",
            com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR
        )
        val requestConfiguration = com.google.android.gms.ads.RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        MobileAds.initialize(this) { }

        // RevenueCat — only initialize if API key present
        val key = BuildConfig.REVENUECAT_API_KEY
        if (key.isNotBlank()) {
            Purchases.configure(
                PurchasesConfiguration.Builder(this, key)
                    .appUserID(null) // anonymous — RevenueCat generates ID
                    .build()
            )
            RevenueCatManager.init(this)
        }

        // Initialize DataStore & App Managers
        SubscriptionManager.initDataStore(this, applicationScope)
        com.l1khith.calender28.security.AppLockManager.init(this, applicationScope)
        com.l1khith.calender28.utils.AppSettingsManager.init(this, applicationScope)
        com.l1khith.calender28.ui.theme.ThemeManager.init(this, applicationScope)

        // Background workers and notification channels
        applicationScope.launch(Dispatchers.IO) {
            NotificationHelper(this@Calender28Application).createNotificationChannels()
            MidnightRolloverWorker.scheduleNextMidnightRollover(this@Calender28Application)
            CalendarContentObserver.register(this@Calender28Application)
        }
    }
}

typealias MatrixApplication = Calender28Application
