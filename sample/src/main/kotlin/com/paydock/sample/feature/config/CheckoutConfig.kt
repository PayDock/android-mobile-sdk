package com.paydock.sample.feature.config

import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.config.models.PaymentProcessor
import com.paydock.sample.feature.config.models.PaymentProcessorConfig
import com.paydock.sample.feature.config.models.ThreeDSService

/**
 * Checkout configuration that applies to the checkout flow.
 */
data class CheckoutConfig(
    val mpgsConfig: PaymentProcessorConfig = PaymentProcessorConfig(
        processor = PaymentProcessor.MPGS,
        serviceId = BuildConfig.SERVICE_ID_MPGS,
        threeDSService = ThreeDSService.GPAYMENTS
    ),
    val cyberSourceConfig: PaymentProcessorConfig = PaymentProcessorConfig(
        processor = PaymentProcessor.CYBERSOURCE,
        serviceId = BuildConfig.SERVICE_ID_CYBERSOURCE,
        threeDSService = ThreeDSService.GPAYMENTS
    ),
    val preferredProcessor: PaymentProcessor? = PaymentProcessor.MPGS // Default to MPGS
)

