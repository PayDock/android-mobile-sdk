package com.paydock.feature.flypay.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
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
import com.paydock.MobileSDK
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.model.Environment
import com.paydock.core.presentation.extensions.putMessageExtra
import com.paydock.core.presentation.extensions.putStatusExtra
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.flypay.presentation.components.FlyPayWebView
import com.paydock.feature.flypay.presentation.utils.CancellationStatus
import com.paydock.feature.flypay.presentation.utils.getClientIdExtra
import com.paydock.feature.flypay.presentation.utils.getOrderIdExtra
import com.paydock.feature.flypay.presentation.utils.putCancellationStatusExtra
import com.paydock.feature.flypay.presentation.utils.putOrderIdExtra

internal class FlyPayWebActivity : ComponentActivity() {
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
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val flyPayOrderId = requireNotNull(intent.getOrderIdExtra())
                        val flyPayClientId = requireNotNull(intent.getClientIdExtra())
                        // Stores and remembers the FlyPay URL created from the callback URL.
                        val flyPayUrl: String by remember(flyPayOrderId, flyPayClientId) {
                            mutableStateOf(createFlyPayUrl(flyPayOrderId, flyPayClientId))
                        }
                        FlyPayWebView(flyPayUrl = flyPayUrl, onSuccess = {
                            setResult(
                                Activity.RESULT_OK,
                                Intent().putOrderIdExtra(flyPayOrderId)
                            )
                            finish()
                        }, onFailure = { status, message ->
                            setResult(
                                Activity.RESULT_CANCELED,
                                Intent()
                                    .putStatusExtra(status)
                                    .putMessageExtra(message)
                            )
                            finish()
                        })
                    }
                }
            }
        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * Creates the FlyPay URL for the payment process based on the callback URL.
     *
     * @param flyPayOrderId The FlyPay orderId.
     * @param clientId The Merchant clientId.
     * @return The composed URL with FlyPay parameters.
     */
    @Suppress("MaxLineLength")
    private fun createFlyPayUrl(flyPayOrderId: String, clientId: String): String =
        when (MobileSDK.getInstance().environment) {
            Environment.PRODUCTION -> "https://checkout.flypay.com.au/?orderId=$flyPayOrderId&redirectUrl=${MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL}&mode=default&clientId=$clientId"
            else -> "https://checkout.sandbox.cxbflypay.com.au/?orderId=$flyPayOrderId&redirectUrl=${MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL}&mode=default&clientId=$clientId" // default to sandbox
        }

    private fun finish(status: CancellationStatus) {
        setResult(Activity.RESULT_CANCELED, Intent().putCancellationStatusExtra(status))
        finish()
    }
}