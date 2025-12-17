package com.paydock.sample.feature.config.data

import com.paydock.sample.feature.config.GlobalConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing global configuration values.
 * This is a singleton that can be injected into ViewModels and other components.
 */
@Singleton
class GlobalConfigRepository @Inject constructor() {

    private val _globalConfig = MutableStateFlow(
        GlobalConfig()
    )

    val globalConfig: StateFlow<GlobalConfig> = _globalConfig.asStateFlow()

    fun updateAccessToken(accessToken: String) {
        _globalConfig.update { it.copy(apiAccessToken = accessToken) }
    }

    fun updateCartCurrency(currency: String) {
        _globalConfig.update { it.copy(currencyCode = currency) }
    }
}

