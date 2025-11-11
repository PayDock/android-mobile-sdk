package com.paydock.feature.paypal.checkout.presentation.viewmodels

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.core.utils.MainDispatcherRule
import com.paydock.feature.paypal.checkout.presentation.state.PayPalWebCheckoutState
import com.paydock.feature.paypal.checkout.presentation.viewmodel.PayPalWebCheckoutViewModel
import com.paydock.feature.paypal.core.presentation.PayPalWebClientManager
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFinishStartResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebStartCallback
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.inject
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
internal class PayPalWebCheckoutViewModelTest : BaseKoinUnitTest() {

    private val dispatchers: DispatchersProvider by inject()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK(relaxed = true)
    lateinit var mockActivity: AppCompatActivity

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `handleDeeplinkResult emits Success on finishStart success`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val mockClient = mockk<PayPalWebCheckoutClient>(relaxed = true)
        val mockManager = mockk<PayPalWebClientManager>(relaxed = true)
        every { mockManager.getOrCreateClient(any()) } returns mockClient

        val vm = PayPalWebCheckoutViewModel(savedStateHandle, dispatchers, mockManager)
        val intent = Intent("action")
        val success = mockk<PayPalWebCheckoutFinishStartResult.Success>(relaxed = true)
        every { success.orderId } returns "ORDER-1"
        every { success.payerId } returns "PAYER-1"
        every { mockClient.finishStart(intent) } returns success

        vm.handleDeeplinkResult(mockActivity, intent)

        assertIs<PayPalWebCheckoutState.Success>(vm.checkoutState.first())
    }

    @Test
    fun `handleDeeplinkResult emits Canceled on finishStart canceled`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val mockClient = mockk<PayPalWebCheckoutClient>(relaxed = true)
        val mockManager = mockk<PayPalWebClientManager>(relaxed = true)
        every { mockManager.getOrCreateClient(any()) } returns mockClient

        val vm = PayPalWebCheckoutViewModel(savedStateHandle, dispatchers, mockManager)
        val intent = Intent("action")
        val canceled = mockk<PayPalWebCheckoutFinishStartResult.Canceled>(relaxed = true)
        every { mockClient.finishStart(intent) } returns canceled

        vm.handleDeeplinkResult(mockActivity, intent)

        assertIs<PayPalWebCheckoutState.Canceled>(vm.checkoutState.first())
    }

    @Test
    fun `handleDeeplinkResult emits Failure on finishStart failure`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val mockClient = mockk<PayPalWebCheckoutClient>(relaxed = true)
        val mockManager = mockk<PayPalWebClientManager>(relaxed = true)
        every { mockManager.getOrCreateClient(any()) } returns mockClient

        val vm = PayPalWebCheckoutViewModel(savedStateHandle, dispatchers, mockManager)
        val intent = Intent("action")
        val failure = mockk<PayPalWebCheckoutFinishStartResult.Failure>(relaxed = true)
        val sdkError = mockk<PayPalSDKError>(relaxed = true)
        every { failure.error } returns sdkError
        every { mockClient.finishStart(intent) } returns failure

        vm.handleDeeplinkResult(mockActivity, intent)

        assertIs<PayPalWebCheckoutState.Failure>(vm.checkoutState.first())
    }

    @Test
    fun `handleDeeplinkResult emits Canceled on NoResult`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val mockManager = mockk<PayPalWebClientManager>(relaxed = true)
        every { mockManager.getOrCreateClient(any()) } returns null

        val vm = PayPalWebCheckoutViewModel(savedStateHandle, dispatchers, mockManager)
        val intent = Intent("action")

        vm.handleDeeplinkResult(mockActivity, intent)

        assertIs<PayPalWebCheckoutState.Canceled>(vm.checkoutState.first())
    }

    @Test
    fun `initiateCheckout success stores client for later finishStart`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val mockClient = mockk<PayPalWebCheckoutClient>(relaxed = true)
        val mockManager = mockk<PayPalWebClientManager>(relaxed = true)
        val presentSuccess = mockk<PayPalPresentAuthChallengeResult.Success>(relaxed = true)
        val callbackSlot = slot<PayPalWebStartCallback>()

        every { mockManager.getOrCreateClient(any(), any()) } returns mockClient
        every { mockClient.start(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onPayPalWebStartResult(presentSuccess)
        }

        val vm = PayPalWebCheckoutViewModel(savedStateHandle, dispatchers, mockManager)
        vm.initiateCheckout(mockActivity, "CLIENT", "ORDER", PayPalWebCheckoutFundingSource.PAYPAL)
        advanceUntilIdle()

        // Now invoke deeplink and expect Success path to be reachable
        every { mockManager.getOrCreateClient(any()) } returns mockClient
        val finishSuccess = mockk<PayPalWebCheckoutFinishStartResult.Success>(relaxed = true)
        every { finishSuccess.orderId } returns "ORDER-1"
        every { finishSuccess.payerId } returns "PAYER-1"
        every { mockClient.finishStart(any()) } returns finishSuccess

        vm.handleDeeplinkResult(mockActivity, Intent("action"))
        assertIs<PayPalWebCheckoutState.Success>(vm.checkoutState.first())
    }

    @Test
    fun `initiateCheckout failure sets Failure state`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val mockClient = mockk<PayPalWebCheckoutClient>(relaxed = true)
        val mockManager = mockk<PayPalWebClientManager>(relaxed = true)
        val failure = mockk<PayPalPresentAuthChallengeResult.Failure>(relaxed = true)
        val error = mockk<PayPalSDKError>(relaxed = true)
        val callbackSlot = slot<PayPalWebStartCallback>()

        every { failure.error } returns error
        every { mockManager.getOrCreateClient(any(), any()) } returns mockClient
        every { mockClient.start(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onPayPalWebStartResult(failure)
        }

        val vm = PayPalWebCheckoutViewModel(savedStateHandle, dispatchers, mockManager)
        vm.initiateCheckout(mockActivity, "CLIENT", "ORDER", PayPalWebCheckoutFundingSource.PAYPAL)
        advanceUntilIdle()
        assertIs<PayPalWebCheckoutState.Failure>(vm.checkoutState.first())
    }

    @Test
    fun `onCleared resets internal state`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val mockManager = mockk<PayPalWebClientManager>(relaxed = true)
        every { mockManager.clear() } returns Unit

        val vm = spyk(PayPalWebCheckoutViewModel(savedStateHandle, dispatchers, mockManager))

        val method = vm.javaClass.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(vm)

        io.mockk.verify { mockManager.clear() }
    }
}
