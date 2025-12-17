package com.paydock.sample.feature.config.data

import com.paydock.sample.feature.config.CheckoutConfig
import com.paydock.sample.feature.config.models.PaymentProcessor
import com.paydock.sample.feature.config.models.PaymentProcessorConfig
import com.paydock.sample.feature.config.models.ThreeDSService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing checkout configuration values.
 * This is a singleton that can be injected into ViewModels and other components.
 */
@Singleton
class CheckoutConfigRepository @Inject constructor() {

    private val _checkoutConfig = MutableStateFlow(
        CheckoutConfig(
            preferredProcessor = PaymentProcessor.MPGS // Default to MPGS
        )
    )

    val checkoutConfig: StateFlow<CheckoutConfig> = _checkoutConfig.asStateFlow()

    fun updateMpgsConfig(config: PaymentProcessorConfig) {
        _checkoutConfig.update { it.copy(mpgsConfig = config) }
    }

    fun updateCyberSourceConfig(config: PaymentProcessorConfig) {
        _checkoutConfig.update { it.copy(cyberSourceConfig = config) }
    }

    fun updatePreferredProcessor(processor: PaymentProcessor?) {
        _checkoutConfig.update { it.copy(preferredProcessor = processor) }
    }

    fun updateMpgsServiceId(serviceId: String) {
        _checkoutConfig.update { current ->
            current.copy(
                mpgsConfig = current.mpgsConfig.copy(serviceId = serviceId)
            )
        }
    }

    fun updateMpgsThreeDSService(service: ThreeDSService) {
        _checkoutConfig.update { current ->
            current.copy(
                mpgsConfig = current.mpgsConfig.copy(threeDSService = service)
            )
        }
    }

    fun updateCyberSourceServiceId(serviceId: String) {
        _checkoutConfig.update { current ->
            current.copy(
                cyberSourceConfig = current.cyberSourceConfig.copy(serviceId = serviceId)
            )
        }
    }

    fun updateCyberSourceThreeDSService(service: ThreeDSService) {
        _checkoutConfig.update { current ->
            current.copy(
                cyberSourceConfig = current.cyberSourceConfig.copy(threeDSService = service)
            )
        }
    }
}

