package com.paydock.feature.card.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.mapper.asEntity
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.usecase.CreateCardPaymentTokenUseCase
import com.paydock.feature.card.domain.usecase.GetCardSchemasUseCase
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
    private lateinit var getCardSchemasUseCaseTest: GetCardSchemasUseCase
    private lateinit var createCardPaymentUseCase: CreateCardPaymentTokenUseCase

    private fun setupCreateCardPaymentUseCaseSuccess() {
        val mockToken = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
        val mockResult = Result.success(
            TokenDetails(
                token = mockToken,
                type = "token"
            )
        )
        coEvery {
            createCardPaymentUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                any()
            )
        } returns mockResult
    }

    private fun setupGetCardSchemasSuccess() {
        val json = """
            {
              "card_schemas": [
                {"bin": "420412", "schema": "visa"},
                { "bin": "2221~2720", "schema": "mastercard" },
                { "bin": "324000", "schema": "amex" },
                { "bin": "309", "schema": "diners" },
                { "bin": "6011", "schema": "discover" },
                { "bin": "180000~180099", "schema": "japcb" },
                { "bin": "633454", "schema": "solo" },
                { "bin": "5610", "schema": "ausbc" }
              ]
            }
        """.trimIndent()
        val response = json.convertToDataClass<CardSchemasResponse>()
        // Act
        val cardSchemas = response.asEntity()
        val mockResult = Result.success(cardSchemas)
        coEvery {
            getCardSchemasUseCaseTest()
        } returns mockResult
    }

    private fun setupCreateCardPaymentUseCasFailure() {
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
            createCardPaymentUseCase(
                MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
                any()
            )
        } returns mockResult
    }

    @Before
    fun setup() {
        createCardPaymentUseCase = mockk()
        getCardSchemasUseCaseTest = mockk()

        setupGetCardSchemasSuccess()

        viewModel = CardDetailsViewModel(
            MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            MobileSDKTestConstants.General.MOCK_GATEWAY_ID,
            SupportedSchemeConfig(),
            getCardSchemasUseCaseTest,
            createCardPaymentUseCase,
            dispatchersProvider
        )
    }

    @Test
    fun `updateCardholderName should update cardholderName and clear error`() = runTest {
        val newName = "John Doe"
        setupGetCardSchemasSuccess()
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
        setupGetCardSchemasSuccess()
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
        setupGetCardSchemasSuccess()
        val newExpiry = "0536"
        // ACTION
        viewModel.updateExpiry(newExpiry)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertEquals(newExpiry, inputState.expiry)
    }

    @Test
    fun `updateSecurityCode should update security and clear error`() = runTest {
        setupGetCardSchemasSuccess()
        val newSecurityCode = "123"
        // ACTION
        viewModel.updateSecurityCode(newSecurityCode)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertEquals(newSecurityCode, inputState.code)
    }

    @Test
    fun `updateSaveCard should update save card state`() = runTest {
        setupGetCardSchemasSuccess()
        // ACTION
        viewModel.updateSaveCard(true)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(CardDetailsUIState.Idle, state)
        assertTrue(inputState.saveCard)
    }

    @Test
    fun `updating credit card details should have valid data`() = runTest {
        setupGetCardSchemasSuccess()
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
        setupGetCardSchemasSuccess()
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
            setupCreateCardPaymentUseCaseSuccess()
            setupGetCardSchemasSuccess()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // Initial state
                assertIs<CardDetailsUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<CardDetailsUIState.Loading>(awaitItem())
                coVerify { createCardPaymentUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<CardDetailsUIState.Success>(state)
                    assertEquals(MobileSDKTestConstants.Card.MOCK_CARD_TOKEN, state.token)
                }
            }
        }

    @Test
    fun `credit card tokeniseCard should update isLoading, call useCase, and update state on failure`() =
        runTest {
            setupCreateCardPaymentUseCasFailure()
            setupGetCardSchemasSuccess()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // Initial state
                assertIs<CardDetailsUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<CardDetailsUIState.Loading>(awaitItem())
                coVerify { createCardPaymentUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
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
        setupCreateCardPaymentUseCaseSuccess()
        setupGetCardSchemasSuccess()
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
