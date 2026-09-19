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
import com.l1khith.calender28.data.user.UserIdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import android.app.Activity
import android.os.Bundle
import java.lang.ref.WeakReference

class Calender28Application : Application() {

    companion object {
        var currentActivity: WeakReference<Activity>? = null
            private set
    }

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                currentActivity = WeakReference(activity)
            }
            override fun onActivityPaused(activity: Activity) {
                if (currentActivity?.get() == activity) {
                    currentActivity = null
                }
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity?.get() == activity) {
                    currentActivity = null
                }
            }
        })

        // AdMob — initialize on background thread to avoid blocking startup
        applicationScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@Calender28Application) { }
        }

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

        // Generate UUID, timestamp, and display name on first launch, reuse forever after
        applicationScope.launch(Dispatchers.IO) {
            UserIdManager.getOrCreate(this@Calender28Application)
            UserIdManager.getOrCreateCreationTimestamp(this@Calender28Application)
            UserIdManager.getOrCreateDisplayName(this@Calender28Application)
            NotificationHelper(this@Calender28Application).createNotificationChannels()
            MidnightRolloverWorker.scheduleNextMidnightRollover(this@Calender28Application)
            CalendarContentObserver.register(this@Calender28Application)
        }
    }
}

typealias MatrixApplication = Calender28Application
