package com.paydock.core.extensions

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.core.domain.error.extensions.mapApiException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.reflect.KClass

/**
 * Inline function to cast an instance to a specific type.
 *
 * @return The casted instance of type [T].
 */
internal inline fun <reified T> Any.castAs(): T {
    return this as T
}

/**
 * Inline function to safely cast an instance to a specific type.
 *
 * @return The casted instance of type [T], or null if the cast is not possible.
 */
internal inline fun <reified T> Any.safeCastAs(): T? {
    return this as? T
}

/**
 * Runs a given [block] that suspends and returns a result wrapped in a [Result].
 * Any exceptions thrown during the execution of the [block] are caught and wrapped in a [Result.failure].
 *
 * @param block The suspend block of code to be executed.
 * @return A [Result] that either contains the result of the [block] in [Result.success],
 * or the exception thrown during execution in [Result.failure].
 */
internal suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    // Execute the provided suspend block and wrap the result in Result.Success
    Result.success(block())
} catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
    Result.failure(exception)
}

/**
 * A utility inline function to execute a suspending block of code and handle exceptions by mapping them to a specific exception type.
 *
 * This function runs a given suspending block, returning a [Result] wrapping the success or failure. If an exception is caught,
 * it maps the caught exception to the specified type [E] using the `mapApiException` function.
 *
 * @param T The type of the result to be returned.
 * @param exceptionClass The type of the exception to map to in case of failure.
 * @param block The suspending block of code to execute.
 * @return A [Result] instance containing either the successful result or the mapped exception.
 */
internal suspend fun <T> suspendRunCatchingMapper(
    exceptionClass: KClass<out SdkException>,
    block: suspend () -> T
): Result<T> = try {
    Result.success(block())
} catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
    Result.failure(exception.mapApiException(exceptionClass))
}

/**
 * Runs a given [flowBlock] that returns a [Flow], and catches any exceptions that occur during the flow collection.
 * It emits the result as a [Result] inside the returned flow.
 *
 * @param flowBlock The block of code that returns a [Flow] to be executed.
 * @return A [Flow] of [Result] that emits either [Result.success] containing the result of [flowBlock],
 * or [Result.failure] containing the exception if any exception occurs.
 */
internal fun <T> runCatchingFlow(flowBlock: () -> Flow<T>): Flow<Result<T>> = flow {
    // Execute the provided flowBlock and catch any exceptions that may occur
    flowBlock()
        .catch { exception -> emit(Result.failure(exception)) }
        .collect { data -> emit(Result.success(data)) }
}

/**
 * Extension function on Result class to get the value or throw a specific exception.
 *
 * @param T The type of the value contained in the Result.
 * @param E The type of the Throwable to be thrown.
 * @param exceptionClass The KClass of the exception to be thrown if the Result is a failure.
 * @return The value contained in the Result if it is successful.
 * @throws E The exception if the Result is a failure and the exception matches the specified type.
 */
fun <T, E : Throwable> Result<T>.getOrThrow(exceptionClass: KClass<E>): T {
    return runBlocking {
        suspendCancellableCoroutine { continuation ->
            onFailure { exception ->
                if (exceptionClass.isInstance(exception)) {
                    continuation.resumeWithException(exception)
                } else {
                    // Continue with the original exception
                    continuation.resumeWithException(exception)
                }
            }
            onSuccess { value ->
                continuation.resume(value)
            }
        }
    }
}

/**
 * Executes a given block of code and provides a mechanism to catch multiple specified exception types,
 * executing an alternative block of code if any of the specified exceptions are caught.
 *
 * This function is designed to handle scenarios where you want to catch a specific set of exceptions
 * and perform a fallback action, while still allowing other exceptions to propagate.
 *
 * @param R The return type of the block of code being executed.
 * @param exceptions A vararg of [KClass] representing the exception types to catch.
 * @param thenDo The block of code to execute if any of the specified exceptions are caught.
 * @return The result of the original block of code if no specified exceptions are thrown,
 * or the result of [thenDo] if a specified exception is caught.
 * @throws Exception If an exception is thrown that is not in the list of [exceptions].
 *
 * @sample
 *
 * val result = {
 * // Some code that might throw exceptions
 *   throw IllegalArgumentException("Invalid argument")
 * }.multiCatch(IllegalArgumentException: : class,  IllegalStateException: : class)  {
 * // Code to execute if IllegalArgumentException or IllegalStateException is thrown
 *   println("Caught a specified exception")
 *   "Fallback Result"
 * }
 * println(result) // Output: Caught a specified exception, Fallback Result
 *
 * @sample
 *
 * val result = {
 * // Some code that might throw exceptions
 *    throw RuntimeException("Unexpected error")
 * }.multiCatch(IllegalArgumentException:: class,  IllegalStateException:: class)  {
 *    // Code to execute if IllegalArgumentException or IllegalStateException is thrown
 *    println("Caught a specified exception")
 *    "Fallback Result"
 * }
 * println(result) // Output: throws RuntimeException
 **/
inline fun <R> (() -> R).multiCatch(vararg exceptions: KClass<out Throwable>, thenDo: () -> R): R {
    return try {
        this()
    } catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
        if (exceptions.any { it.isInstance(ex) }) {
            thenDo()
        } else {
            throw ex
        }
    }
}

/**
 * Extension function on Result class to perform an action if the Result is a failure of a specific type.
 *
 * @param R The type of the value contained in the Result.
 * @param T The type of the Throwable to be checked and handled.
 * @param type The KClass of the exception type to be handled.
 * @param action The action to be performed if the Result is a failure of the specified type.
 * @return The original Result.
 */
@Suppress("UNCHECKED_CAST")
inline fun <R, T : Throwable> Result<R>.onFailure(
    type: KClass<T>,
    action: (throwable: T) -> Unit
): Result<R> {
    exceptionOrNull()?.let { throwable ->
        if (throwable::class == type) {
            action(throwable as T)
        }
    }
    return this
}
