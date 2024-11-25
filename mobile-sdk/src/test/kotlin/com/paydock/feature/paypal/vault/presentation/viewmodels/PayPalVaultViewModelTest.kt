package com.paydock.feature.paypal.vault.presentation.viewmodels

import app.cash.turbine.test
import com.paydock.api.gateways.domain.usecase.GetPayPalClientIdUseCase
import com.paydock.api.tokens.domain.model.PayPalPaymentTokenDetails
import com.paydock.api.tokens.domain.model.SessionAuthToken
import com.paydock.api.tokens.domain.usecase.CreatePayPalVaultPaymentTokenUseCase
import com.paydock.api.tokens.domain.usecase.CreateSessionAuthTokenUseCase
import com.paydock.api.tokens.domain.usecase.CreateSetupTokenUseCase
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.network.dto.error.ApiErrorResponse
import com.paydock.core.network.dto.error.ErrorSummary
import com.paydock.core.utils.MainDispatcherRule
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
    private lateinit var createSessionAuthTokenUseCase: CreateSessionAuthTokenUseCase
    private lateinit var createSetupTokenUseCase: CreateSetupTokenUseCase
    private lateinit var getPayPalClientIdUseCase: GetPayPalClientIdUseCase
    private lateinit var createPayPalVaultPaymentTokenUseCase: CreatePayPalVaultPaymentTokenUseCase

    private lateinit var viewModel: PayPalVaultViewModel
    private lateinit var config: PayPalVaultConfig

    companion object {
        const val MOCK_ACCESS_TOKEN = MobileSDKTestConstants.PayPalVault.MOCK_ACCESS_TOKEN
        const val MOCK_ID_TOKEN = MobileSDKTestConstants.PayPalVault.MOCK_ID_TOKEN
        const val MOCK_SETUP_TOKEN = MobileSDKTestConstants.PayPalVault.MOCK_SETUP_TOKEN
        const val MOCK_CLIENT_ID = MobileSDKTestConstants.PayPalVault.MOCK_CLIENT_ID
    }

    @Before
    fun setup() {
        config = PayPalVaultConfig(
            accessToken = MobileSDKTestConstants.General.MOCK_ACCESS_TOKEN,
            gatewayId = MobileSDKTestConstants.General.MOCK_GATEWAY_ID
        )
        createSessionAuthTokenUseCase = mockk()
        createSetupTokenUseCase = mockk()
        getPayPalClientIdUseCase = mockk()
        createPayPalVaultPaymentTokenUseCase = mockk()
        viewModel = PayPalVaultViewModel(
            config,
            createSessionAuthTokenUseCase,
            createSetupTokenUseCase,
            getPayPalClientIdUseCase,
            createPayPalVaultPaymentTokenUseCase,
            dispatchersProvider
        )
    }

    private fun preparePayPalCompleteSuccessFlow() {
        prepareCreateSessionAuthSuccess()
        prepareCreateSetupTokenSuccess()
        prepareGetClientIdSuccess()
    }

    private fun prepareCreateSessionAuthSuccess() {
        // Prepare Auth Token
        val mockAuthTokenResult = Result.success(
            SessionAuthToken(
                accessToken = MOCK_ACCESS_TOKEN,
                idToken = MOCK_ID_TOKEN
            )
        )
        coEvery {
            createSessionAuthTokenUseCase(
                any(),
                any()
            )
        } returns mockAuthTokenResult
    }

    private fun prepareCreateSessionAuthTokenFailure() {
        val mockError = PayPalVaultException.CreateSessionAuthTokenException(
            error = ApiErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                summary = ErrorSummary(
                    code = "auth_token",
                    message = MobileSDKTestConstants.Errors.MOCK_AUTH_TOKEN_ERROR
                )
            )
        )
        val mockResult = Result.failure<SessionAuthToken>(mockError)
        coEvery {
            createSessionAuthTokenUseCase(
                any(),
                any()
            )
        } returns mockResult
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
    fun `resetResultState should reset UI state`() = runTest {
        viewModel.stateFlow.test {
            // ACTION
            viewModel.resetResultState()
            // Initial state
            // Result state - success
            assertIs<PayPalVaultUIState.Idle>(awaitItem())
        }
    }

    @Test
    fun `paypal vault token and clientId end-to-end flow should update isLoading, call 3 useCases, and update states on success`() =
        runTest {
            preparePayPalCompleteSuccessFlow()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                // ACTION
                viewModel.createCustomerSessionAuthToken()
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
                    createSessionAuthTokenUseCase(
                        any(),
                        any()
                    )
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
    fun `create session auth token failure flow should update isLoading, call useCase, and update state on failure`() =
        runTest {
            prepareCreateSessionAuthTokenFailure()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                viewModel.createCustomerSessionAuthToken()
                // CHECK
                // Initial state
                assertIs<PayPalVaultUIState.Idle>(awaitItem())
                // Loading state - before execution
                assertIs<PayPalVaultUIState.Loading>(awaitItem())
                runCurrent() // Execute pending coroutine dispatcher
                coVerify { createSessionAuthTokenUseCase(any(), any()) }
                // Result state - success
                awaitItem().let { state ->
                    assertIs<PayPalVaultUIState.Error>(state)
                    assertIs<PayPalVaultException.CreateSessionAuthTokenException>(state.exception)
                    assertEquals(
                        MobileSDKTestConstants.Errors.MOCK_AUTH_TOKEN_ERROR,
                        state.exception.message
                    )
                }
            }
        }

    @Test
    fun `create setup token failure flow should update isLoading, call useCase, and update states on failure`() =
        runTest {
            prepareCreateSessionAuthSuccess()
            prepareCreateSetupTokenFailure()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                viewModel.createCustomerSessionAuthToken()
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
            prepareCreateSessionAuthSuccess()
            prepareCreateSetupTokenSuccess()
            prepareGetClientIdFailure()
            // Allows for testing flow state
            viewModel.stateFlow.test {
                viewModel.createCustomerSessionAuthToken()
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
            viewModel.createCustomerSessionAuthToken()
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
            viewModel.createCustomerSessionAuthToken()
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
}