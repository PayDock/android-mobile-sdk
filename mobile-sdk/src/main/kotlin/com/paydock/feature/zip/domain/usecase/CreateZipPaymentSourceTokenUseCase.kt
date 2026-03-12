package com.paydock.feature.zip.domain.usecase

import com.paydock.core.domain.error.exceptions.ZipException
import com.paydock.core.extensions.suspendRunCatchingMapper
import com.paydock.feature.zip.domain.repository.ZipRepository

/**
 * Use case responsible for creating a payment source token from a Zip checkout token.
 *
 * This use case interacts with the [ZipRepository] to convert the checkout token
 * (received after user completes Zip checkout) into a payment source token.
 *
 * @param repository The repository that handles the token creation request.
 */
internal class CreateZipPaymentSourceTokenUseCase(private val repository: ZipRepository) {

    /**
     * Invokes the use case to create a payment source token.
     *
     * @param accessToken The access token used for authentication with the backend services.
     * @param checkoutToken The checkout token received from the Zip checkout flow.
     * @param gatewayId The gateway ID for processing the payment.
     * @return A [Result] containing the payment source token string on success,
     *         or a [ZipException.CreatingPaymentSourceTokenException] on failure.
     */
    suspend operator fun invoke(
        accessToken: String,
        checkoutToken: String,
        gatewayId: String
    ): Result<String> =
        suspendRunCatchingMapper(ZipException.CreatingPaymentSourceTokenException::class) {
            repository.createPaymentSourceToken(accessToken, checkoutToken, gatewayId)
        }
}
