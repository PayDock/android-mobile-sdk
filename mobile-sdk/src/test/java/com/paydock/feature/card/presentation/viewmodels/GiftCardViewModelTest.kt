package com.paydock.feature.card.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.network.error.ApiErrorResponse
import com.paydock.core.data.network.error.ErrorSummary
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.ApiException
import com.paydock.core.domain.error.exceptions.GiftCardException
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.card.domain.model.TokenisedCardDetails
import com.paydock.feature.card.domain.usecase.TokeniseGiftCardUseCase
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
class GiftCardViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()

    private lateinit var viewModel: GiftCardViewModel
    private lateinit var useCase: TokeniseGiftCardUseCase

    @Before
    fun setup() {
        useCase = mockk()
        viewModel = GiftCardViewModel(useCase, dispatchersProvider)
    }

    @Test
    fun `setStorePin should update storePin`() = runTest {
        val storePin = false
        // ACTION
        viewModel.setStorePin(storePin)
        // CHECK
        val state = viewModel.stateFlow.first()
        assertEquals(storePin, state.storePin)
    }

    @Test
    fun `updateCardNumber should update cardNumber and clear error`() = runTest {
        val newNumber = "62734010001104878"
        // ACTION
        viewModel.updateCardNumber(newNumber)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newNumber, state.cardNumber)
        assertNull(state.error)
    }

    @Test
    fun `updatePin should update pin and clear error`() = runTest {
        val newSecurityCode = "1234"
        viewModel.updateCardPin(newSecurityCode)
        val state = viewModel.stateFlow.first()
        // CHECK
        assertEquals(newSecurityCode, state.pin)
        assertNull(state.error)
    }

    @Test
    fun `updating gift card details should have valid data`() = runTest {
        // ACTION - Valid card details data
        viewModel.updateCardNumber("62734010001104878")
        viewModel.updateCardPin("1234")
        // CHECK
        val state = viewModel.stateFlow.first()
        assertTrue(state.isDataValid)
    }

    @Test
    fun `updating gift card details should have invalid data`() = runTest {
        // ACTION - Invalid card details data
        viewModel.updateCardNumber("4111abc") // invalid characters
        viewModel.updateCardPin("1234")
        // CHECK
        val state = viewModel.stateFlow.first()
        assertFalse(state.isDataValid)
    }

    @Test
    fun `gift card tokeniseCard should update isLoading, call useCase, and update state on success`() =
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
    fun `gift card tokeniseCard should update isLoading, call useCase, and update state on failure`() =
        runTest {
            val mockError = ApiException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "invalid_card",
                        message = MobileSDKTestConstants.Errors.MOCK_INVALID_CARD_DETAILS_ERROR
                    )
                )
            )
            val mockResult = Result.failure<TokenisedCardDetails>(mockError)
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
                // Resul state - failure
                awaitItem().let { state ->
                    assertFalse(state.isLoading)
                    assertNull(state.token)
                    assertNotNull(state.error)
                    assertIs<GiftCardException.TokenisingCardException>(state.error)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_INVALID_CARD_DETAILS_ERROR,
                        state.error.message
                    )
                }
            }
        }
}
