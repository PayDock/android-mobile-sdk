package com.paydock.sample.core.extensions

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
    // Execute the provided suspend block and wrap the result in Result.Success
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
    Result.failure(exception)
}