package com.paydock.feature.zip.domain.usecase

import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.extensions.suspendRunCatchingMapper
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.feature.zip.domain.repository.ZipRepository

/**
 * Use case responsible for initializing a Zip checkout session.
 *
 * This use case interacts with the [ZipRepository] to create an external checkout session
 * and retrieve the checkout URL and token needed for the Zip payment flow.
 *
 * @param repository The repository that handles the checkout initialization request.
 */
internal class InitializeZipCheckoutUseCase(private val repository: ZipRepository) {

    /**
     * Invokes the use case to initialize a Zip checkout session.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param config The [ZipWidgetConfig] containing the checkout details.
     * @return A [Result] containing a pair of (checkoutUrl, checkoutToken) on success,
     *         or a [ZipException.FetchingCheckoutUrlException] on failure.
     */
    suspend operator fun invoke(
        accessToken: String,
        config: ZipWidgetConfig
    ): Result<Pair<String, String>> =
        suspendRunCatchingMapper(ZipException.FetchingCheckoutUrlException::class) {
            repository.initializeExternalCheckout(accessToken, config)
        }
}
