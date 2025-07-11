package com.paydock.sample.core.extensions

import java.net.UnknownHostException
import java.util.concurrent.CancellationException

/**
 * Runs a given [block] that suspends and returns a result wrapped in a [Result].
 * Any exceptions thrown during the execution of the [block] are caught and wrapped in a [Result.failure].
 *
 * @param block The suspend block of code to be executed.
 * @return A [Result] that either contains the result of the [block] in [Result.success],
 * or the exception thrown during execution in [Result.failure].
 */
suspend fun <T> suspendRunCatching(block: suspend () -> T): kotlin.Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    // Re-throw cancellation exceptions to ensure coroutines are properly cancelled
    throw cancellationException
} catch (unknownHostException: UnknownHostException) {
    Result.failure(
        NoInternetException(
            message = "No Internet Connection. Please check your network settings and try again.",
            cause = unknownHostException
        )
    )
} catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
    // Catch any other exceptions
    Result.failure(exception)
}

// Custom Exception handling
class NoInternetException(message: String, cause: Throwable? = null) : Exception(message, cause)