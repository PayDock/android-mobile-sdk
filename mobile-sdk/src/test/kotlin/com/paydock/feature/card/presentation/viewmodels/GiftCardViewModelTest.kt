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
import com.paydock.feature.card.presentation.state.GiftCardInputState
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
            val testScope = this
            val mockToken = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN
            val mockResult = Result.success(
                TokenDetails(
                    token = mockToken,
                    type = "token"
                )
            )
            coEvery { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) } returns mockResult
            // Loading is now set synchronously (before dispatching to IO — see tokeniseCard's
            // re-entrancy guard), so by the time a collector subscribes it has already moved past
            // Idle; start observing from Loading rather than expecting Idle first.
            viewModel.tokeniseCard()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // Loading state - before execution
                assertIs<GiftCardUIState.Loading>(awaitItem())
                // Let the launched coroutine actually reach the use-case call before verifying it.
                testScope.runCurrent()
                coVerify { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
                // Resul state - success
                awaitItem().let { state ->
                    assertIs<GiftCardUIState.Success>(state)
                    assertEquals(mockToken, state.token)
                }
            }
        }

    @Test
    fun `gift card tokeniseCard called twice while already loading only calls useCase once`() = runTest {
        // Regression test: a fast double-submit (e.g. the internal button and an external
        // state.submit() racing) must not fire two tokenisation requests.
        val mockResult = Result.success(TokenDetails(token = MobileSDKTestConstants.Card.MOCK_CARD_TOKEN, type = "token"))
        coEvery { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) } returns mockResult

        viewModel.tokeniseCard()
        viewModel.tokeniseCard() // Should be a no-op: state is already Loading synchronously.

        viewModel.stateFlow.first { it is GiftCardUIState.Success }
        coVerify(exactly = 1) { useCase(MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN, any()) }
    }

    @Test
    fun `gift card tokeniseCard should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val testScope = this
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
            // Loading is now set synchronously (before dispatching to IO — see tokeniseCard's
            // re-entrancy guard), so by the time a collector subscribes it has already moved past
            // Idle; start observing from Loading rather than expecting Idle first.
            viewModel.tokeniseCard()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // Loading state - before execution
                assertIs<GiftCardUIState.Loading>(awaitItem())
                // Let the launched coroutine actually reach the use-case call before verifying it.
                testScope.runCurrent()
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
    fun `validateAllFields should force show errors for both fields`() = runTest {
        // ACTION
        viewModel.validateAllFields()
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertTrue(inputState.cardNumberErrorOverwrite)
        assertTrue(inputState.pinErrorOverwrite)
    }

    @Test
    fun `updateCardNumber should reset cardNumberErrorOverwrite`() = runTest {
        viewModel.validateAllFields()
        // ACTION
        viewModel.updateCardNumber("62734010001104878")
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertFalse(inputState.cardNumberErrorOverwrite)
        // Unrelated field's overwrite flag is untouched
        assertTrue(inputState.pinErrorOverwrite)
    }

    @Test
    fun `updateCardPin should reset pinErrorOverwrite`() = runTest {
        viewModel.validateAllFields()
        // ACTION
        viewModel.updateCardPin("1234")
        val inputState = viewModel.inputStateFlow.first()
        // CHECK
        assertFalse(inputState.pinErrorOverwrite)
        // Unrelated field's overwrite flag is untouched
        assertTrue(inputState.cardNumberErrorOverwrite)
    }

    @Test
    fun `invalidFields and errorCount reflect empty form`() = runTest {
        val inputState = viewModel.inputStateFlow.first()
        assertEquals(
            listOf(GiftCardInputState.GiftCardField.CARD_NUMBER, GiftCardInputState.GiftCardField.PIN),
            inputState.invalidFields
        )
        assertEquals(2, inputState.errorCount)
    }

    @Test
    fun `invalidFields and errorCount are empty when form is valid`() = runTest {
        viewModel.updateCardNumber("62734010001104878")
        viewModel.updateCardPin("1234")
        val inputState = viewModel.inputStateFlow.first()
        assertTrue(inputState.invalidFields.isEmpty())
        assertEquals(0, inputState.errorCount)
    }

    @Test
    fun `GiftCardWidgetConfig activePrimaryButton defaults to true`() {
        assertTrue(GiftCardWidgetConfig(accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN).activePrimaryButton)
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
