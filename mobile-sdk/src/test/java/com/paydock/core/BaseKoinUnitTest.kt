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

package com.paydock.core

import com.paydock.core.data.injection.modules.mockSuccessNetworkModule
import com.paydock.core.data.injection.modules.sdkModule
import com.paydock.core.data.injection.modules.testModule
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

abstract class BaseKoinUnitTest : BaseUnitTest() {

    @Before
    fun setupKoin() {
        // Load the Koin modules here, if needed for the test
        startKoin {
            modules(
                // Includes basic architecture modules (data, domain and presentation)
                sdkModule,
                // Includes core testing module (context)
                testModule,
                // Includes core networking module (can be substituted)
                mockSuccessNetworkModule
            )
        }
    }

    @After
    fun tearDownKoin() {
        stopKoin()
    }
}