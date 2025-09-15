package com.paydock.feature.paypal.checkout.presentation.viewmodels

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.paydock.core.BaseKoinUnitTest
import com.paydock.core.data.util.DispatchersProvider
import com.paydock.feature.paypal.checkout.presentation.viewmodel.PayPalWebCheckoutViewModel
import com.paydock.feature.paypal.checkout.presentation.state.PayPalWebCheckoutState
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFinishStartResult
import com.paypal.android.corepayments.PayPalSDKError
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import com.paydock.core.utils.MainDispatcherRule
import org.junit.Rule
import org.junit.Before
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
        val vm = spyk(PayPalWebCheckoutViewModel(dispatchers))
        val client = io.mockk.mockk<PayPalWebCheckoutClient>(relaxed = true)
        // Set internal fields via reflection
        vm.apply {
            val authField = javaClass.getDeclaredField("authState"); authField.isAccessible = true; authField.set(this, "auth")
            val clientField = javaClass.getDeclaredField("paypalClient"); clientField.isAccessible = true; clientField.set(this, client)
        }
        val intent = Intent("action")
        val success = io.mockk.mockk<PayPalWebCheckoutFinishStartResult.Success>(relaxed = true)
        every { success.orderId } returns "ORDER-1"
        every { success.payerId } returns "PAYER-1"
        every { client.finishStart(intent, any()) } returns success

        vm.handleDeeplinkResult(intent)

        assertIs<PayPalWebCheckoutState.Success>(vm.checkoutState.first())
    }

    @Test
    fun `handleDeeplinkResult emits Canceled on finishStart canceled`() = runTest {
        val vm = spyk(PayPalWebCheckoutViewModel(dispatchers))
        val client = io.mockk.mockk<PayPalWebCheckoutClient>(relaxed = true)
        vm.apply {
            val authField = javaClass.getDeclaredField("authState"); authField.isAccessible = true; authField.set(this, "auth")
            val clientField = javaClass.getDeclaredField("paypalClient"); clientField.isAccessible = true; clientField.set(this, client)
        }
        val intent = Intent("action")
        val canceled = io.mockk.mockk<PayPalWebCheckoutFinishStartResult.Canceled>(relaxed = true)
        every { client.finishStart(intent, any()) } returns canceled

        vm.handleDeeplinkResult(intent)

        assertIs<PayPalWebCheckoutState.Canceled>(vm.checkoutState.first())
    }

    @Test
    fun `handleDeeplinkResult emits Failure on finishStart failure`() = runTest {
        val vm = spyk(PayPalWebCheckoutViewModel(dispatchers))
        val client = io.mockk.mockk<PayPalWebCheckoutClient>(relaxed = true)
        vm.apply {
            val authField = javaClass.getDeclaredField("authState"); authField.isAccessible = true; authField.set(this, "auth")
            val clientField = javaClass.getDeclaredField("paypalClient"); clientField.isAccessible = true; clientField.set(this, client)
        }
        val intent = Intent("action")
        val failure = io.mockk.mockk<PayPalWebCheckoutFinishStartResult.Failure>(relaxed = true)
        val sdkError = io.mockk.mockk<PayPalSDKError>(relaxed = true)
        every { failure.error } returns sdkError
        every { client.finishStart(intent, any()) } returns failure

        vm.handleDeeplinkResult(intent)

        assertIs<PayPalWebCheckoutState.Failure>(vm.checkoutState.first())
    }

    @Test
    fun `handleDeeplinkResult emits Canceled on NoResult`() = runTest {
        val vm = spyk(PayPalWebCheckoutViewModel(dispatchers))
        // Set authState but leave client null to trigger NoResult path via Elvis operator
        vm.apply { val authField = javaClass.getDeclaredField("authState"); authField.isAccessible = true; authField.set(this, "auth") }
        val intent = Intent("action")

        vm.handleDeeplinkResult(intent)

        assertIs<PayPalWebCheckoutState.Canceled>(vm.checkoutState.first())
    }

    @Test
    fun `initiateCheckout success stores authState`() = runTest {
        val present = io.mockk.mockk<com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult.Success>(relaxed = true)
        every { present.authState } returns "auth"
        val vm = PayPalWebCheckoutViewModel(
            dispatchers,
            coreConfigProvider = { _: String -> io.mockk.mockk(relaxed = true) },
            clientProvider = { _: AppCompatActivity, _: com.paypal.android.corepayments.CoreConfig, _: String ->
            val client = io.mockk.mockk<PayPalWebCheckoutClient>(relaxed = true)
            every { client.start(any(), any()) } returns present
            client
        }
        )
        vm.initiateCheckout(mockActivity, "CLIENT", "ORDER")
        advanceUntilIdle()
        // Now invoke deeplink and expect Success path to be reachable
        val finishSuccess = io.mockk.mockk<PayPalWebCheckoutFinishStartResult.Success>(relaxed = true)
        every { finishSuccess.orderId } returns "ORDER-1"
        every { finishSuccess.payerId } returns "PAYER-1"
        val clientField = vm.javaClass.getDeclaredField("paypalClient"); clientField.isAccessible = true
        val client = clientField.get(vm) as PayPalWebCheckoutClient
        every { client.finishStart(any(), "auth") } returns finishSuccess
        vm.handleDeeplinkResult(Intent("action"))
        assertIs<PayPalWebCheckoutState.Success>(vm.checkoutState.first())
    }

    @Test
    fun `initiateCheckout failure sets Failure state`() = runTest {
        val failure = io.mockk.mockk<com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult.Failure>(relaxed = true)
        val error = io.mockk.mockk<PayPalSDKError>(relaxed = true)
        every { failure.error } returns error
        val vm = PayPalWebCheckoutViewModel(
            dispatchers,
            coreConfigProvider = { _: String -> io.mockk.mockk(relaxed = true) },
            clientProvider = { _: AppCompatActivity, _: com.paypal.android.corepayments.CoreConfig, _: String ->
            val client = io.mockk.mockk<PayPalWebCheckoutClient>(relaxed = true)
            every { client.start(any(), any()) } returns failure
            client
        }
        )
        vm.initiateCheckout(mockActivity, "CLIENT", "ORDER")
        advanceUntilIdle()
        assertIs<PayPalWebCheckoutState.Failure>(vm.checkoutState.first())
    }

    @Test
    fun `onCleared resets internal state`() = runTest {
        val vm = spyk(PayPalWebCheckoutViewModel(dispatchers))
        // Pre-set internal fields
        vm.apply {
            val authField = javaClass.getDeclaredField("authState"); authField.isAccessible = true; authField.set(this, "auth")
            val clientField = javaClass.getDeclaredField("paypalClient"); clientField.isAccessible = true; clientField.set(this, io.mockk.mockk<PayPalWebCheckoutClient>(relaxed = true))
        }

        val method = vm.javaClass.getDeclaredMethod("onCleared"); method.isAccessible = true; method.invoke(vm)

        val authField = vm.javaClass.getDeclaredField("authState"); authField.isAccessible = true
        val clientField = vm.javaClass.getDeclaredField("paypalClient"); clientField.isAccessible = true
        assert(authField.get(vm) == null)
        assert(clientField.get(vm) == null)
    }
}


