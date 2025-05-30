package com.paydock.feature.colespay.presentation

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
import com.paydock.MobileSDK
import com.paydock.R
import com.paydock.core.domain.mapper.mapToColesPayEnv
import com.paydock.core.presentation.extensions.putMessageExtra
import com.paydock.core.presentation.extensions.putStatusExtra
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.colespay.presentation.components.ColesPayWebView
import com.paydock.feature.colespay.presentation.utils.CancellationStatus
import com.paydock.feature.colespay.presentation.utils.getClientIdExtra
import com.paydock.feature.colespay.presentation.utils.getOrderIdExtra
import com.paydock.feature.colespay.presentation.utils.putCancellationStatusExtra
import com.paydock.feature.colespay.presentation.utils.putOrderIdExtra

internal class ColesPayWebActivity : ComponentActivity() {
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
                    Box(modifier = Modifier.padding(innerPadding).background(Theme.colors.background)) {
                        val colesPayOrderId = requireNotNull(intent.getOrderIdExtra())
                        val colesPayClientId = requireNotNull(intent.getClientIdExtra())
                        // Stores and remembers the Coles Pay URL created from the callback URL.
                        val colesPayUrl: String by remember(colesPayOrderId, colesPayClientId) {
                            mutableStateOf(createColesPayUrl(colesPayOrderId, colesPayClientId))
                        }
                        ColesPayWebView(colesPayUrl = colesPayUrl, onSuccess = {
                            setResult(
                                RESULT_OK,
                                Intent().putOrderIdExtra(colesPayOrderId)
                            )
                            finish()
                        }, onFailure = { status, message ->
                            setResult(
                                RESULT_CANCELED,
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
        finish()
    }
}