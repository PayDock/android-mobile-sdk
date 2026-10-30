package com.paydock.core.domain.error.exceptions

/**
 * Represents an exception related to Standalone3D Secure operations.
 *
 * @constructor Creates a ThreeDSException with the specified displayable message.
 * @param displayableMessage A user-friendly message explaining the cause of the event mapping failure.
 *                              This message is intended for display to the end-user or logging for debugging purposes
 */
sealed class Standalone3DSException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Exception thrown when there is an error while communicating with a WebView.
     *
     * @param code The HTTP code of the response, if available.
     * @param displayableMessage A message that can be displayed to the user.
     * @param host The host of the request that failed (no path or query), if known.
     * @param charge3dsId The 3DS charge ID of the session, to look the session up in Paydock.
     * @param inBackground Whether the app was in the background when the error happened.
     * @param networkAvailable Whether the device had a network connection when the error happened.
     * @constructor Creates a WebViewException with the specified HTTP code, displayable message and context.
     */
    class WebViewException(
        val code: Int? = null,
        displayableMessage: String,
        val host: String? = null,
        val charge3dsId: String? = null,
        val inBackground: Boolean? = null,
        val networkAvailable: Boolean? = null,
    ) : Standalone3DSException(displayableMessage) {

        /**
         * Kept for binary compatibility with the original constructor.
         */
        @Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
        constructor(code: Int?, displayableMessage: String) : this(code, displayableMessage, null)
    }

    /**
     * Exception thrown when the WebView's renderer process stops, either because it crashed or because Android
     * reclaimed it for memory (typically while the app is in the background). The 3DS session can't continue.
     *
     * @param didCrash True if the renderer crashed, false if Android killed it to reclaim memory.
     * @param displayableMessage A message that can be displayed to the user.
     * @param charge3dsId The 3DS charge ID of the session, to look the session up in Paydock.
     * @param inBackground Whether the app was in the background when the renderer stopped.
     */
    class RenderProcessGoneException(
        val didCrash: Boolean,
        displayableMessage: String,
        val charge3dsId: String? = null,
        val inBackground: Boolean? = null,
    ) : Standalone3DSException(displayableMessage)

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
     * @see Standalone3DSException The base class for exceptions related to the 3DS protocol.
     */
    class InvalidTokenException(displayableMessage: String) : Standalone3DSException(displayableMessage)

    /**
     * Exception thrown when there is an issue mapping an event to a specific action or handling mechanism.
     *
     * This exception indicates that the system was unable to correctly interpret or process an incoming
     * event. It typically occurs when the event data is malformed, unexpected, or the system lacks
     * the necessary configuration or logic to handle that particular event type.
     *
     * @constructor Creates a new EventMappingException with a specified displayable message.
     * @param displayableMessage The user-friendly error message.
     * @see Standalone3DSException
     */
    class EventMappingException(displayableMessage: String) : Standalone3DSException(displayableMessage)
}
