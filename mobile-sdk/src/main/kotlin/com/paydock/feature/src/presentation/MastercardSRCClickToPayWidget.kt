package com.paydock.feature.src.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.paydock.R
import com.paydock.core.domain.error.exceptions.MastercardSRCException
import com.paydock.core.domain.model.meta.MastercardSRCMeta
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.src.presentation.utils.MastercardSRCJSBridge
import com.paydock.feature.src.presentation.viewmodels.MastercardSRCViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function to render the Mastercard SRC (Secure Remote Commerce) Click to Pay widget.
 *
 * @param modifier The modifier for the composable.
 * @param serviceId Card Scheme Service ID for Mastercard SRC.
 * @param meta Data that configures the Mastercard SRC checkout.
 * @param completion Callback function invoked upon completion of the checkout process.
 */
@Suppress("LongMethod")
@Composable
fun MastercardSRCClickToPayWidget(
    modifier: Modifier = Modifier,
    serviceId: String,
    meta: MastercardSRCMeta? = null,
    completion: (Result<String>) -> Unit
) {
    val context = LocalContext.current

    // Obtain instances of view models
    val viewModel: MastercardSRCViewModel = koinViewModel()

    // Collect states for Mastercard SRC view models
    val uiState by viewModel.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()

    // Handle back button press
    BackHandler(true) {
        scope.launch {
            // Notify completion with failure and error message upon back press
            completion(
                Result.failure(
                    MastercardSRCException.CancellationException(
                        displayableMessage = context.getString(
                            R.string.error_mastercard_src_cancelled
                        )
                    )
                )
            )
            viewModel.resetResultState()
        }
    }

    // Handle result and reset state
    LaunchedEffect(uiState) {
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

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    // Render Mastercard SRC Click to Pay widget
    SdkTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                // Generate HTML string for Mastercard SRC checkout
                val htmlString = HtmlWidgetBuilder.createHtml(
                    config = WidgetConfig.MastercardSRCConfig(
                        serviceId = serviceId,
                        meta = meta
                    )
                )
                // Render WebView for Mastercard SRC
                SdkWebView(
                    webUrl = "https://paydock.com", // Placeholder URL
                    data = htmlString,
                    jsBridge = MastercardSRCJSBridge {
                        viewModel.updateSRCEvent(it)
                    },
                    showLoader = true,
                ) { status, message ->
                    // Invoke the completion callback with the Mastercard SRC exception upon WebView error
                    completion(
                        Result.failure(
                            MastercardSRCException.WebViewException(
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