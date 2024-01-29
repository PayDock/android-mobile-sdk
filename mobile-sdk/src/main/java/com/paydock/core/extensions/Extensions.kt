/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.core.extensions

import com.paydock.core.data.injection.modules.provideJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.concurrent.CancellationException

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
internal suspend fun <T> suspendRunCatching(block: suspend () -> T): kotlin.Result<T> = try {
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
