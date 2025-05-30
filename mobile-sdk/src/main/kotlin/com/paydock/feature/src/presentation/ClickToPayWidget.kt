package com.paydock.feature.src.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.ClickToPayException
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.presentation.utils.ClickToPayJSBridge
import com.paydock.feature.src.presentation.viewmodels.ClickToPayViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function to render the SRC (Secure Remote Commerce) Click to Pay widget.
 *
 * @param modifier The modifier for the composable.
 * @param accessToken The access token used for authentication with the backend services.
 * @param serviceId Card Scheme Service ID for Click to Pay.
 * @param meta Data that configures the Click to Pay checkout.
 * @param completion Callback function invoked upon completion of the checkout process.
 */
@Composable
fun ClickToPayWidget(
    modifier: Modifier = Modifier,
    accessToken: String,
    serviceId: String,
    meta: ClickToPayMeta? = null,
    completion: (Result<String>) -> Unit
) {
    // Obtain instances of view models
    val viewModel: ClickToPayViewModel = koinViewModel()

    // Collect states for Click to PayC view models
    val uiState by viewModel.stateFlow.collectAsState()

    // Handle result and reset state
    LaunchedEffect(uiState::class) {
        // Handle error flow and display
        uiState.error?.let {
            // Send error state to the completion callback
            completion(Result.failure(it))
            viewModel.resetResultState()
        }

        // Handle OTT result and reset state
        uiState.token?.let { response ->
            // Send success state to the completion callback
            completion(Result.success(response))
            viewModel.resetResultState()
        }
    }

    // Render Click to Pay widget
    SdkTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier.fillMaxWidth().background(Theme.colors.background),
                verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                // Generate HTML string for Click to Pay checkout
                val htmlString = HtmlWidgetBuilder.createHtml(
                    config = WidgetConfig.ClickToPayConfig(
                        accessToken = accessToken,
                        serviceId = serviceId,
                        meta = meta
                    )
                )
                // Render WebView for Click to Pay
                SdkWebView(
                    webUrl = MobileSDKConstants.DEFAULT_WEB_URL, // Placeholder URL
                    data = htmlString,
                    jsBridge = ClickToPayJSBridge {
                        viewModel.updateSRCEvent(it)
                    },
                    shouldShowCustomLoader = true,
                ) { status, message ->
                    // Invoke the completion callback with the Click to Pay exception upon WebView error
                    completion(
                        Result.failure(
                            ClickToPayException.WebViewException(
                                code = status,
                                displayableMessage = message
                            )
                        )
                    )
                }
            }
        }
    }
}