package com.paydock.core

import com.paydock.BuildConfig

/**
 * Constants related to the Mobile SDK configuration.
 */
internal object MobileSDKConstants {
    const val MOBILE_SDK_TAG = "[MobileSDK]"
    const val JS_BRIDGE_NAME = "PayDockMobileSDK"
    const val DEFAULT_WEB_URL = "https://paydock.com/"

    /**
     * Constants related to the UI.
     */
    object General {
        // UI Constants
        internal const val INPUT_DELAY = 300L
        internal const val DEBOUNCE_DELAY = 500L

        // Animation Constants
        internal const val DEFAULT_ANIMATION_DURATION = 300
        internal const val EXPANSION_TRANSITION_DURATION = 400

        object Errors {
            const val SERIALIZATION_ERROR =
                "Troubles with data serialization. Please try again later or contact support for assistance."
            const val DEFAULT_ERROR = "An unknown error occurred. Please try again later."
        }
    }

    /**
     * A utility object containing reusable regular expressions for input validation.
     */
    object Regex {
        /**
         * A regular expression that matches numeric strings containing only digits (0-9).
         *
         * This regex ensures that the input consists entirely of numeric characters,
         * with no spaces, letters, or special characters.
         *
         * Example:
         * - Valid: "12345", "007"
         * - Invalid: "123a", "12 34", "!123"
         */
        internal val NUMERIC_DIGITS = Regex("^[0-9]+$")
    }

    /**
     * Constants related to the Network.
     */
    object Network {
        // sha256 hssh for paydock base urls
        internal const val SSH_HASH = "sha256/kV0cxZABuhXdMFROcAZwIflgJilKOqxMBcRhzFhZMok="

        object Errors {
            const val IO_ERROR =
                "An error occurred while communicating with the server. Please check your internet connection and try again."
            const val SOCKET_TIMEOUT_ERROR =
                "The connection timed out. Please check your internet connection and try again."
            const val UNKNOWN_HOST_ERROR =
                "The server could not be found. Please check your internet connection and try again."
        }
    }

    /**
     * Constants related to the Base URL.
     */
    object BaseUrls {
        internal const val PRODUCTION_BASE_URL = "api.paydock.com"
        internal const val SANDBOX_BASE_URL = "api-sandbox.paydock.com"
        internal const val STAGING_BASE_URL = "apista.paydock.com"
    }

    /**
     * Constants related to card details.
     */
    object CardDetailsConfig {
        internal const val CARD_NUMBER_SECTION_SIZE = 4
        internal const val MIN_CREDIT_CARD_LENGTH = 12
        internal const val MAX_CREDIT_CARD_LENGTH = 19
        internal const val MIN_GIFT_CARD_LENGTH = 14
        internal const val MIN_GIFT_CARD_PIN_LENGTH = 4
        internal const val MAX_GIFT_CARD_LENGTH = 25
        internal const val MAX_EXPIRY_LENGTH = 4
        internal const val EXPIRY_CHUNK_SIZE = 2
        internal const val CVV_CVC_LENGTH = 3
        internal const val CID3_LENGTH = 3
        internal const val CID_LENGTH = 4
        internal const val EXPIRY_BASE_YEAR = 2000
        internal const val MAX_MONTH_COUNT = 12
        internal const val DEFAULT_ACTION_TEXT = "Submit"
        internal const val DEFAULT_CONSENT_TEXT = "Remember this card for next time."
        internal const val DEFAULT_POLICY_TEXT = "Read our privacy policy"
        internal const val FONT_SCALE_THRESHOLD = 2.0f

        object Errors {
            const val CARD_ERROR =
                "An unexpected error occurred while tokenising card details. Please try again later or contact support for assistance."
        }
    }

    /**
     * Constants related to address configuration.
     */
    object AddressConfig {
        internal const val MAX_SEARCH_RESULTS = 5

        object Errors
    }

    /**
     * Constants related to 3D Secure (3DS) configuration.
     *
     * This object holds constants specific to the 3DS verification process.
     */
    object Integrated3DSConfig {
        object Errors {
            const val INTEGRATED_3DS_ERROR =
                "An unexpected error occurred while processing Integrated 3DS verification. " +
                    "Please try again later or contact support for assistance."
            const val INVALID_TOKEN_ERROR = "Invalid Integrated 3DS token!"
            const val INVALID_TOKEN_FORMAT_ERROR = "Invalid Integrated 3DS token format!"
        }
    }

    /**
     * Constants related to 3D Secure (3DS) configuration.
     *
     * This object holds constants specific to the 3DS verification process.
     */
    object Standalone3DSConfig {
        object Errors {
            const val STANDALONE_3DS_ERROR =
                "An unexpected error occurred while processing Standalone 3DS verification. " +
                    "Please try again later or contact support for assistance."
            const val INVALID_TOKEN_ERROR = "Invalid Standalone 3DS token!"
            const val INVALID_TOKEN_FORMAT_ERROR = "Invalid Standalone 3DS token format!"
        }
    }

    /**
     * Constants related to wallet callback types.
     */
    object WalletCallbackType {
        internal const val TYPE_CREATE_TRANSACTION = "CREATE_TRANSACTION"
        internal const val TYPE_CREATE_SESSION = "CREATE_SESSION"
    }

    /**
     * Constants related to Google Pay configuration.
     */
    object GooglePayConfig {
        @Suppress("TopLevelPropertyNaming")
        internal val ALLOWED_CARD_AUTH_METHODS = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

        @Suppress("TopLevelPropertyNaming")
        internal val ALLOWED_CARD_NETWORKS = listOf("AMEX", "DISCOVER", "JCB", "MASTERCARD", "VISA")

        internal const val TRANSACTION_PRICE_STATUS = "FINAL"
        internal const val TOKENIZATION_TYPE = "PAYMENT_GATEWAY"

        internal const val ALLOWED_PAYMENT_METHODS_KEY = "allowedPaymentMethods"
        internal const val TOKENIZATION_DATA_KEY = "tokenizationData"
        internal const val TOKEN_KEY = "token"
        internal const val GATEWAY = "paydock"

        /**
         * MIN: Name, country code, and postal code (default).
         * FULL: Name, street address, locality, region, country code, and postal code.
         */
        internal const val BILLING_ADDRESS_FORMAT = "FULL"
        internal const val CARD_PAYMENT_TYPE = "CARD"

        object Errors {
            const val TOKEN_ERROR =
                "There is a problem retrieving the Google Pay token. Please try again later or " +
                    "contact support for assistance."
            const val DEV_ERROR =
                "A developer error occurred. Please try again later or contact support for assistance."
            const val GOOGLE_PAY_ERROR =
                "An unexpected error occurred while processing Google Pay. Please try again later or " +
                    "contact support for assistance."
            const val INITIALISATION_ERROR = "Unexpected non API exception when trying to " +
                "retrieve [allowedPaymentMethods] parameter from PaymentRequest!"
            const val CANCELLATION_ERROR = "Google Pay charge was cancelled!"
            const val WALLET_TOKEN_ERROR =
                "An unexpected error occurred while retrieving Google Pay wallet token. Please try again later."
        }
    }

    /**
     * Constants related to PayPal configuration.
     */
    object PayPalConfig {
        internal const val PAY_PAL_REDIRECT_PARAM_VALUE =
            "${DEFAULT_WEB_URL}paypal/success&native_xo=1"
        internal const val REDIRECT_PARAM_NAME = "redirect_uri"
        internal const val TOKEN_KEY = "token"
        internal const val PAYER_ID_KEY = "PayerID"
        internal const val FLOW_ID_KEY = "flowId"
        internal const val OP_TYPE_KEY = "opType"
        internal const val CANCEL_TYPE = "cancel"
        internal const val COMPLETE_TYPE = "payment"

        object Errors {
            const val PAY_PAL_ERROR =
                "An unexpected error occurred while processing PayPal. Please try again later or contact support for assistance."
            const val CANCELLATION_ERROR = "PayPal charge was cancelled!"
            const val WALLET_TOKEN_ERROR =
                "An unexpected error occurred while retrieving PayPal wallet token. Please try again later."
        }
    }

    /**
     * Constants related to PayPal Vault configuration.
     */
    object PayPalVaultConfig {
        // This needs to match the scheme used in the manifest file
        internal const val URL_SCHEME = "${BuildConfig.LIBRARY_PACKAGE_NAME}.paypal.vault"
        internal const val RETURN_URL = "$URL_SCHEME://vault/success"
        internal const val CANCEL_URL = "$URL_SCHEME://vault/cancel"

        object Errors {
            const val VAULT_ERROR =
                "An unexpected error occurred while processing PayPal Vault. Please try again " +
                    "later or contact support for assistance."
            const val DATA_COLLECTOR_ERROR =
                "An unexpected error occurred while processing PayPal Data Collector. Please try " +
                    "again later or contact support for assistance."
            const val DATA_COLLECTOR_UNKNOWN_ERROR =
                "An unknown error occurred while trying to initialise the PayPalDataCollector. " +
                    "Please try again later or contact support for assistance."
            const val CANCELLATION_ERROR = "PayPal Vault charge was cancelled!"
        }
    }

    /**
     * Constants related to Coles Pay configuration.
     */
    object ColesPayConfig {
        internal const val COLES_PAY_SUCCESS_PATH = "/payment-confirmed"
        internal const val COLES_PAY_REDIRECT_URL = DEFAULT_WEB_URL

        object Errors {
            const val CANCELLATION_ERROR = "Coles Pay charge was cancelled!"
            const val COLES_PAY_ERROR =
                "An unexpected error occurred while processing Coles Pay. Please try again later or contact support for assistance"
        }
    }

    /**
     * Constants related to Afterpay configuration.
     */
    object AfterpayConfig {
        const val USER_INITIATED_ERROR_MESSAGE = "Afterpay: User cancelled the charge"
        const val NO_CHECKOUT_URL_ERROR_MESSAGE = "Afterpay: No checkout URL"
        const val INVALID_CHECKOUT_URL_ERROR_MESSAGE = "Afterpay: Invalid checkout URL"
        const val NO_CHECKOUT_HANDLER_ERROR_MESSAGE = "Afterpay: No checkout handler supplied"
        const val NO_CONFIGURATION_ERROR_MESSAGE = "Afterpay: No configuration supplied"
        const val LANGUAGE_NOT_SUPPORTED_ERROR_MESSAGE = "Afterpay: Language not supported"

        object Errors {
            const val AFTER_PAY_ERROR =
                "An unexpected error occurred while processing Afterpay. Please try again later or contact support for assistance."
            const val CALLBACK_ERROR =
                "An unexpected error occurred while retrieving checkout token. Please try again later or contact support for assistance."
            const val SDK_INTERNAL_ERROR = "Intent should always be populated by the SDK!"
            const val SDK_CANCELLATION_ERROR = "A wallet token is always associated with a wallet transaction!"
            const val WALLET_TOKEN_ERROR =
                "An unexpected error occurred while retrieving PayPal wallet token. Please try again later."
        }
    }

    /**
     * Constants related to ClickToPay configuration.
     */
    object ClickToPayConfig {
        object Errors {
            const val CLICK_TO_PAY_ERROR =
                "An unexpected error occurred while processing ClickToPay. Please try again later or contact support for assistance."
        }
    }
}