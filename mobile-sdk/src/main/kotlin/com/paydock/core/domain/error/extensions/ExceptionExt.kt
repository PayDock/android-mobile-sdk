package com.paydock.core.domain.error.extensions

import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.GenericException
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.network.exceptions.ApiException
import com.paydock.core.network.exceptions.UnknownApiException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Maps an exception to a corresponding API-related exception or a generic exception.
 *
 * This function provides a structured way to handle and transform different types of exceptions
 * into custom exceptions, improving the granularity and readability of error handling in the SDK.
 * It uses the `E` type parameter to specify an expected exception type.
 *
 * @param E The expected exception type to handle, typically a custom exception subclass.
 * @receiver Exception The exception to be mapped.
 * @return A [Throwable] that corresponds to the mapped exception type.
 *
 * ### Exception Mapping:
 * - **[SocketTimeoutException]**: Mapped to [GenericException.TimeoutException].
 * - **[UnknownHostException]**: Mapped to [GenericException.ConnectionException].
 * - **[SerializationException]**: Mapped to [GenericException.DataParsingException].
 * - **[IOException]**: Mapped to [GenericException.GeneralException].
 * - **[AfterpayException]**: Delegates mapping to `mapAfterpayApiException`.
 * - **[CardDetailsException]**: Delegates mapping to `mapCardDetailsApiException`.
 * - **[PayPalException]**: Delegates mapping to `mapPayPalApiException`.
 * - **[PayPalVaultException]**: Delegates mapping to `mapPayPalVaultException`.
 * - **Fallback**: Maps to [GenericException.UnknownException] if no match is found.
 *
 * ### Example Usage:
 * ```kotlin
 * try {
 *     // API call or some operation that may throw exceptions
 * } catch (e: Exception) {
 *     val mappedException = e.mapApiException<CustomExceptionType>()
 *     // Handle the mapped exception
 * }
 * ```
 */
internal inline fun <reified E : Exception> Throwable.mapApiException(): SdkException {
    // Generic Exception Mapping
    val genericException = this.mapGenericExceptions()
    if (genericException != null) {
        return genericException
    }
    return when {
        // TODO - Add all other widget type exceptions here!
        AfterpayException::class.java.isAssignableFrom(E::class.java) ->
            this.mapAfterpayApiException<E>()
        CardDetailsException::class.java.isAssignableFrom(E::class.java) ->
            this.mapCardDetailsApiException<E>()
        GiftCardException::class.java.isAssignableFrom(E::class.java) ->
            this.mapGiftCardDetailsApiException<E>()
        PayPalException::class.java.isAssignableFrom(E::class.java) ->
            this.mapPayPalApiException<E>()
        PayPalVaultException::class.java.isAssignableFrom(E::class.java) ->
            this.mapPayPalVaultApiException<E>()
        // Add other mappings for different exception types using similar patterns
        else -> GenericException.UnknownException(
            "No suitable exception mapping of [${this::class.java.simpleName}] " +
                "found for ${E::class.simpleName}"
        )
    }
}

/**
 * Maps common exceptions to corresponding `GenericException` types for standardized error handling.
 *
 * This function evaluates a `Throwable` and maps it to a specific `GenericException` type based on its
 * class. If no matching mapping is found, it returns `null`.
 *
 * @return A `GenericException` representing the mapped error, or `null` if no mapping exists.
 */
internal fun Throwable.mapGenericExceptions(): SdkException? {
    return when {
        // Maps a socket timeout error to a `TimeoutException`.
        this is SocketTimeoutException -> GenericException.TimeoutException(
            MobileSDKConstants.Errors.SOCKET_TIMEOUT_ERROR
        )

        // Maps an unknown host error to a `ConnectionException`.
        this is UnknownHostException -> GenericException.ConnectionException(
            MobileSDKConstants.Errors.UNKNOWN_HOST_ERROR
        )

        // Maps a serialization error to a `DataParsingException`.
        this is SerializationException -> GenericException.DataParsingException(
            MobileSDKConstants.Errors.SERIALIZATION_ERROR
        )

        // Maps non-API-related `IOException` instances to a `GeneralException`.
        !(this is ApiException || this is UnknownApiException) && this is IOException ->
            GenericException.GeneralException(
                MobileSDKConstants.Errors.IO_ERROR
            )

        // Returns null if no mapping is available for the given exception type.
        else -> null
    }
}

/**
 * Maps a generic `Throwable` to a specific type of `PayPalVaultException` based on the provided reified exception type.
 *
 * This function handles known `ApiException` and `UnknownApiException` types, converting them into
 * corresponding `PayPalVaultException` subclasses. For other exceptions, a default `PayPalVaultException.UnknownException`
 * is returned.
 *
 * @param E The target type of `PayPalVaultException` to map the throwable to. This is a reified type parameter, allowing the function
 *          to infer the target exception type at runtime.
 * @receiver The throwable to map.
 * @return A `PayPalVaultException` instance that represents the mapped exception.
 *
 * ## Behavior:
 * - **`ApiException` Handling**:
 *   - Maps to:
 *     - `PayPalVaultException.CreateSessionAuthTokenException` if `E` matches this type.
 *     - `PayPalVaultException.CreateSetupTokenException` if `E` matches this type.
 *     - `PayPalVaultException.GetPayPalClientIdException` if `E` matches this type.
 *     - `PayPalVaultException.CreatePaymentTokenException` if `E` matches this type.
 *   - Defaults to `PayPalVaultException.UnknownException` for other `ApiException` cases.
 * - **`UnknownApiException` Handling**:
 *   - Maps to `PayPalVaultException.UnknownException` with the error message from the `UnknownApiException`.
 * - **Fallback Handling**:
 *   - Maps other exception types to `PayPalVaultException.UnknownException` with a generic error message.
 *
 * ## Example Usage:
 * ```kotlin
 * val apiException = ApiException(error = "Invalid setup token")
 * val mappedException: PayPalVaultException = apiException.mapPayPalVaultApiException<PayPalVaultException.CreateSetupTokenException>()
 * println(mappedException) // Output: PayPalVaultException.CreateSetupTokenException(error="Invalid setup token")
 * ```
 *
 * ## Notes:
 * - Ensure that the type parameter `E` is a subclass of `Throwable`.
 * - Use this mapping function to consistently convert low-level exceptions into higher-level domain exceptions.
 */
internal inline fun <reified E : Throwable> Throwable.mapPayPalVaultApiException(): PayPalVaultException =
    when (this) {
        is ApiException -> {
            when (E::class) {
                PayPalVaultException.CreateSetupTokenException::class ->
                    PayPalVaultException.CreateSetupTokenException(error = this.error)

                PayPalVaultException.GetPayPalClientIdException::class ->
                    PayPalVaultException.GetPayPalClientIdException(error = this.error)

                PayPalVaultException.CreatePaymentTokenException::class ->
                    PayPalVaultException.CreatePaymentTokenException(error = this.error)

                else -> PayPalVaultException.UnknownException(
                    displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
                )
            }
        }

        is UnknownApiException -> PayPalVaultException.UnknownException(displayableMessage = this.errorMessage)

        else -> PayPalVaultException.UnknownException(
            displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
        )
    }

/**
 * Maps a generic `Throwable` to a specific type of `PayPalException` based on the provided reified exception type.
 *
 * This function handles known `ApiException` and `UnknownApiException` types, converting them into
 * corresponding `PayPalException` subclasses. For other exceptions, a default `PayPalException.UnknownException`
 * is returned.
 *
 * @param E The target type of `PayPalException` to map the throwable to. This is a reified type parameter, allowing the function
 *          to infer the target exception type at runtime.
 * @receiver The throwable to map.
 * @return A `PayPalException` instance that represents the mapped exception.
 *
 * ## Behavior:
 * - **`ApiException` Handling**:
 *   - Maps to `PayPalException.CapturingChargeException` if `E` is `PayPalException.CapturingChargeException`.
 *   - Maps to `PayPalException.FetchingUrlException` if `E` is `PayPalException.FetchingUrlException`.
 *   - Defaults to `PayPalException.UnknownException` for other `ApiException` cases.
 * - **`UnknownApiException` Handling**:
 *   - Maps to `PayPalException.UnknownException` with the error message from the `UnknownApiException`.
 * - **Fallback Handling**:
 *   - Maps other exception types to `PayPalException.UnknownException` with a generic error message.
 *
 * ## Example Usage:
 * ```kotlin
 * val apiException = ApiException(error = "Invalid token")
 * val mappedException: PayPalException = apiException.mapPayPalApiException<PayPalException.CapturingChargeException>()
 * println(mappedException) // Output: PayPalException.CapturingChargeException(error="Invalid token")
 * ```
 *
 * ## Notes:
 * - Ensure that the type parameter `E` is a subclass of `Throwable`.
 */
internal inline fun <reified E : Throwable> Throwable.mapPayPalApiException(): PayPalException =
    when (this) {
        is ApiException -> {
            when (E::class) {
                PayPalException.CapturingChargeException::class ->
                    PayPalException.CapturingChargeException(error = this.error)

                PayPalException.FetchingUrlException::class ->
                    PayPalException.FetchingUrlException(error = this.error)

                else -> PayPalException.UnknownException(
                    displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
                )
            }
        }

        is UnknownApiException -> PayPalException.UnknownException(displayableMessage = this.errorMessage)

        else -> PayPalException.UnknownException(
            displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
        )
    }

/**
 * Maps a `Throwable` to a specific `CardDetailsException` subtype based on the provided type parameter `E`.
 *
 * This function is designed to handle exceptions thrown during API calls related to card details processing.
 * It identifies the type of exception (`ApiException`, `UnknownApiException`, or other `Throwable`)
 * and maps it to an appropriate `CardDetailsException` subtype.
 *
 * @param E The specific subtype of `Throwable` expected for handling (e.g., `CardDetailsException.TokenisingCardException`).
 * @return A `CardDetailsException` that represents the mapped exception.
 *
 * ## Mapping Rules:
 * - If the `Throwable` is of type `ApiException`:
 *   - Maps to `CardDetailsException.TokenisingCardException` if `E` is `TokenisingCardException`,
 *     with the `error` property propagated from the `ApiException`.
 *   - Otherwise, maps to `CardDetailsException.UnknownException` with a generic error message.
 * - If the `Throwable` is of type `UnknownApiException`:
 *   - Maps to `CardDetailsException.UnknownException` with the `errorMessage` from the `UnknownApiException`.
 * - For all other cases:
 *   - Maps to `CardDetailsException.UnknownException` with a generic message derived from the `Throwable`'s `message` property.
 *
 * ## Example Usage:
 * ```kotlin
 * try {
 *     // Code that may throw an exception
 * } catch (e: Throwable) {
 *     val mappedException = e.mapCardDetailsApiException<CardDetailsException.TokenisingCardException>()
 *     handleException(mappedException)
 * }
 * ```
 *
 * ## Exception Types:
 * - `ApiException`: Represents known API-related exceptions.
 * - `UnknownApiException`: Represents an unknown API error with an error message.
 * - `CardDetailsException.TokenisingCardException`: Represents a specific error during card tokenization.
 * - `CardDetailsException.UnknownException`: Represents all other errors with a generic message.
 */
internal inline fun <reified E : Throwable> Throwable.mapCardDetailsApiException(): CardDetailsException =
    when (this) {
        is ApiException -> {
            when (E::class) {
                CardDetailsException.TokenisingCardException::class ->
                    CardDetailsException.TokenisingCardException(error = this.error)

                else -> CardDetailsException.UnknownException(
                    displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
                )
            }
        }

        is UnknownApiException -> CardDetailsException.UnknownException(displayableMessage = this.errorMessage)

        else -> CardDetailsException.UnknownException(
            displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
        )
    }

/**
 * Maps a `Throwable` to a specific type of `AfterpayException` based on its type and the provided generic parameter.
 *
 * This function is designed to handle exceptions that occur during API interactions with Afterpay,
 * converting them into specific, developer-defined exceptions (`AfterpayException`) for better error handling.
 *
 * It supports differentiating between different subtypes of `ApiException` and maps them
 * to corresponding subclasses of `AfterpayException`. If no specific mapping is found,
 * it defaults to an `UnknownException`.
 *
 * @param E The expected type of the `AfterpayException` to map to. This is inferred at compile time.
 * @receiver The original exception (`Throwable`) to be mapped.
 * @return An `AfterpayException` that represents the mapped exception.
 */
internal inline fun <reified E : Throwable> Throwable.mapAfterpayApiException(): AfterpayException =
    when (this) {
        // Handle cases where the exception is of type ApiException
        is ApiException -> {
            when (E::class) {
                AfterpayException.CapturingChargeException::class ->
                    AfterpayException.CapturingChargeException(error = this.error)

                AfterpayException.FetchingUrlException::class ->
                    AfterpayException.FetchingUrlException(error = this.error)

                else -> AfterpayException.UnknownException(
                    displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
                )
            }
        }

        // Handle cases where the exception is of type UnknownApiException
        is UnknownApiException -> AfterpayException.UnknownException(
            displayableMessage = this.errorMessage
        )

        // Default case for any other types of exceptions
        else -> AfterpayException.UnknownException(
            displayableMessage = this.message ?: "An unknown error occurred"
        )
    }

/**
 * Maps a throwable to a specific type of `GiftCardException` based on the provided exception type `E`.
 *
 * This function is designed to handle API exceptions and map them to the corresponding
 * `GiftCardException`, ensuring meaningful error information is propagated to the application.
 * If the throwable does not match any specific exception type, it defaults to an `UnknownException`.
 *
 * @param E The type of `GiftCardException` to map to.
 * @return A `GiftCardException` instance that corresponds to the throwable.
 *
 * @throws GiftCardException.TokenisingCardException If the exception is related to card tokenization errors.
 * @throws GiftCardException.UnknownException For all other types of exceptions, including unknown API errors.
 */
internal inline fun <reified E : Throwable> Throwable.mapGiftCardDetailsApiException(): GiftCardException =
    when (this) {
        is ApiException -> {
            when (E::class) {
                GiftCardException.TokenisingCardException::class ->
                    GiftCardException.TokenisingCardException(error = this.error)

                else -> GiftCardException.UnknownException(
                    displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
                )
            }
        }

        is UnknownApiException -> GiftCardException.UnknownException(
            displayableMessage = this.errorMessage
        )

        else -> GiftCardException.UnknownException(
            displayableMessage = this.message ?: MobileSDKConstants.Errors.DEFAULT_ERROR
        )
    }
