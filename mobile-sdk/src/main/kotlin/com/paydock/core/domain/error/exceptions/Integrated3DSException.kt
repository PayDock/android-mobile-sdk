package com.paydock.core.domain.error.exceptions

/**
 * Represents an exception related to Integrated 3D Secure operations.
 *
 * @constructor Creates a ThreeDSException with the specified displayable message.
 * @param displayableMessage A user-friendly message explaining the cause of the event mapping failure.
 *                              This message is intended for display to the end-user or logging for debugging purposes
 */
sealed class Integrated3DSException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @param code The HTTP code of the response, if available.
     * @param displayableMessage A message that can be displayed to the user.
     * @constructor Creates a WebViewException with the specified HTTP code and displayable message.
     */
    class WebViewException(val code: Int? = null, displayableMessage: String) :
        Integrated3DSException(displayableMessage)

    /**
     * Exception thrown when an operation is attempted with an invalid or expired token.
     *
     * This exception indicates that the token provided for authentication or authorization
     * is not valid. This could be due to:
     *
     * - The token being malformed.
     * - The token being expired.
     * - The token not being recognized by the system.
     * - The token being revoked.
     *
     * @param displayableMessage A user-friendly message describing the reason for the exception.
     *                           This message is intended to be displayed to the user if necessary.
     *
     * @see Integrated3DSException The base class for exceptions related to the 3DS protocol.
     */
    class InvalidTokenException(displayableMessage: String) : Integrated3DSException(displayableMessage)

    /**
     * Exception thrown when there is an issue mapping an event to a specific action or handling mechanism.
     *
     * This exception indicates that the system was unable to correctly interpret or process an incoming
     * event. It typically occurs when the event data is malformed, unexpected, or the system lacks
     * the necessary configuration or logic to handle that particular event type.
     *
     * @constructor Creates a new EventMappingException with a specified displayable message.
     * @param displayableMessage The user-friendly error message.
     * @see Integrated3DSException
     */
    class EventMappingException(displayableMessage: String) : Integrated3DSException(displayableMessage)
}
