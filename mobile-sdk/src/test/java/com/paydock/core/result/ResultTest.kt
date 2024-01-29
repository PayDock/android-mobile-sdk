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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ResultTest {

    /**
     * Test case to verify that the Result catches an error when emitted from the flow.
     */
    @Suppress("TooGenericExceptionThrown")
    @Test
    fun `Result catches error from emitted flow`() = runTest {
        // Create a flow that emits a value and then throws an exception
        flow {
            emit(1)
            throw Exception("Test Done")
        }
            .asResult() // Transform the flow into a Result type
            .test {
                // Assert the initial loading state
                assertEquals(Result.Loading, awaitItem())
                // Assert the success state with the emitted value
                assertEquals(Result.Success(1), awaitItem())

                // Check the emitted item for error
                when (val errorResult = awaitItem()) {
                    // Assert that the error result contains the expected exception message
                    is Result.Error -> assertEquals(
                        "Test Done",
                        errorResult.exception?.message
                    )

                    // If the result is not an error, or still loading, it's an invalid state
                    Result.Loading,
                    is Result.Success -> error(
                        "The flow should have emitted an Error Result"
                    )
                }

                // Ensure the test completes successfully
                awaitComplete()
            }
    }
}