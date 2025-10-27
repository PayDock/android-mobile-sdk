package com.paydock.feature.colespay.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paydock.MobileSDK
import com.paydock.R
import com.paydock.core.domain.mapper.mapToColesPayEnv
import com.paydock.core.presentation.extensions.putMessageExtra
import com.paydock.core.presentation.extensions.putStatusExtra
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.feature.colespay.presentation.components.ColesPayWebView
import com.paydock.feature.colespay.presentation.utils.CancellationStatus
import com.paydock.feature.colespay.presentation.utils.getClientIdExtra
import com.paydock.feature.colespay.presentation.utils.getOrderIdExtra
import com.paydock.feature.colespay.presentation.utils.putCancellationStatusExtra
import com.paydock.feature.colespay.presentation.utils.putOrderIdExtra

/**
 * An activity that handles the Coles Pay web payment flow.
 *
 * This activity launches a WebView to display the Coles Pay payment page and handles the
 * interaction between the user and the payment provider. It manages the lifecycle of the
 * payment process, including handling success, failure, and cancellation scenarios.
 *
 * Key responsibilities:
 * - Displays the Coles Pay payment page in a WebView.
 * - Handles navigation and user interactions within the WebView.
 * - Communicates the payment status (success, failure, cancellation) back to the calling activity.
 * - Manages the back press behavior to allow users to cancel the payment process.
 *
 * This activity is designed to be launched using an Intent and expects specific extras
 * (`colesPayOrderId` and `colesPayClientId`) to be provided for initiating the payment.
 * Upon completion, it returns a result (RESULT_OK or RESULT_CANCELED) along with
 * relevant data (e.g., order ID, error status, error message) to the calling activity.
 */
internal class ColesPayWebActivity : ComponentActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish(CancellationStatus.USER_INITIATED)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setContent {
            enableEdgeToEdge()
            // Applies the SDK theme.
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        actions = {
                            IconButton(onClick = { finish(CancellationStatus.USER_INITIATED) }) {
                                SdkIcon(
                                    painter = painterResource(id = R.drawable.ic_close_circle),
                                    contentDescription = stringResource(id = R.string.content_desc_close_icon)
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                // Apply inner padding to avoid content overlapping with the TopAppBar
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        // This caters for keyboard changes within compose
                        .consumeWindowInsets(paddingValues = innerPadding)
                        .imePadding()
                ) {
                    val colesPayOrderId = requireNotNull(intent.getOrderIdExtra())
                    val colesPayClientId = requireNotNull(intent.getClientIdExtra())
                    // Stores and remembers the ColesPay URL created from the callback URL.
                    val colesPayUrl: String by rememberSaveable(inputs = arrayOf(colesPayOrderId, colesPayClientId)) {
                        mutableStateOf(createColesPayUrl(colesPayOrderId, colesPayClientId))
                    }
                    ColesPayWebView(colesPayUrl = colesPayUrl, onSuccess = {
                        setResult(
                            RESULT_OK,
                            Intent().putOrderIdExtra(colesPayOrderId)
                        )
                        finish()
                    }, onFailure = { status, message ->
                        // If status maps to a known CancellationStatus enum, surface that explicitly
                        val cancellationStatus = CancellationStatus.entries.getOrNull(status)
                        if (cancellationStatus != null) {
                            setResult(
                                RESULT_CANCELED,
                                Intent()
                                    .putCancellationStatusExtra(cancellationStatus)
                            )
                        } else {
                            setResult(
                                RESULT_CANCELED,
                                Intent()
                                    .putStatusExtra(status)
                                    .putMessageExtra(message)
                            )
                        }
                        finish()
                    })
                }
            }
        }
        // Sets the window layout to match the parent dimensions.
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * Creates the Coles Pay URL for the payment process based on the callback URL.
     *
     * @param colesPayOrderId The Coles Pay orderId.
     * @param clientId The Merchant clientId.
     * @return The composed URL with Coles Pay parameters.
     */
    @Suppress("MaxLineLength")
    private fun createColesPayUrl(colesPayOrderId: String, clientId: String): String =
        MobileSDK.getInstance().environment.mapToColesPayEnv(colesPayOrderId, clientId)

    private fun finish(status: CancellationStatus) {
        setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(status))
        super.finish()
    }
}