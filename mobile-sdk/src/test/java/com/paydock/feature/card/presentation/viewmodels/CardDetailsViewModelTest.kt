package com.paydock.feature.card.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.ErrorSummary
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.ApiException
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.usecase.TokeniseCreditCardUseCase
import io.ktor.http.HttpStatusCode
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
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CardDetailsViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: CardDetailsViewModel
    private lateinit var useCase: TokeniseCreditCardUseCase

    @Before
    fun setup() {
        useCase = mockk()
        viewModel = CardDetailsViewModel(useCase, dispatchersProvider)
    }

    @Test
    fun `setGatewayId should update gatewayId`() = runTest {
        val gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
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
    fun `updateSaveCard should update save card state`() = runTest {
        viewModel.updateSaveCard(true)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertTrue(state.saveCard)
    }

    @Test
    fun `updating credit card details should have valid data`() = runTest {
        viewModel.setGatewayId(MobileSDKTestConstants.General.MOCK_GATEWAY_ID)
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
        viewModel.setGatewayId(MobileSDKTestConstants.General.MOCK_GATEWAY_ID)
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
    fun `credit card tokeniseCard should update isLoading, call useCase, and update state on success`() =
        runTest {
            val mockToken = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
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
            val mockError = ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "tokenisation_error",
                        message = MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR
                    )
                )
            )
            val mockResult = Result.failure<TokenisedCardDetails>(mockError)
            coEvery { useCase(any()) } returns mockResult
            viewModel.setGatewayId(MobileSDKTestConstants.General.MOCK_GATEWAY_ID)
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
                    assertIs<CardDetailsException.TokenisingCardException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR,
                        state.error.message
                    )
                }
            }
        }
}
