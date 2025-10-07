package com.paydock.feature.paypal.checkout.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paydock.core.MobileSDKConstants
import com.paydock.core.extensions.castAs
import com.paydock.core.presentation.extensions.positionAwareImePadding
import com.paydock.core.presentation.extensions.putMessageExtra
import com.paydock.core.presentation.extensions.putStatusExtra
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.paypal.checkout.presentation.state.PayPalWebCheckoutState
import com.paydock.feature.paypal.checkout.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.checkout.presentation.utils.getClientIdExtra
import com.paydock.feature.paypal.checkout.presentation.utils.getFundingSourceExtra
import com.paydock.feature.paypal.checkout.presentation.utils.getOrderIdExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putCancellationStatusExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putPayerIdExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putPaymentMethodIdExtra
import com.paydock.feature.paypal.checkout.presentation.viewmodel.PayPalWebCheckoutViewModel
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that hosts the PayPal Web SDK checkout flow.
 *
 * - Reads `clientId` and `orderId` from the launching intent extras (via intent extensions).
 * - Delegates orchestration to [PayPalWebCheckoutViewModel].
 * - Observes [PayPalWebCheckoutState] and returns the result to the caller using intent extras.
 */
internal class PayPalWebCheckoutActivity : AppCompatActivity() {

    private val viewModel: PayPalWebCheckoutViewModel by viewModel()
    private var hasStartedCheckout: Boolean = false

    /**
     * Initializes the Compose content, validates required inputs, and triggers the SDK start.
     *
     * If either `clientId` or `orderId` is missing, the activity returns `RESULT_CANCELED` with
     * an informative message and finishes.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasStartedCheckout = savedInstanceState?.getBoolean(KEY_HAS_STARTED) ?: false
        setContent {
            enableEdgeToEdge()
            val checkoutState by viewModel.checkoutState.collectAsState()
            var shouldStartPayPal by remember { mutableStateOf(!hasStartedCheckout) }

            SideEffect {
                if (shouldStartPayPal) {
                    val clientId = intent.getClientIdExtra().orEmpty()
                    val orderId = intent.getOrderIdExtra().orEmpty()
                    if (clientId.isBlank() || orderId.isBlank()) {
                        setResult(
                            RESULT_CANCELED,
                            Intent()
                                .putStatusExtra(-1)
                                .putMessageExtra("Missing PayPal clientId or orderId")
                        )
                        finish()
                    } else {
                        try {
                            val fundingSourceString = intent.getFundingSourceExtra().orEmpty()
                            val fundingSource = PayPalWebCheckoutFundingSource.valueOf(fundingSourceString)

                            viewModel.initiateCheckout(
                                this@PayPalWebCheckoutActivity,
                                clientId, orderId, fundingSource
                            )

                            shouldStartPayPal = false
                            hasStartedCheckout = true
                        } catch (e: IllegalArgumentException) {
                            // Log and propagate a user-friendly error to avoid swallowing exceptions
                            android.util.Log.w(
                                MobileSDKConstants.MOBILE_SDK_TAG,
                                "Invalid PayPal funding source: ${intent.getFundingSourceExtra().orEmpty()}",
                                e
                            )
                            setResult(
                                RESULT_CANCELED,
                                Intent()
                                    .putStatusExtra(-1)
                                    .putMessageExtra("${intent.getFundingSourceExtra().orEmpty()} not a valid funding source.")
                            )
                            finish()
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .positionAwareImePadding(),
                contentAlignment = Alignment.Center
            ) {
                when (checkoutState) {
                    is PayPalWebCheckoutState.Idle -> SdkLoader()
                    is PayPalWebCheckoutState.Canceled -> {
                        setResult(
                            RESULT_CANCELED,
                            Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED)
                        )
                        finish()
                    }
                    is PayPalWebCheckoutState.Failure -> {
                        val error = (checkoutState as PayPalWebCheckoutState.Failure).error
                        setResult(
                            RESULT_CANCELED,
                            Intent()
                                .putStatusExtra(error.code)
                                .putMessageExtra(error.errorDescription)
                        )
                        finish()
                    }
                    is PayPalWebCheckoutState.Success -> {
                        val success = checkoutState.castAs<PayPalWebCheckoutState.Success>()
                        setResult(
                            RESULT_OK,
                            Intent()
                                .putPaymentMethodIdExtra(success.paymentMethodId)
                                .putPayerIdExtra(success.payerId)
                        )
                        finish()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_HAS_STARTED, hasStartedCheckout)
    }

    /**
     * Receives deep link intents from the PayPal flow and forwards them to the ViewModel.
     *
     * Detects the user-cancel deep link and finishes accordingly.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        val data = intent.data
        val host = data?.host
        val pathSegment = data?.pathSegments?.firstOrNull()
        if (host == "checkout" && pathSegment == "cancel") {
            setResult(
                RESULT_CANCELED,
                Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED)
            )
            finish()
            return
        }

        viewModel.handleDeeplinkResult(this, intent)
    }

    /**
     * Provides a fallback for cases where the user returns without a redirect intent.
     *
     * If the activity resumes a second time without a deep link, it is treated as a cancellation.
     */
    override fun onResume() {
        super.onResume()
        val data = intent?.data
        val host = data?.host
        val pathSegment = data?.pathSegments?.firstOrNull()
        if (host == "checkout" && pathSegment == "cancel") {
            setResult(
                RESULT_CANCELED,
                Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED)
            )
            finish()
            return
        }

        // Fallback: if returning without redirect intent and flow was already started, treat as cancel
        if (hasStartedCheckout && data == null) {
            setResult(
                RESULT_CANCELED,
                Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED)
            )
            finish()
            return
        }
    }

    private companion object {
        const val KEY_HAS_STARTED: String = "paypal.web_checkout.has_started"
    }
}
