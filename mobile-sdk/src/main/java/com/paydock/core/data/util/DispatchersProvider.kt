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

package com.paydock.core.data.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Interface for providing different [CoroutineDispatcher] instances used for coroutines.
 * Defines functions to obtain dispatchers for IO, Main, MainImmediate, and Default threads.
 */
interface DispatchersProvider {
    /**
     * Returns the [CoroutineDispatcher] for IO-bound tasks.
     *
     * @return The [CoroutineDispatcher] for IO-bound tasks.
     */
    fun getIO(): CoroutineDispatcher

    /**
     * Returns the [CoroutineDispatcher] for the Main thread.
     *
     * @return The [CoroutineDispatcher] for the Main thread.
     */
    fun getMain(): CoroutineDispatcher

    /**
     * Returns the [CoroutineDispatcher] for the Main thread with immediate execution.
     *
     * @return The [CoroutineDispatcher] for the Main thread with immediate execution.
     */
    fun getMainImmediate(): CoroutineDispatcher

    /**
     * Returns the default [CoroutineDispatcher] for CPU-intensive tasks.
     *
     * @return The default [CoroutineDispatcher] for CPU-intensive tasks.
     */
    fun getDefault(): CoroutineDispatcher
}