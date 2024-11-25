package com.paydock.feature.card.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.api.tokens.domain.model.TokenDetails
import com.paydock.api.tokens.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.card.presentation.state.CardDetailsUIState
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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
internal class CardDetailsViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: CardDetailsViewModel
    private lateinit var useCase: CreateCardPaymentTokenUseCase

    @Before
    fun setup() {
        useCase = mockk()
        viewModel = CardDetailsViewModel(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            MobileSDKTestConstants.General.MOCK_GATEWAY_ID,
            useCase,
            dispatchersProvider
        )
    }

    @Test
    fun `updateCardholderName should update cardholderName and clear error`() = runTest {
        val newName = "John Doe"
        // ACTION
        viewModel.updateCardholderName(newName)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertEquals(newName, inputState.cardholderName)
    }

    @Test
    fun `updateCardNumber should update cardNumber and clear error`() = runTest {
        val newNumber = "4111111111111111"
        // ACTION
        viewModel.updateCardNumber(newNumber)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertEquals(newNumber, inputState.cardNumber)
    }

    @Test
    fun `updateExpiry should update card expiry and clear error`() = runTest {
        val newExpiry = "0536"
        viewModel.updateExpiry(newExpiry)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertEquals(newExpiry, inputState.expiry)
    }

    @Test
    fun `updateSecurityCode should update security and clear error`() = runTest {
        val newSecurityCode = "123"
        viewModel.updateSecurityCode(newSecurityCode)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertEquals(newSecurityCode, inputState.code)
    }

    @Test
    fun `updateSaveCard should update save card state`() = runTest {
        viewModel.updateSaveCard(true)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertTrue(inputState.saveCard)
    }

    @Test
    fun `updating credit card details should have valid data`() = runTest {
        // ACTION - Valid card details data
        viewModel.updateCardholderName("John Doe")
        viewModel.updateCardNumber("4111111111111111")
        viewModel.updateExpiry("0536")
        viewModel.updateSecurityCode("123")
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertTrue(inputState.isDataValid)
    }

    @Test
    fun `updating credit card details should have invalid data`() = runTest {
        // ACTION - Invalid card details data
        viewModel.updateCardholderName("John Doe")
        viewModel.updateCardNumber("4111abc") // invalid characters
        viewModel.updateExpiry("0520") // expired
        viewModel.updateSecurityCode("1234") // too many numbers for CVV
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertFalse(inputState.isDataValid)
    }

    @Test
    fun `credit card tokeniseCard should update isLoading, call useCase, and update state on success`() =
        runTest {
            val mockToken = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
            val mockResult = Result.success(TokenDetails(token = mockToken, type = "token"))
            coEvery {
                useCase(
                    MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                    any()
                )
            } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // Initial state
                assertIs<CardDetailsUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<CardDetailsUIState.Loading>(awaitItem())
                coVerify { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<CardDetailsUIState.Success>(state)
                    assertEquals(mockToken, state.token)
                }
            }
        }

    @Test
    fun `credit card tokeniseCard should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val mockError = CardDetailsException.TokenisingCardException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "tokenisation_error",
                        message = MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR
                    )
                )
            )
            val mockResult = Result.failure<TokenDetails>(mockError)
            coEvery {
                useCase(
                    MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                    any()
                )
            } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // Initial state
                assertIs<CardDetailsUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<CardDetailsUIState.Loading>(awaitItem())
                coVerify { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<CardDetailsUIState.Error>(state)
                    assertIs<CardDetailsException.TokenisingCardException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `resetResultState should reset data state`() = runTest {
        val mockToken = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
        val mockResult = Result.success(TokenDetails(token = mockToken, type = "token"))
        coEvery {
            useCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                any()
            )
        } returns mockResult

        viewModel.stateFlow.test {
            // ACTION
            viewModel.tokeniseCard()
            // CHECK
            // Initial state
            assertIs<CardDetailsUIState.Idle>(awaitItem())
            // Loading state - before execution
            assertIs<CardDetailsUIState.Loading>(awaitItem())
            // Success state - after execution
            assertIs<CardDetailsUIState.Success>(awaitItem())
            viewModel.resetResultState()
            // Reset State
            assertIs<CardDetailsUIState.Idle>(awaitItem())
        }
    }
}
