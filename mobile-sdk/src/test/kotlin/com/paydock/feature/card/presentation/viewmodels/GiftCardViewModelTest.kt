package com.paydock.feature.card.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.feature.card.domain.model.ui.TokenDetails
import com.paydock.feature.card.domain.usecase.CreateGiftCardPaymentTokenUseCase
import com.paydock.feature.card.presentation.state.GiftCardUIState
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
internal class GiftCardViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: GiftCardViewModel
    private lateinit var useCase: CreateGiftCardPaymentTokenUseCase

    @Before
    fun setup() {
        useCase = mockk()
        viewModel = GiftCardViewModel(
            GiftCardWidgetConfig(accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN),
            useCase,
            dispatchersProvider
        )
    }

    @Test
    fun `updateCardNumber should update cardNumber and clear error`() = runTest {
        val newNumber = "62734010001104878"
        // ACTION
        viewModel.updateCardNumber(newNumber)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(GiftCardUIState.Idle, state)
        assertEquals(newNumber, inputState.cardNumber)
    }

    @Test
    fun `updatePin should update pin and clear error`() = runTest {
        val newSecurityCode = "1234"
        viewModel.updateCardPin(newSecurityCode)
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(GiftCardUIState.Idle, state)
        assertEquals(newSecurityCode, inputState.pin)
    }

    @Test
    fun `updating gift card details should have valid data`() = runTest {
        // ACTION - Valid card details data
        viewModel.updateCardNumber("62734010001104878")
        viewModel.updateCardPin("1234")
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(GiftCardUIState.Idle, state)
        assertTrue(inputState.isDataValid)
    }

    @Test
    fun `updating gift card details should have invalid data`() = runTest {
        // ACTION - Invalid card details data
        viewModel.updateCardNumber("4111abc") // invalid characters
        viewModel.updateCardPin("1234")
        val state = viewModel.stateFlow.first()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertEquals(GiftCardUIState.Idle, state)
        assertFalse(inputState.isDataValid)
    }

    @Test
    fun `gift card tokeniseCard should update isLoading, call useCase, and update state on success`() =
        runTest {
            val mockToken = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
            val mockResult = Result.success(
                TokenDetails(
                    token = mockToken,
                    type = "token"
                )
            )
            coEvery { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // Initial state
                assertIs<GiftCardUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<GiftCardUIState.Loading>(awaitItem())
                coVerify { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertIs<GiftCardUIState.Success>(state)
                    assertEquals(mockToken, state.token)
                }
            }
        }

    @Test
    fun `gift card tokeniseCard should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val mockError = GiftCardException.TokenisingCardException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "invalid_card",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_CARD_DETAILS_ERROR
                    )
                )
            )
            val mockResult = Result.failure<TokenDetails>(mockError)
            coEvery { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) } returns mockResult
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.tokeniseCard()
                // CHECK
                // Initial state
                assertIs<GiftCardUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<GiftCardUIState.Loading>(awaitItem())
                coVerify { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<GiftCardUIState.Error>(state)
                    assertIs<GiftCardException.TokenisingCardException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_INVALID_CARD_DETAILS_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `resetResultState should reset data state`() = runTest {
        val mockToken = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
        val mockResult = Result.success(
            TokenDetails(
                token = mockToken,
                type = "token"
            )
        )
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
            assertIs<GiftCardUIState.Idle>(awaitItem())
            // Loading state - before execution
            assertIs<GiftCardUIState.Loading>(awaitItem())
            // Success state - after execution
            assertIs<GiftCardUIState.Success>(awaitItem())
            viewModel.resetResultState()
            // Reset State
            assertIs<GiftCardUIState.Idle>(awaitItem())
        }
    }
}
