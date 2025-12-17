package com.paydock.sample.feature.config.data

import com.paydock.sample.feature.config.WidgetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing widget configuration values.
 * This is a singleton that can be injected into ViewModels and other components.
 */
@Singleton
class WidgetConfigRepository @Inject constructor() {

    private val _widgetConfig = MutableStateFlow(
        WidgetConfig()
    )

    val widgetConfig: StateFlow<WidgetConfig> = _widgetConfig.asStateFlow()

    fun updateCartAmount(amount: BigDecimal) {
        _widgetConfig.update { it.copy(cartAmount = amount) }
    }
}

