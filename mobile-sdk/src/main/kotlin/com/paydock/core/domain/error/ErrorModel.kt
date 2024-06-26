package com.paydock.core.domain.error

import com.paydock.core.domain.error.exceptions.AfterPayException
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.domain.error.exceptions.MastercardSRCException
import com.paydock.core.domain.error.exceptions.PayPalException
import com.paydock.core.domain.error.exceptions.ThreeDSException
import com.paydock.core.domain.error.exceptions.UnknownApiException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Represents various types of errors that can occur within the application.
 */
sealed interface ErrorModel {

    /**
     * Connection Error: Represents different connection-related issues.
     */
    sealed class ConnectionError : ErrorModel {

        /** Timeout error for connection. */
        data object Timeout : ConnectionError()

        /** Input/Output error for connection. */
        data object IOError : ConnectionError()

        /** Unknown host error for connection. */
        data object UnknownHost : ConnectionError()
    }

    /**
     * Serialization Error: Indicates an error during serialization process.
     *
     * @property throwable The [Throwable] representing the serialization error.
     */
    data class SerializationError(val throwable: Throwable) : ErrorModel

    /**
     * Unknown Error: Represents an unknown error encountered.
     *
     * @property throwable The [Throwable] representing the unknown error.
     */
    data class UnknownError(val throwable: Throwable) : ErrorModel

    /**
     * 3DS Error: Represents errors specific to 3DS functionality.
     *
     * @property exception The [Exception] specific to 3DS.
     */
    data class ThreeDSError(val exception: ThreeDSException) : ErrorModel

    /**
     * PayPal Error: Represents errors specific to PayPal functionality.
     *
     * @property exception The [Exception] specific to PayPal.
     */
    data class PayPalError(val exception: PayPalException) : ErrorModel

    /**
     * FlyPay Error: Represents errors specific to FlyPay functionality.
     *
     * @property exception The [Exception] specific to FlyPay.
     */
    data class FlyPayError(val exception: FlyPayException) : ErrorModel

    /**
     * Mastercard SRC Error: Represents errors specific to Mastercard SRC functionality.
     *
     * @property exception The [Exception] specific to Mastercard SRC.
     */
    data class MastercardSRCError(val exception: MastercardSRCException) : ErrorModel

    /**
     * Google Pay Error: Represents errors specific to Google Pay functionality.
     *
     * @property exception The [Exception] specific to Google Pay.
     */
    data class GooglePayError(val exception: GooglePayException) : ErrorModel

    /**
     * Afterpay Error: Represents errors specific to Afterpay functionality.
     *
     * @property exception The [Exception] specific to Afterpay.
     */
    data class AfterPayError(val exception: AfterPayException) : ErrorModel

    /**
     * Gift Card Error: Represents errors specific to Gift Card tokenisation functionality.
     *
     * @property exception The [Exception] specific to Gift Card.
     */
    data class GiftCardError(val exception: GiftCardException) : ErrorModel

    /**
     * Card Details Error: Represents errors specific to Card tokenisation functionality.
     *
     * @property exception The [Exception] specific to Card details.
     */
    data class CardDetailsError(val exception: CardDetailsException) : ErrorModel
}

/**
 * Converts various types of [Throwable] instances to corresponding [ErrorModel] instances.
 * @return The appropriate [ErrorModel] instance based on the given Throwable.
 */
@Suppress("MaxLineLength", "CyclomaticComplexMethod")
fun Throwable.toError(): ErrorModel {
    return when (this) {
        is UnknownApiException -> ErrorModel.UnknownError(this)
        // Widget Exceptions
        is CardDetailsException -> ErrorModel.CardDetailsError(this)
        is GiftCardException -> ErrorModel.GiftCardError(this)
        is ThreeDSException -> ErrorModel.ThreeDSError(this)
        is PayPalException -> ErrorModel.PayPalError(this)
        is FlyPayException -> ErrorModel.FlyPayError(this)
        is MastercardSRCException -> ErrorModel.MastercardSRCError(this)
        is GooglePayException -> ErrorModel.GooglePayError(this)
        is AfterPayException -> ErrorModel.AfterPayError(this)
        // Generic
        is SocketTimeoutException -> ErrorModel.ConnectionError.Timeout
        is UnknownHostException -> ErrorModel.ConnectionError.UnknownHost
        is IOException -> ErrorModel.ConnectionError.IOError
        is SerializationException -> ErrorModel.SerializationError(this)
        else -> ErrorModel.UnknownError(this)
    }
}