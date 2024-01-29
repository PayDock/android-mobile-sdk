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

package com.paydock.feature.googlepay.presentation.viewmodels

import app.cash.turbine.test
import com.google.android.gms.wallet.PaymentsClient
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.domain.usecase.CaptureWalletTransactionUseCase
import com.paydock.feature.wallet.domain.usecase.GetWalletCallbackUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@Suppress("MaxLineLength")
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GooglePayViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: GooglePayViewModel
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var captureWalletTransactionUseCase: CaptureWalletTransactionUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    @Before
    fun setup() {
        captureWalletTransactionUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        paymentsClient = mockk()
        viewModel = GooglePayViewModel(paymentsClient, captureWalletTransactionUseCase, getWalletCallbackUseCase, dispatchersProvider)
    }

    @Test
    fun `setWalletToken should update token`() = runTest {
        val walletToken = "testToken"
        // ACTION
        viewModel.setWalletToken(walletToken)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(walletToken, state.token)
    }

    @Test
    fun `capture GooglePay wallet charge should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = "valid-token"
            val validRefToken =
                "{\"signature\":\"MEQCIDPyn7sbShwHJgfp7+odS4csaJKZwL9cWkvLNXfl8aIwAiAiQEJT3p+O0Ul8zeKxf5UkjCb/YNfiDCjT0kWAD7M3Xg\\u003d\\u003d\"," +
                    "\"intermediateSigningKey\":{\"signedKey\":\"{\\\"keyValue\\\":\\\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEwyp2Bhw5FzeKh/1p+XEBjdnb" +
                    "jhKFRXs46fM2SAI787SmNn45I3sKsw3hpkMalE/LVUN6nH/k2dW9L6rvuyueoQ\\\\u003d\\\\u003d\\\",\\\"keyExpiration\\\":\\\"1698720666540\\\"}" +
                    "\",\"signatures\":[\"MEYCIQCKbMoZ4ZkcmBVDr7rHsHQfOHOHTuQ2SjWggWfak0TSMAIhAIwfjJMfwcIl4aOmOzTfHk75VKlMuKTiVCSFn6PGeYgO\"]}," +
                    "\"protocolVersion\":\"ECv2\",\"signedMessage\":\"{\\\"encryptedMessage\\\":\\\"5xKfO44VN1wnvquWpYDPB0outuJrlXGV7E/AdK2/snKT0z/f" +
                    "aInFxUL2urp9WF826qMl0UlAppmm2hUtQeTRxO8v6WJXx+XYAH128Uoc1lKDAsZfZRycq1czYkUnaYHT0OPdkqTUlI9/k3o5vClOyq3YoEVv1e4nUVKGjPTALEEC7Qe3" +
                    "fCReIWIH9RnIP0HQdfYcv6cKWaZHxt7ZJUf3nqSSDeU3H7lKdKJ8zajOC/9erXcm6a2KymtDpz9dIxn/WNBeJjDSYIrPg1RPWm0PRkvU///HuAJrBBJF/SgnQVnotYHk" +
                    "cZnZyFLoo5oByBMR/X5+NM5TyknpoLjVAophJQ/pBiUhsA+dZ0TF4mUBa3bRcMgjhnmR49zRBrf8C5MT+JbyV1ltifWi8t/Vil4ILpIy3CG5hcX50+xbZssUzkEtS6Hd" +
                    "M2X7REVZwuZc5x2eHVTY+MXi2oBdyPljxb4Kfstbf94lZy9R2zY\\\\u003d\\\",\\\"ephemeralPublicKey\\\":\\\"BOZLC8D8kjf9q+qXHR2GumIkeAsIpoad9A" +
                    "bvQwLKp7NjJFSTQuww82Ukyw/YpYEKUtZ1swHrr9RbfJsrFsjjj9Q\\\\u003d\\\",\\\"tag\\\":\\\"f5lDx7XlR8mbHgV9ivXjiULNIekvd8pf6Z/UuOVKnoY\\\\u003d\\\"}\"}"
            val mockResult = Result.success(
                ChargeResponse(
                    status = 200,
                    resource = ChargeResponse.ChargeResource(
                        type = "charge",
                        data = ChargeResponse.ChargeData(
                            status = "complete",
                            id = "653784a37cb913091e1be3e5",
                            amount = BigDecimal(10),
                            currency = "AUD"
                        )
                    )
                )
            )
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(accessToken, validRefToken)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { captureWalletTransactionUseCase(any(), any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertEquals(mockResult.getOrNull(), state.chargeData)
                    assertNull(state.error)
                }
            }
        }

    @Test
    fun `capture GooglePay wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val invalidAccessToken = "invalid-token"
            val validRefToken =
                "{\"signature\":\"MEQCIDPyn7sbShwHJgfp7+odS4csaJKZwL9cWkvLNXfl8aIwAiAiQEJT3p+O0Ul8zeKxf5UkjCb/YNfiDCjT0kWAD7M3Xg\\u003d\\u003d\"," +
                    "\"intermediateSigningKey\":{\"signedKey\":\"{\\\"keyValue\\\":\\\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEwyp2Bhw5FzeKh/1p+XEBjdnb" +
                    "jhKFRXs46fM2SAI787SmNn45I3sKsw3hpkMalE/LVUN6nH/k2dW9L6rvuyueoQ\\\\u003d\\\\u003d\\\",\\\"keyExpiration\\\":\\\"1698720666540\\\"}" +
                    "\",\"signatures\":[\"MEYCIQCKbMoZ4ZkcmBVDr7rHsHQfOHOHTuQ2SjWggWfak0TSMAIhAIwfjJMfwcIl4aOmOzTfHk75VKlMuKTiVCSFn6PGeYgO\"]}," +
                    "\"protocolVersion\":\"ECv2\",\"signedMessage\":\"{\\\"encryptedMessage\\\":\\\"5xKfO44VN1wnvquWpYDPB0outuJrlXGV7E/AdK2/snKT0z/f" +
                    "aInFxUL2urp9WF826qMl0UlAppmm2hUtQeTRxO8v6WJXx+XYAH128Uoc1lKDAsZfZRycq1czYkUnaYHT0OPdkqTUlI9/k3o5vClOyq3YoEVv1e4nUVKGjPTALEEC7Qe3" +
                    "fCReIWIH9RnIP0HQdfYcv6cKWaZHxt7ZJUf3nqSSDeU3H7lKdKJ8zajOC/9erXcm6a2KymtDpz9dIxn/WNBeJjDSYIrPg1RPWm0PRkvU///HuAJrBBJF/SgnQVnotYHk" +
                    "cZnZyFLoo5oByBMR/X5+NM5TyknpoLjVAophJQ/pBiUhsA+dZ0TF4mUBa3bRcMgjhnmR49zRBrf8C5MT+JbyV1ltifWi8t/Vil4ILpIy3CG5hcX50+xbZssUzkEtS6Hd" +
                    "M2X7REVZwuZc5x2eHVTY+MXi2oBdyPljxb4Kfstbf94lZy9R2zY\\\\u003d\\\",\\\"ephemeralPublicKey\\\":\\\"BOZLC8D8kjf9q+qXHR2GumIkeAsIpoad9A" +
                    "bvQwLKp7NjJFSTQuww82Ukyw/YpYEKUtZ1swHrr9RbfJsrFsjjj9Q\\\\u003d\\\",\\\"tag\\\":\\\"f5lDx7XlR8mbHgV9ivXjiULNIekvd8pf6Z/UuOVKnoY\\\\u003d\\\"}\"}"
            val mockError = Exception("Unexpected error during process")
            val mockResult = Result.failure<ChargeResponse>(mockError)
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(invalidAccessToken, validRefToken)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { captureWalletTransactionUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.chargeData)
                    assertNotNull(state.error)
                    assertEquals(
                        mockError.toError().displayableMessage,
                        state.error?.displayableMessage
                    )
                }
            }
        }
}
