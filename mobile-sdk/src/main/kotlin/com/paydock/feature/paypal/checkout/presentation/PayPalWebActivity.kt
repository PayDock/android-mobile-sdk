package com.paydock.feature.paypal.checkout.presentation

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.extensions.putMessageExtra
import com.paydock.core.presentation.extensions.putStatusExtra
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.paypal.checkout.presentation.components.PayPalWebView
import com.paydock.feature.paypal.checkout.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.checkout.presentation.utils.getCallbackUrlExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putCancellationStatusExtra
import com.paydock.feature.paypal.checkout.presentation.utils.putDecodedUrlExtra

/**
 * This activity handles the PayPal payment process by rendering a WebView for PayPal transactions.
 * It provides a back button handler to cancel the payment process if the user decides to navigate back.
 */
internal class PayPalWebActivity : ComponentActivity() {

    /**
     * Called when the activity is starting. It sets up the UI and handles the back-press action.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handles the back button press to cancel the payment process with a user-initiated status.
        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish(CancellationStatus.USER_INITIATED)
            }
        }
        // Adds the back button callback to the dispatcher.
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Retrieves the callback URL passed via the intent and ensures it is not null.
        val callbackUrl = requireNotNull(intent.getCallbackUrlExtra())

        setContent {
            // Applies the SDK theme.
            SdkTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            actions = {
                                IconButton(onClick = { finish(CancellationStatus.USER_INITIATED) }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close_circle),
                                        contentDescription = stringResource(id = R.string.content_desc_close_icon)
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    // Apply inner padding to avoid content overlapping with the TopAppBar
                    Box(modifier = Modifier.padding(innerPadding).background(Theme.colors.background)) {
                        // Stores and remembers the PayPal URL created from the callback URL.
                        val payPalUrl: String by remember(callbackUrl) {
                            mutableStateOf(createPayPalUrl(callbackUrl))
                        }
                        PayPalWebView(
                            payPayUrl = payPalUrl,
                            onSuccess = { decodedUrl ->
                                setResult(
                                    RESULT_OK,
                                    Intent().putDecodedUrlExtra(decodedUrl)
                                )
                                finish()
                            },
                            onFailure = { status, message ->
                                setResult(
                                    RESULT_CANCELED,
                                    Intent()
                                        .putStatusExtra(status)
                                        .putMessageExtra(message)
                                )
                                finish()
                            }
                        )
                    }
                }
            }
        }

        // Sets the window layout to match the parent dimensions.
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * Creates the PayPal URL by appending the necessary redirect parameter to the callback URL.
     *
     * @param callbackUrl The original callback URL for the payment.
     * @return The modified PayPal URL with the redirect parameter appended.
     */
    private fun createPayPalUrl(callbackUrl: String): String =
        "$callbackUrl&${MobileSDKConstants.PayPalConfig.REDIRECT_PARAM_NAME}" +
            "=${MobileSDKConstants.PayPalConfig.PAY_PAL_REDIRECT_PARAM_VALUE}"

    /**
     * Finishes the activity and sets the result to indicate a cancellation status.
     *
     * @param status The cancellation status to be included in the result intent.
     */
    private fun finish(status: CancellationStatus) {
        setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(status))
        finish()
    }
}