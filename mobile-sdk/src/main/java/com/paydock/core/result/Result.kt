/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
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

package com.paydock.core.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Represents a sealed interface for encapsulating the result of an operation.
 * The result can be either a Success containing the data, an Error containing an exception,
 * or a Loading state indicating that the operation is in progress.
 *
 * @param T The type of data returned in the Success case.
 */
sealed interface Result<out T> {
    /**
     * Represents a Success result containing the data.
     *
     * @property data The data returned by the operation.
     */
    data class Success<T>(val data: T) : Result<T>

    /**
     * Represents an Error result containing an exception.
     *
     * @property exception The exception associated with the error, if any.
     */
    data class Error(val exception: Throwable? = null) : Result<Nothing>

    /**
     * Represents a Loading state indicating that the operation is in progress.
     */
    object Loading : Result<Nothing>
}

/**
 * Extension function to convert a Flow of type T to a Flow of Result<T>.
 * Wraps the emitted values from the original flow into Success results,
 * handles the loading state, and catches any exceptions, transforming them into Error results.
 *
 * @return A Flow of Result<T> where each emitted value is wrapped in a Result.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> {
            Result.Success(it)
        }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
}
