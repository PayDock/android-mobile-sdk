package com.paydock.sample.feature.config.models

enum class PaymentProcessor {
    MPGS,
    CYBERSOURCE
}

enum class ThreeDSService {
    INTEGRATED_3DS,
    GPAYMENTS
}

data class PaymentProcessorConfig(
    val processor: PaymentProcessor,
    val serviceId: String = "",
    val threeDSService: ThreeDSService = ThreeDSService.INTEGRATED_3DS
)

