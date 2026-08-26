package com.paydock.feature.card.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.paydock.binprocessor.data.dto.BinDataResponse
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.CardDetailsException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.core.utils.MainDispatcherRule
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
import kotlinx.coroutines.test.runCurrent
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
              "2": {
                "4": "v",
                "41": "v",
                "34": "a",
                "37": "a",
                "51": "m",
                "55": "m",
                "62": "u",
                "60": "d"
              },
              "4": {
                "4024": "v",
                "4208": "v",
                "4917": "v",
                "2356": "m",
                "5570": "m",
                "5499": "m",
                "3480": "a",
                "3714": "a",
                "3755": "a",
                "3031": "c",
                "3048": "c",
                "3624": "c",
                "2131": "j",
                "3569": "j",
                "6011": "d",
                "6018": "d",
                "6282": "u",
                "6285": "u",
                "8105": "u"
              },
              "6": {
                "402400": "v",
                "420800": "v",
                "491734": "v",
                "235699": "m",
                "557023": "m",
                "549929": "m",
                "348090": "a",
                "371400": "a",
                "375527": "a",
                "303125": "c",
                "304848": "c",
                "362400": "c",
                "213151": "j",
                "356901": "j",
                "601170": "d",
                "601182": "d",
                "601126": "d",
                "628212": "u",
                "628598": "u",
                "810512": "u",
                "622987": "u"
              },
              "v": 1,
              "s": {
                "m": "mastercard",
                "c": "diners",
                "j": "japcb",
                "a": "amex",
                "v": "visa",
                "d": "discover",
                "u": "unionpay"
              },
              "r": {
                "2": [],
                "4": [
                  ["2221", "2720", "m"]
                ],
                "6": []
              }
            }
        """.trimIndent()
        val binData = json.convertToDataClass<BinDataResponse>()
        coEvery {
            getCardSchemasUseCaseTest()
        } returns Result.success(binData)
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
            dispatchersProvider,
            SavedStateHandle()
        )
    }

    private fun makeViewModel(savedStateHandle: SavedStateHandle) = CardDetailsViewModel(
        MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
        MobileSDKTestConstants.General.MOCK_GATEWAY_ID,
        SupportedSchemeConfig(),
        getCardSchemasUseCaseTest,
        createCardPaymentUseCase,
        dispatchersProvider,
        savedStateHandle
    )

    // region Rotation / configuration-change state preservation

    @Test
    fun `config change restores cardholder name and save card but drops PAN CVV and expiry`() = runTest {
        setupGetCardSchemasSuccess()
        val savedState = SavedStateHandle()
        val original = makeViewModel(savedState)
        original.updateCardholderName("John Doe")
        original.updateSaveCard(true)
        original.updateCardNumber("4111111111111111")
        original.updateExpiry("0536")
        original.updateSecurityCode("123")

        // A rotation/process death recreates the ViewModel from the same SavedStateHandle.
        val recreated = makeViewModel(savedState)
        val state = recreated.inputStateFlow.first()

        // Non-sensitive fields survive the recreation.
        assertEquals("John Doe", state.cardholderName)
        assertTrue(state.saveCard)
        // PCI: PAN, CVV and expiry are never persisted, so they come back empty.
        assertEquals("", state.cardNumber)
        assertEquals("", state.code)
        assertEquals("", state.expiry)
    }

    @Test
    fun `config change with no prior input keeps defaults`() = runTest {
        setupGetCardSchemasSuccess()
        val savedState = SavedStateHandle()
        makeViewModel(savedState) // nothing entered

        val recreated = makeViewModel(savedState)
        val state = recreated.inputStateFlow.first()

        assertEquals(null, state.cardholderName)
        assertFalse(state.saveCard)
        assertEquals("", state.cardNumber)
    }

    // endregion

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
            val testScope = this
            setupCreateCardPaymentUseCaseSuccess()
            setupGetCardSchemasSuccess()
            // Loading is now set synchronously (before dispatching to IO — see tokeniseCard's
            // re-entrancy guard), so by the time a collector subscribes it has already moved past
            // Idle; start observing from Loading rather than expecting Idle first.
            viewModel.tokeniseCard()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // Loading state - before execution
                assertIs<CardDetailsUIState.Loading>(awaitItem())
                // Let the launched coroutine actually reach the use-case call before verifying it.
                testScope.runCurrent()
                coVerify { createCardPaymentUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<CardDetailsUIState.Success>(state)
                    assertEquals(MobileSDKTestConstants.Card.MOCK_CARD_TOKEN, state.token)
                }
            }
        }

    @Test
    fun `credit card tokeniseCard called twice while already loading only calls useCase once`() = runTest {
        // Regression test: a fast double-submit (e.g. the internal button and an external
        // state.submit() racing) must not fire two tokenisation requests.
        setupCreateCardPaymentUseCaseSuccess()
        setupGetCardSchemasSuccess()

        viewModel.tokeniseCard()
        viewModel.tokeniseCard() // Should be a no-op: state is already Loading synchronously.

        viewModel.stateFlow.first { it is CardDetailsUIState.Success }
        coVerify(exactly = 1) { createCardPaymentUseCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
    }

    @Test
    fun `credit card tokeniseCard should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val testScope = this
            setupCreateCardPaymentUseCasFailure()
            setupGetCardSchemasSuccess()
            // Loading is now set synchronously (before dispatching to IO — see tokeniseCard's
            // re-entrancy guard), so by the time a collector subscribes it has already moved past
            // Idle; start observing from Loading rather than expecting Idle first.
            viewModel.tokeniseCard()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // Loading state - before execution
                assertIs<CardDetailsUIState.Loading>(awaitItem())
                // Let the launched coroutine actually reach the use-case call before verifying it.
                testScope.runCurrent()
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
