package com.paydock.core.extensions

import com.paydock.core.data.injection.modules.provideJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
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
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (@Suppress("TooGenericExceptionCaught") exception: Exception) {
    Result.failure(exception)
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
 * Inline function to convert a JSON string to a data class of type [R].
 *
 * @return An instance of the data class [R] created from the JSON string.
 */
internal inline fun <reified R : Any> String.convertToDataClass(): R =
    provideJson().decodeFromString(this)

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
