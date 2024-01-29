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

package com.paydock.feature.card.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.displayableMessage
import com.paydock.core.domain.error.toError
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
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

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CreditCardDetailsViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: CreditCardDetailsViewModel
    private lateinit var useCase: TokeniseCreditCardUseCase

    @Before
    fun setup() {
        useCase = mockk()
        viewModel = CreditCardDetailsViewModel(useCase, dispatchersProvider)
    }

    @Test
    fun `setGatewayId should update gatewayId`() = runTest {
        val gatewayId = "testGateWayId"
        // ACTION
        viewModel.setGatewayId(gatewayId)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(gatewayId, state.gatewayId)
    }

    @Test
    fun `updateCardholderName should update cardholderName and clear error`() = runTest {
        val newName = "John Doe"
        // ACTION
        viewModel.updateCardholderName(newName)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newName, state.cardholderName)
        assertNull(state.error)
    }

    @Test
    fun `updateCardNumber should update cardNumber and clear error`() = runTest {
        val newNumber = "4111111111111111"
        // ACTION
        viewModel.updateCardNumber(newNumber)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newNumber, state.cardNumber)
        assertNull(state.error)
    }

    @Test
    fun `updateExpiry should update card expiry and clear error`() = runTest {
        val newExpiry = "0536"
        viewModel.updateExpiry(newExpiry)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newExpiry, state.expiry)
        assertNull(state.error)
    }

    @Test
    fun `updateSecurityCode should update security and clear error`() = runTest {
        val newSecurityCode = "123"
        viewModel.updateSecurityCode(newSecurityCode)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newSecurityCode, state.code)
        assertNull(state.error)
    }

    @Test
    fun `updating credit card details should have valid data`() = runTest {
        viewModel.setGatewayId("testGateWayId")
        // ACTION - Valid card details data
        viewModel.updateCardholderName("John Doe")
        viewModel.updateCardNumber("4111111111111111")
        viewModel.updateExpiry("0536")
        viewModel.updateSecurityCode("123")
        // CHECK
        val state = viewModel.stateFlow.first()
        assertTrue(state.isDataValid)
    }

    @Test
    fun `updating credit card details should have invalid data`() = runTest {
        viewModel.setGatewayId("testGateWayId")
        // ACTION - Invalid card details data
        viewModel.updateCardholderName("John Doe")
        viewModel.updateCardNumber("4111abc") // invalid characters
        viewModel.updateExpiry("0520") // expired
        viewModel.updateSecurityCode("1234") // too many numbers for CVV
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isDataValid)
    }

    @Test
    fun `credit card  tokeniseCard should update isLoading, call useCase, and update state on success`() =
        runTest {
            val mockToken = "mocked_token"
            val mockResult = Result.success(TokenisedCardDetails(token = mockToken, type = "token"))
            coEvery { useCase(any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { useCase(any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertEquals(mockToken, state.token)
                    assertNull(state.error)
                }
            }
        }

    @Test
    fun `credit card tokeniseCard should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val mockError = Exception("Tokenization failed")
            val mockResult = Result.failure<TokenisedCardDetails>(mockError)
            coEvery { useCase(any()) } returns mockResult
            viewModel.setGatewayId("testGateWayId")
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // 4.
                // Initial state
                assertFalse(awaitItem().isLoading)
                // Loading state - before execution
                assertTrue(awaitItem().isLoading)
                coVerify { useCase(any()) }
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.token)
                    assertNotNull(state.error)
                    assertEquals(
                        mockError.toError().displayableMessage,
                        state.error?.displayableMessage
                    )
                }
            }
        }
}
