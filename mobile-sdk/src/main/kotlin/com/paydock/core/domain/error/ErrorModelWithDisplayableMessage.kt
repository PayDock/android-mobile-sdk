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
                    ErrorModel.ConnectionError.IOError -> MobileSDKConstants.Network.Errors.IO_ERROR
                    ErrorModel.ConnectionError.Timeout -> MobileSDKConstants.Network.Errors.SOCKET_TIMEOUT_ERROR
                    ErrorModel.ConnectionError.UnknownHost -> MobileSDKConstants.Network.Errors.UNKNOWN_HOST_ERROR
                }
            }

            is ErrorModel.SerializationError -> MobileSDKConstants.General.Errors.SERIALIZATION_ERROR
            is ErrorModel.CardDetailsError ->
                exception.message
                    ?: MobileSDKConstants.CardDetailsConfig.Errors.CARD_ERROR

            is ErrorModel.GiftCardError -> exception.message ?: MobileSDKConstants.CardDetailsConfig.Errors.CARD_ERROR
            is ErrorModel.Standalone3DSError ->
                exception.message
                    ?: MobileSDKConstants.Standalone3DSConfig.Errors.STANDALONE_3DS_ERROR

            is ErrorModel.Integrated3DSError ->
                exception.message
                    ?: MobileSDKConstants.Integrated3DSConfig.Errors.INTEGRATED_3DS_ERROR

            is ErrorModel.PayPalError ->
                exception.message
                    ?: MobileSDKConstants.PayPalConfig.Errors.PAY_PAL_ERROR

            is ErrorModel.PayPalVaultError ->
                exception.message
                    ?: MobileSDKConstants.PayPalVaultConfig.Errors.VAULT_ERROR

            is ErrorModel.PayPalDataCollectorError ->
                exception.message
                    ?: MobileSDKConstants.PayPalVaultConfig.Errors.DATA_COLLECTOR_ERROR

            is ErrorModel.ColesPayError ->
                exception.message
                    ?: MobileSDKConstants.ColesPayConfig.Errors.COLES_PAY_ERROR

            is ErrorModel.ClickToPayError ->
                exception.message
                    ?: MobileSDKConstants.ClickToPayConfig.Errors.CLICK_TO_PAY_ERROR

            is ErrorModel.GooglePayError ->
                exception.message
                    ?: MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_ERROR

            is ErrorModel.AfterpayError ->
                exception.message
                    ?: MobileSDKConstants.AfterpayConfig.Errors.AFTER_PAY_ERROR

            is ErrorModel.UnknownError ->
                throwable.message
                    ?: MobileSDKConstants.General.Errors.DEFAULT_ERROR

            else -> MobileSDKConstants.General.Errors.DEFAULT_ERROR
        }
    }
