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

package com.paydock.feature.paypal.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.domain.model.WalletCallback
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
class PayPalViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: PayPalViewModel
    private lateinit var captureWalletTransactionUseCase: CaptureWalletTransactionUseCase
    private lateinit var getWalletCallbackUseCase: GetWalletCallbackUseCase

    @Before
    fun setup() {
        captureWalletTransactionUseCase = mockk()
        getWalletCallbackUseCase = mockk()
        viewModel = PayPalViewModel(captureWalletTransactionUseCase, getWalletCallbackUseCase, dispatchersProvider)
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
    fun `capture PayPal wallet charge should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = "valid-token"
            val paymentMethodId = "06S13800C2876432A"
            val payerId = "H2G7GULMXJZU6"
            val mockResult = Result.success(
                ChargeResponse(
                    status = 200,
                    resource = ChargeResponse.ChargeResource(
                        type = "charge",
                        data = ChargeResponse.ChargeData(
                            status = "complete",
                            id = "659e9d46fb585906ac8d2e9a",
                            amount = BigDecimal(10),
                            currency = "USD"
                        )
                    )
                )
            )
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(accessToken, paymentMethodId, payerId)
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
    fun `capture PayPal wallet charge should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val invalidAccessToken = "invalid-token"
            val paymentMethodId = "06S13800C2876432A"
            val payerId = "H2G7GULMXJZU6"
            val mockError = Exception("Unexpected error during process")
            val mockResult = Result.failure<ChargeResponse>(mockError)
            coEvery { captureWalletTransactionUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.captureWalletTransaction(invalidAccessToken, paymentMethodId, payerId)
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

    @Test
    fun `get PayPal wallet callback should update isLoading, call useCase, and update state on success`() =
        runTest {
            val accessToken = "valid-token"
            val mockCallbackUrl = "https://www.sandbox.paypal.com/checkoutnow?token=63124973UW6479408"
            val mockResult = Result.success(
                WalletCallback(
                    callbackId = "0AT311688E3149121",
                    status = "wallet_initialized",
                    callbackUrl = mockCallbackUrl
                )
            )
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // wallet token is added to state
                assertNotNull(awaitItem().token)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNotNull(state.callbackData)
                    assertNotNull(state.callbackData?.callbackUrl)
                    assertEquals(mockCallbackUrl, state.callbackData?.callbackUrl)
                    assertNull(state.error)
                }
            }
        }

    @Test
    fun `get PayPal wallet callback should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val accessToken = "valid-token"
            val mockError = Exception("wallet_type must be a valid enum value")
            val mockResult = Result.failure<WalletCallback>(mockError)
            coEvery { getWalletCallbackUseCase(any(), any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.setWalletToken(accessToken)
                viewModel.getWalletCallback(walletToken = accessToken, requestShipping = true)
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // wallet token is added to state
                assertNotNull(awaitItem().token)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { getWalletCallbackUseCase(any(), any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.callbackData)
                    assertNotNull(state.error)
                    assertEquals(
                        mockError.toError().displayableMessage,
                        state.error?.displayableMessage
                    )
                }
            }
        }

}
