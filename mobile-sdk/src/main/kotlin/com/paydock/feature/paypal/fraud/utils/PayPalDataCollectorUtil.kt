package com.paydock.feature.paypal.fraud.utils

import android.content.Context
import com.paydock.MobileSDK
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.PayPalDataCollectorException
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.domain.mapper.mapToPayPalEnv
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.paypal.fraud.domain.model.integration.PayPalDataCollectorConfig
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.fraudprotection.PayPalDataCollector
import com.paypal.android.fraudprotection.PayPalDataCollectorRequest
import org.koin.mp.KoinPlatform.getKoin
import kotlin.jvm.Throws

/**
 * A utility class for collecting PayPal device data within an SDK.
 * This class handles initialization and interaction with the PayPal SDK,
 * using provided configuration and dependency injection for internal use.
 *
 * @property config The configuration object containing access token and gateway ID for PayPal interactions.
 */
class PayPalDataCollectorUtil private constructor(
    val config: PayPalDataCollectorConfig,
) {
    // Lazily loaded dependencies for dispatchers and use cases
    private val getPayPalClientIdUseCase: GetPayPalClientIdUseCase by lazy { getKoin().get<GetPayPalClientIdUseCase>() }
    private lateinit var paypalCollector: PayPalDataCollector

    /**
     * Asynchronous initialization of the PayPalDataCollector.
     * Retrieves the client ID via a use case and initializes the PayPal collector.
     *
     * @throws Exception if client ID retrieval fails or another error occurs.
     */
    private suspend fun initialiseCollector() {
        val clientId = getPayPalClientIdUseCase(config.accessToken, config.gatewayId)
            .getOrElse { throw it } // This will throw the exception if there's an error in the result

        paypalCollector = PayPalDataCollector(buildPayPalCoreConfig(clientId)).apply {
            setLogging(MobileSDK.getInstance().enableTestMode)
        }
    }

    /**
     * Builds the PayPal core configuration using the provided client ID.
     *
     * @param clientId The PayPal client ID.
     * @return A CoreConfig object containing the necessary PayPal configuration.
     */
    private fun buildPayPalCoreConfig(clientId: String): CoreConfig {
        return CoreConfig(
            clientId = clientId,
            environment = MobileSDK.getInstance().environment.mapToPayPalEnv()
        )
    }

    companion object {

        /**
         * Factory method to create and initialize a PayPalDataCollector instance.
         * This method blocks the calling thread until initialization completes or fails with a timeout.
         *
         * @param config Configuration for the PayPalDataCollector.
         * @return An initialized PayPalDataCollector instance.
         * @throws PayPalDataCollectorException If initialization fails due to a timeout, client ID retrieval, or unknown error.
         */
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        @Throws(PayPalDataCollectorException::class)
        suspend fun initialise(
            config: PayPalDataCollectorConfig,
        ): PayPalDataCollectorUtil {
            try {
                val collector = PayPalDataCollectorUtil(config)
                collector.initialiseCollector()
                return collector
            } catch (e: PayPalVaultException.GetPayPalClientIdException) {
                throw PayPalDataCollectorException.InitialisationClientIdException(e.error)
            } catch (e: Exception) {
                // This is just a fallback in the event that some other error occurs
                throw PayPalDataCollectorException.UnknownException(
                    e.message ?: MobileSDKConstants.Errors.PAY_PAL_DATA_COLLECTOR_UNKNOWN_ERROR
                )
            }
        }
    }

    /**
     * Collects device information using the PayPalDataCollector, if it has been initialized.
     *
     * @param context The application context.
     * @param hasUserLocationConsent Boolean indicating if user location consent is provided (optional).
     * @param clientMetadataId Optional client metadata ID for tracking.
     * @param additionalData Additional optional data for collection.
     * @return A string representing collected device data, or null if the collector is not initialized.
     */
    fun collectDeviceId(
        context: Context,
        hasUserLocationConsent: Boolean = false,
        clientMetadataId: String? = null,
        additionalData: Map<String, String>? = null,
    ): String? {
        // This should always succeed, but just added to cater for lateinit
        return if (::paypalCollector.isInitialized) {
            val request = PayPalDataCollectorRequest(
                hasUserLocationConsent = hasUserLocationConsent,
                clientMetadataId = clientMetadataId,
                additionalData = additionalData
            )
            paypalCollector.collectDeviceData(context = context, request = request)
        } else {
            null
        }
    }
}