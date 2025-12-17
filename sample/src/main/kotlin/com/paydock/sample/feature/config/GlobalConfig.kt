package com.paydock.sample.feature.config

import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AU_CURRENCY_CODE

/**
 * Global configuration that applies to all widgets and flows.
 */
data class GlobalConfig(
    val apiAccessToken: String = BuildConfig.ACCESS_TOKEN_API,
    val currencyCode: String = AU_CURRENCY_CODE
)
