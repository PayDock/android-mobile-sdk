package com.paydock.sample.feature.config.data

import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.feature.config.GlobalConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing global configuration values.
 * This is a singleton that can be injected into ViewModels and other components.
 */
@Singleton
class GlobalConfigRepository @Inject constructor() {

    private val _globalConfig = MutableStateFlow(
        GlobalConfig(
            apiAccessToken = BuildConfig.ACCESS_TOKEN_API,
            cartAmount = BigDecimal(AMOUNT),
            cartCurrency = AU_CURRENCY_CODE
        )
    )

    val globalConfig: StateFlow<GlobalConfig> = _globalConfig.asStateFlow()

    fun updateAccessToken(accessToken: String) {
        _globalConfig.update { it.copy(apiAccessToken = accessToken) }
    }

    fun updateCartAmount(amount: BigDecimal) {
        _globalConfig.update { it.copy(cartAmount = amount) }
    }

    fun updateCartCurrency(currency: String) {
        _globalConfig.update { it.copy(cartCurrency = currency) }
    }
}

