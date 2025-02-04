package com.paydock.feature.paypal.vault.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.paypal.core.domain.model.ui.PayPalPaymentTokenDetails
import com.paydock.feature.paypal.core.domain.usecase.CreatePayPalVaultPaymentTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.CreateSetupTokenUseCase
import com.paydock.feature.paypal.core.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.presentation.state.PayPalVaultUIState
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalVaultViewModel
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
internal class PayPalVaultViewModelTest : BaseKoinUnitTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatchersProvider: DispatchersProvider by inject()
    private lateinit var createSetupTokenUseCase: CreateSetupTokenUseCase
    private lateinit var getPayPalClientIdUseCase: GetPayPalClientIdUseCase
    private lateinit var createPayPalVaultPaymentTokenUseCase: CreatePayPalVaultPaymentTokenUseCase

    private lateinit var viewModel: PayPalVaultViewModel
    private lateinit var config: PayPalVaultConfig

    companion object {
        const val MOCK_ACCESS_TOKEN = MobileSDKTestConstants.PayPalVault.MOCK_ACCESS_TOKEN
        const val MOCK_SETUP_TOKEN = MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN
        const val MOCK_CLIENT_ID = MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID
    }

    @Before
    fun setup() {
        config = PayPalVaultConfig(
            accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
        )
        createSetupTokenUseCase = mockk()
        getPayPalClientIdUseCase = mockk()
        createPayPalVaultPaymentTokenUseCase = mockk()
        viewModel = PayPalVaultViewModel(
            config,
            createSetupTokenUseCase,
            getPayPalClientIdUseCase,
            createPayPalVaultPaymentTokenUseCase,
            dispatchersProvider
        )
    }

    private fun preparePayPalCompleteSuccessFlow() {
        prepareCreateSetupTokenSuccess()
        prepareGetClientIdSuccess()
    }

    private fun prepareCreateSetupTokenFailure() {
        val mockError = PayPalVaultException.CreateSetupTokenException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "setup_token",
                    message = MobileSDKTestConstants.Errors.MOCK_SETUP_TOKEN_ERROR
                )
            )
        )
        val mockResult = Result.failure<String>(mockError)
        coEvery {
            createSetupTokenUseCase(
                any(),
                any()
            )
        } returns mockResult
    }

    private fun prepareCreateSetupTokenSuccess() {
        // Prepare Setup Token
        val mockSetupTokenResult = Result.success(MOCK_SETUP_TOKEN)
        coEvery {
            createSetupTokenUseCase(
                any(),
                any()
            )
        } returns mockSetupTokenResult
    }

    private fun prepareGetClientIdSuccess() {
        // Prepare Client ID
        val mockClientIdResult = Result.success(MOCK_CLIENT_ID)
        coEvery {
            getPayPalClientIdUseCase(
                eq(config.accessToken),
                eq(config.gatewayId)
            )
        } returns mockClientIdResult
    }

    private fun prepareGetClientIdFailure() {
        val mockError = PayPalVaultException.GetPayPalClientIdException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "gateway_client_id",
                    message = MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR
                )
            )
        )
        val mockResult = Result.failure<String>(mockError)
        coEvery {
            getPayPalClientIdUseCase(
                any(),
                any()
            )
        } returns mockResult
    }

    @Test
    fun `paypal vault token and clientId end-to-end flow should update isLoading, call 2 useCases, and update states on success`() =
        runTest {
            preparePayPalCompleteSuccessFlow()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.createPayPalSetupToken()
                // CHECK
                // Initial state
                assertIs<PayPalVaultUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatchers
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.LaunchIntent>(state)
                    assertEquals(MOCK_SETUP_TOKEN, state.setupToken)
                    assertEquals(MOCK_CLIENT_ID, state.clientId)
                }
                coVerify {
                    createSetupTokenUseCase(
                        any(),
                        any()
                    )
                }
                coVerify {
                    getPayPalClientIdUseCase(
                        any(),
                        any()
                    )
                }
            }
        }

    @Test
    fun `create setup token failure flow should update isLoading, call useCase, and update states on failure`() =
        runTest {
            prepareCreateSetupTokenFailure()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                viewModel.createPayPalSetupToken()
                // CHECK
                // Initial state
                assertIs<PayPalVaultUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { createSetupTokenUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Error>(state)
                    assertIs<PayPalVaultException.CreateSetupTokenException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_SETUP_TOKEN_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `get clientId failure flow should update isLoading, call useCase, and update states on failure`() =
        runTest {
            prepareCreateSetupTokenSuccess()
            prepareGetClientIdFailure()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                viewModel.createPayPalSetupToken()
                // CHECK
                // Initial state
                assertIs<PayPalVaultUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { getPayPalClientIdUseCase(any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Error>(state)
                    assertIs<PayPalVaultException.GetPayPalClientIdException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_CLIENT_ID_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `create payment source should update isLoading, call useCase, and update state on success`() =
        runTest {
            preparePayPalCompleteSuccessFlow()
            viewModel.createPayPalSetupToken()
            runCurrent() // Execute pending coroutine dispatchers

            val mockToken = MobileSDKTestConstants.PayPalVault.MOCK_PAYMENT_TOKEN
            val mockEmail = MobileSDKTestConstants.PayPalVault.MOCK_EMAIL
            val mockResult = Result.success(PayPalPaymentTokenDetails(token = mockToken, email = mockEmail))
            coEvery {
                createPayPalVaultPaymentTokenUseCase(
                    any(),
                    any(),
                    any()
                )
            } returns mockResult

            viewModel.stateFlow.test {
                viewModel.createPayPalPaymentSourceToken()
                // CHECK
                // Initial state (from previous states)
                assertIs<PayPalVaultUIState.LaunchIntent>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { createPayPalVaultPaymentTokenUseCase(any(), any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Success>(state)
                    assertEquals(mockToken, state.details.token)
                    assertEquals(mockEmail, state.details.email)
                }
            }
        }

    @Test
    fun `create payment source should update isLoading, call useCase, and update state on failure`() =
        runTest {
            preparePayPalCompleteSuccessFlow()
            viewModel.createPayPalSetupToken()
            runCurrent() // Execute pending coroutine dispatchers

            val mockError = PayPalVaultException.CreatePaymentTokenException(
                error = ApiErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    summary = ErrorSummary(
                        code = "payment_token_error",
                        message = MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR
                    )
                )
            )
            val mockResult = Result.failure<PayPalPaymentTokenDetails>(mockError)
            coEvery {
                createPayPalVaultPaymentTokenUseCase(
                    any(),
                    any(),
                    any()
                )
            } returns mockResult

            viewModel.stateFlow.test {
                viewModel.createPayPalPaymentSourceToken()
                // CHECK
                // Initial state (from previous states)
                assertIs<PayPalVaultUIState.LaunchIntent>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { createPayPalVaultPaymentTokenUseCase(any(), any(), any()) }
                // Result state - failure
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Error>(state)
                    assertIs<PayPalVaultException.CreatePaymentTokenException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_TOKENIZATION_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `resetResultState should reset UI state`() = runTest {
        preparePayPalCompleteSuccessFlow()
        viewModel.createPayPalSetupToken()
        runCurrent() // Execute pending coroutine dispatchers
        val mockToken = MobileSDKTestConstants.PayPalVault.MOCK_PAYMENT_TOKEN
        val mockEmail = MobileSDKTestConstants.PayPalVault.MOCK_EMAIL
        val mockResult = Result.success(PayPalPaymentTokenDetails(token = mockToken, email = mockEmail))
        coEvery {
            createPayPalVaultPaymentTokenUseCase(
                any(),
                any(),
                any()
            )
        } returns mockResult
        viewModel.createPayPalPaymentSourceToken()
        runCurrent()
        viewModel.stateFlow.test {
            assertIs<PayPalVaultUIState.Success>(awaitItem())
            // ACTION
            viewModel.resetResultState()
            // Result state - reset
            assertIs<PayPalVaultUIState.Idle>(awaitItem())
        }
    }
}