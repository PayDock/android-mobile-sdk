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

package com.paydock.core.data.injection

import com.paydock.MobileSDK
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

/**
 * Interface for components that require access to the Koin instance associated with the Mobile SDK.
 * Implementing this interface allows classes to access Koin functionalities for dependency injection.
 */
interface MobileSDKKoinComponent : KoinComponent {

    /**
     * Overrides the [getKoin] function from the [KoinComponent] interface to provide access to the Koin instance
     * associated with the Mobile SDK.
     *
     * @return The Koin instance associated with the Mobile SDK.
     * @throws IllegalStateException if the MobileSDK has not been initialized.
     */
    override fun getKoin(): Koin {
        // Ensure the MobileSDK has been initialized
        val koinContext = MobileSDK.getInstance().koinContext
            ?: error(IllegalStateException("MobileSDK has not been initialized. Call MobileSDK.initialize() first."))
        return koinContext.koin
    }
}