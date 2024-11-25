package com.paydock.core.domain.error.exceptions

import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.serialization.SerializationException
import okio.IOException
import java.net.UnknownHostException

/**
 * Represents a set of generic exceptions that can occur within the SDK.
 *
 * This sealed class provides a structured hierarchy for common exceptions, allowing for more
 * descriptive and specific error handling. All subclasses inherit from [SdkException].
 *
 * @param displayableMessage A user-friendly message describing the error, typically used for displaying
 * error messages in the UI or logs.
 */
sealed class GenericException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * Represents a timeout error, commonly related to [SocketTimeoutException].
     *
     * @param displayableMessage A user-friendly message describing the timeout issue.
     */
    class TimeoutException(displayableMessage: String) : GenericException(displayableMessage)

    /**
     * Represents a connection error, commonly related to [UnknownHostException].
     *
     * @param displayableMessage A user-friendly message describing the connection issue.
     */
    class ConnectionException(displayableMessage: String) : GenericException(displayableMessage)

    /**
     * Represents a data parsing error, commonly related to [SerializationException].
     *
     * @param displayableMessage A user-friendly message describing the data parsing issue.
     */
    class DataParsingException(displayableMessage: String) : GenericException(displayableMessage)

    /**
     * Represents a general error, related to common exceptions like [IOException],
     * [IllegalStateException], etc.
     *
     * @param displayableMessage A user-friendly message describing the general issue.
     */
    class GeneralException(displayableMessage: String) : GenericException(displayableMessage)

    /**
     * Represents an unknown error, acting as a catch-all for exceptions that do not fall into
     * the predefined categories.
     *
     * @param displayableMessage A user-friendly message describing the unknown issue.
     */
    class UnknownException(displayableMessage: String) : GenericException(displayableMessage)
}