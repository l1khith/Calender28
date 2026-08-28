package com.l1khith.calender28.utils

import com.l1khith.calender28.BuildConfig

object AppConfig {
    const val APP_NAME = "Calender28"
    const val APP_VERSION_NAME = BuildConfig.VERSION_NAME
    const val APP_BUILD_NUMBER = BuildConfig.VERSION_CODE
    val APP_BUILD_INFO = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"

    const val PRIVACY_POLICY_URL = Constants.PRIVACY_POLICY_URL
    const val TERMS_OF_SERVICE_URL = Constants.TERMS_OF_SERVICE_URL
    const val DEVELOPER_EMAIL = Constants.DEVELOPER_EMAIL
}
