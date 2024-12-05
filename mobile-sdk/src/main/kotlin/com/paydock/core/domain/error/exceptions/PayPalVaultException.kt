package com.paydock.core.domain.error.exceptions

import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage
import java.io.IOException

/**
 * A sealed class representing exceptions that can occur during the PayPal vault operations.
 *
 * This class extends [IOException] to provide a specific hierarchy of exceptions related
 * to PayPal vault interactions. Each exception includes a displayable message that can
 * be shown to the user, which provides context about the error.
 *
 * @param displayableMessage The message that can be displayed to the user for this exception.
 */
sealed class PayPalVaultException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error creating a setup token.
     *
     * @param error An [ApiErrorResponse] containing details about the error.
     */
    data class CreateSetupTokenException(
        val error: ApiErrorResponse
    ) : PayPalVaultException(error.displayableMessage)

    /**
     * Exception thrown when there is an error retrieving the PayPal client ID.
     *
     * @param error An [ApiErrorResponse] containing details about the error.
     */
    data class GetPayPalClientIdException(
        val error: ApiErrorResponse
    ) : PayPalVaultException(error.displayableMessage)

    /**
     * Exception thrown when there is an error creating a payment token.
     *
     * @param error An [ApiErrorResponse] containing details about the error.
     */
    data class CreatePaymentTokenException(
        val error: ApiErrorResponse
    ) : PayPalVaultException(error.displayableMessage)

    /**
     * Represents an exception encountered during PayPal SDK operations.
     *
     * This exception holds detailed error information provided by the PayPal SDK, such as error codes
     * and descriptions. It extends the [PayPalVaultException] to fit into the existing exception hierarchy
     * for vaulting-related processes.
     *
     * @property error The detailed error information returned by the PayPal SDK.
     */
    data class PayPalSDKException(
        val code: Int,
        val description: String
    ) : PayPalVaultException(description)

    /**
     * Represents a cancellation exception during the PayPal vaulting process.
     *
     * This exception is thrown when a user or the system cancels the vaulting operation. It provides a
     * user-friendly message that can be displayed in the UI to indicate that the process was cancelled.
     *
     * @param displayableMessage The message that describes the reason for the cancellation, typically
     *        shown to the user.
     */
    class CancellationException(displayableMessage: String) : PayPalVaultException(displayableMessage)

    /**
     * Exception thrown when an unknown error occurs in PayPal vault operations.
     *
     * @param displayableMessage The message that can be displayed to the user for this exception.
     */
    class UnknownException(displayableMessage: String) : PayPalVaultException(displayableMessage)
}