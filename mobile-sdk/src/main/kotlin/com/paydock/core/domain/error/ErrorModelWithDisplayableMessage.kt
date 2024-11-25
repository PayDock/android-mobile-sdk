package com.paydock.core.domain.error

import com.paydock.core.MobileSDKConstants

/**
 * Provides a user-friendly displayable message for the current ErrorModel.
 * @return The displayable error message.
 */
val ErrorModel?.displayableMessage: String
    get() {
        return when (this) {
            is ErrorModel.ConnectionError -> {
                when (this) {
                    ErrorModel.ConnectionError.IOError -> MobileSDKConstants.Errors.IO_ERROR
                    ErrorModel.ConnectionError.Timeout -> MobileSDKConstants.Errors.SOCKET_TIMEOUT_ERROR
                    ErrorModel.ConnectionError.UnknownHost -> MobileSDKConstants.Errors.UNKNOWN_HOST_ERROR
                    else -> MobileSDKConstants.Errors.CONNECTION_ERROR
                }
            }

            is ErrorModel.SerializationError -> MobileSDKConstants.Errors.SERIALIZATION_ERROR
            is ErrorModel.CardDetailsError ->
                exception.message
                    ?: MobileSDKConstants.Errors.CARD_ERROR

            is ErrorModel.GiftCardError -> exception.message ?: MobileSDKConstants.Errors.CARD_ERROR
            is ErrorModel.ThreeDSError ->
                exception.message
                    ?: MobileSDKConstants.Errors.THREE_DS_ERROR

            is ErrorModel.PayPalError ->
                exception.message
                    ?: MobileSDKConstants.Errors.PAY_PAL_ERROR

            is ErrorModel.PayPalVaultError ->
                exception.message
                    ?: MobileSDKConstants.Errors.PAY_PAL_VAULT_ERROR

            is ErrorModel.PayPalDataCollectorError ->
                exception.message
                    ?: MobileSDKConstants.Errors.PAY_PAL_DATA_COLLECTOR_ERROR

            is ErrorModel.FlyPayError ->
                exception.message
                    ?: MobileSDKConstants.Errors.FLY_PAY_ERROR

            is ErrorModel.ClickToPayError ->
                exception.message
                    ?: MobileSDKConstants.Errors.CLICK_TO_PAY_ERROR

            is ErrorModel.GooglePayError ->
                exception.message
                    ?: MobileSDKConstants.Errors.GOOGLE_PAY_ERROR

            is ErrorModel.AfterpayError ->
                exception.message
                    ?: MobileSDKConstants.Errors.AFTER_PAY_ERROR

            is ErrorModel.UnknownError ->
                throwable.message
                    ?: MobileSDKConstants.Errors.AFTER_PAY_ERROR

            else -> MobileSDKConstants.Errors.DEFAULT_ERROR
        }
    }
