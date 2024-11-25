package com.paydock.core.domain.error.exceptions

import java.io.IOException

/**
 * A base sealed class representing exceptions that can occur within the SDK.
 * It extends [IOException], allowing these exceptions to represent errors related to I/O operations.
 *
 * @param displayableMessage A user-facing message describing the error,
 *                            which can be displayed in the app's UI if needed.
 */
sealed class SdkException(displayableMessage: String) : IOException(displayableMessage)
