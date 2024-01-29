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

package com.paydock.core.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Implementation of [DispatchersProvider] providing standard [CoroutineDispatcher] instances.
 */
object DispatchersProviderImpl : DispatchersProvider {

    /**
     * Returns the main thread dispatcher.
     *
     * @return The main thread [CoroutineDispatcher].
     */
    override fun getMain(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Returns the main thread immediate dispatcher.
     *
     * @return The main thread immediate [CoroutineDispatcher].
     */
    override fun getMainImmediate(): CoroutineDispatcher = Dispatchers.Main.immediate

    /**
     * Returns the IO dispatcher.
     *
     * @return The IO [CoroutineDispatcher].
     */
    override fun getIO(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Returns the default dispatcher.
     *
     * @return The default [CoroutineDispatcher].
     */
    override fun getDefault(): CoroutineDispatcher = Dispatchers.Default
}
