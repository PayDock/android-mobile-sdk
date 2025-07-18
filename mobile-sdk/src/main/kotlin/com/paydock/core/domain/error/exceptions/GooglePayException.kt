package com.paydock.core.domain.error.exceptions

import com.paydock.core.MobileSDKConstants
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.displayableMessage

/**
 * Exception thrown when there's an error related to Google Pay integration.
 *
 * @constructor Creates a GooglePayException with the specified displayable message.
 */
@Suppress("MaxLineLength")
sealed class GooglePayException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error capturing the charge for Google Pay.
     *
     * @property error The underlying error response causing this exception.
     * @constructor Creates a CapturingChargeException with the specified error response.
     *              The displayable message is derived from the error response.
     */
    data class CapturingChargeException(
        val error: ApiErrorResponse
    ) : GooglePayException(error.displayableMessage)

    /**
     * Exception thrown when there is an initialization error related to Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an InitialisationException with the specified displayable message.
     */
    class InitialisationException(
        displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.INITIALISATION_ERROR
    ) : GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is a result error related to Google Pay from Paydock's side after Google Pay.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a ResultException with the specified displayable message.
     */
    class ResultException(displayableMessage: String) : GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is a user-initiated cancellation from Paydock's UI (e.g. close button).
     * This is distinct from GooglePaySDKException.CancelledBySdk where the SDK itself returns a CANCELED status.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates a CancellationException with the specified displayable message.
     */
    class CancellationException(
        displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.CANCELLATION_ERROR
    ) : GooglePayException(displayableMessage)

    /**
     * Represents an exception that occurs during the parsing of data from an API call, typically JSON.
     *
     * This exception is thrown when there is an issue with the format or structure of
     * the data being parsed, preventing it from being processed correctly.
     *
     * @property displayableMessage A user-friendly message describing the parsing error.
     *                             This message is intended to be displayed to the user.
     * @property errorBody An optional string containing the JSON data that caused the parsing error.
     *                     This can be helpful for debugging purposes to pinpoint the exact
     *                     location and nature of the error within the data. If the error isn't related
     *                     to a particular JSON, it could be null.
     * @constructor Creates a new ParseException with the specified displayable message and
     *              optional error JSON.
     */
    class ParseException(displayableMessage: String, val errorBody: String?) : GooglePayException(displayableMessage)

    /**
     * Exception thrown when there is an error during the initialisation of the Google Pay wallet token.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an InitialisationWalletTokenException with the specified displayable message.
     */
    class InitialisationWalletTokenException(displayableMessage: String) : GooglePayException(displayableMessage)

    /**
     * Represents exceptions that originate directly from the Google Pay SDK's status codes.
     * Each specific exception within this sealed class maps to a CommonStatusCode.
     *
     * @param displayableMessage A user-friendly message describing the SDK error.
     * @property statusCodeString The Google Pay SDK CommonStatusCode string representation that triggered this exception.
     */
    sealed class SDKException(
        displayableMessage: String,
        val statusCodeString: String
    ) : GooglePayException(displayableMessage) {

        /**
         * Corresponds to `CommonStatusCodes.CANCELED`.
         * The Google Pay flow was not completed successfully. This could be due to various reasons,
         * not necessarily an explicit user cancellation.
         */
        class CancelledBySdk(
            displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.FLOW_NOT_COMPLETED_ERROR,
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)

        /**
         * Corresponds to `CommonStatusCodes.NETWORK_ERROR`.
         * A network issue prevented the Google Pay operation from completing.
         */
        class NetworkError(
            displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.NETWORK_ERROR,
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)

        /**
         * Corresponds to `CommonStatusCodes.TIMEOUT`.
         * The Google Pay operation timed out.
         */
        class Timeout(
            displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.TIMEOUT_ERROR,
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)

        /**
         * Corresponds to `CommonStatusCodes.DEVELOPER_ERROR`.
         * An issue with the integration configuration or parameters.
         * The displayable message should be user-friendly, not exposing "developer error".
         */
        class DeveloperError(
            displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.DEV_ERROR, // User-friendly version
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)

        /**
         * Corresponds to generic errors like `CommonStatusCodes.INTERNAL_ERROR`, `CommonStatusCodes.ERROR`, `CommonStatusCodes.INTERRUPTED`.
         * An unexpected or internal error occurred within the Google Pay services.
         */
        class ServiceError(
            displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.GOOGLE_PAY_SERVICE_ERROR,
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)

        /**
         * Corresponds to errors related to Google Play Services availability or account issues,
         * such as `SERVICE_VERSION_UPDATE_REQUIRED`, `SERVICE_DISABLED`, `SIGN_IN_REQUIRED`,
         * `INVALID_ACCOUNT`, `API_NOT_CONNECTED`.
         */
        class PlayServicesError(
            displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.PLAY_SERVICES_ERROR,
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)

        /**
         * Corresponds to `CommonStatusCodes.RESOLUTION_REQUIRED` when the resolution fails or cannot be launched.
         */
        class ResolutionFailed(
            displayableMessage: String = MobileSDKConstants.GooglePayConfig.Errors.RESOLUTION_FAILED_ERROR,
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)

        /**
         * For any other unhandled or unknown status codes from the Google Pay SDK.
         */
        class UnknownSdkException(
            displayableMessage: String,
            statusCodeString: String
        ) : SDKException(displayableMessage, statusCodeString)
    }

    /**
     * Exception thrown when there is an unknown error related to Google Pay
     * that doesn't fit into other more specific categories.
     *
     * @param displayableMessage A human-readable message describing the error.
     * @constructor Creates an UnknownException with the specified displayable message.
     */
    class UnknownException(displayableMessage: String) : GooglePayException(displayableMessage)
}
