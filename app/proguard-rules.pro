# -----------------------------------------------------------------------------
# Calender28 ProGuard / R8 Optimization & Obfuscation Rules
# -----------------------------------------------------------------------------

# Keep Attributes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# -----------------------------------------------------------------------------
# Application & App Packages
# -----------------------------------------------------------------------------
-keep class com.l1khith.calender28.MatrixApplication { *; }
-keep class com.l1khith.calender28.MainActivity { *; }
-keep class com.l1khith.calender28.data.** { *; }
-keep class com.l1khith.calender28.repository.** { *; }
-keep class com.l1khith.calender28.viewmodel.** { *; }
-keep class com.l1khith.calender28.service.** { *; }
-keep class com.l1khith.calender28.receiver.** { *; }
-keep class com.l1khith.calender28.widget.** { *; }
-keep class com.l1khith.calender28.billing.** { *; }
-keep class com.l1khith.calender28.utils.** { *; }

# -----------------------------------------------------------------------------
# Room Database
# -----------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.migration.Migration { *; }
-dontwarn androidx.room.paging.**

# -----------------------------------------------------------------------------
# WorkManager
# -----------------------------------------------------------------------------
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# -----------------------------------------------------------------------------
# Glance App Widget
# -----------------------------------------------------------------------------
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# -----------------------------------------------------------------------------
# Jetpack Compose & Kotlin Coroutines
# -----------------------------------------------------------------------------
-keep class androidx.compose.material3.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# -----------------------------------------------------------------------------
# RevenueCat & Google Play Billing
# -----------------------------------------------------------------------------
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# -----------------------------------------------------------------------------
# Google Play Services & Ads
# -----------------------------------------------------------------------------
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# -----------------------------------------------------------------------------
# DataStore & Serialization
# -----------------------------------------------------------------------------
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }
-dontwarn kotlinx.serialization.**
