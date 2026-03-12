package com.paydock.sample.feature.config

import com.paydock.sample.core.AMOUNT
import java.math.BigDecimal

/**
 * Widget configuration that applies to widget-specific settings.
 */
data class WidgetConfig(
    val cartAmount: BigDecimal = BigDecimal(AMOUNT)
)

