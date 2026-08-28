import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.l1khith.calender28"
    compileSdk = 37

    val secretsPropertiesFile = rootProject.file("secrets.properties")
    val secretsProperties = Properties()
    if (secretsPropertiesFile.exists()) {
        secretsProperties.load(FileInputStream(secretsPropertiesFile))
    }
    val revenueCatApiKey = secretsProperties.getProperty("REVENUECAT_API_KEY")
        ?: System.getenv("REVENUECAT_API_KEY")
        ?: ""

    defaultConfig {
        applicationId = "com.l1khith.calender28"
        minSdk = 24
        targetSdk = 37
        versionCode = 4
        versionName = "1.0.3"

        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatApiKey\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    val hasKeystoreProps = keystorePropertiesFile.exists()
    if (hasKeystoreProps) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            val keystorePath = keystoreProperties.getProperty("storeFile") 
                ?: System.getenv("KEYSTORE_FILE") 
                ?: "calender28-key"
            val resolvedFile = if (keystorePath.startsWith("/") || keystorePath.contains(":\\")) {
                file(keystorePath)
            } else {
                rootProject.file(keystorePath.removePrefix("../"))
            }

            if (resolvedFile.exists()) {
                storeFile = resolvedFile
                storePassword = keystoreProperties.getProperty("storePassword") ?: System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = keystoreProperties.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS") ?: ""
                keyPassword = keystoreProperties.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    val isSigningConfigured = hasKeystoreProps || (System.getenv("KEYSTORE_PASSWORD") != null && System.getenv("KEY_ALIAS") != null)

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (isSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.9.2")

    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.datastore:datastore-preferences:1.1.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    implementation("io.ktor:ktor-client-android:3.1.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-svg:2.7.0")

    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")

    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.google.android.gms:play-services-ads:24.2.0")

    implementation("com.revenuecat.purchases:purchases:10.16.2")
    implementation("com.revenuecat.purchases:purchases-ui:10.16.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.work:work-testing:2.10.0")
}