package com.paydock.feature.threeDS.standalone.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.threeDS.common.domain.model.ui.enums.TokenFormat
import com.paydock.feature.threeDS.common.domain.presentation.utils.ThreeDSTokenUtils
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.presentation.state.Standalone3DSUIState
import com.paydock.feature.threeDS.standalone.presentation.utils.Standalone3DSJSBridge
import com.paydock.feature.threeDS.standalone.presentation.viewmodels.Standalone3DSViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable widget for handling the Standalone 3D Secure (3DS) flow.
 *
 * This widget displays a WebView for the 3DS authentication process and manages UI state changes
 * using a `ThreeDSViewModel`. It supports theming, back button handling, and error reporting.
 *
 * @param token The 3DS token required for authentication.
 * @param completion A callback invoked with the result of the 3DS process, either success or failure.
 */
@Composable
fun Standalone3DSWidget(
    token: String,
    completion: (Result<Standalone3DSResult>) -> Unit,
) {
    val context = LocalContext.current
    val parsedToken = ThreeDSTokenUtils.extractToken(token)

    if (parsedToken == null) {
        // Handle the case where the token is invalid
        completion(
            Result.failure(
                Standalone3DSException.InvalidTokenException(
                    displayableMessage = context.getString(R.string.error_standalone_3ds_invalid_token)
                )
            )
        )
        return
    } else if (parsedToken.format != TokenFormat.STANDALONE_3DS) {
        // Handle the case where the token is invalid format
        completion(
            Result.failure(
                Standalone3DSException.InvalidTokenException(
                    displayableMessage = context.getString(R.string.error_standalone_3ds_invalid_token_format)
                )
            )
        )
        return
    }

    // Obtain instances of view models
    val viewModel: Standalone3DSViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { state ->
            handleUIState(
                state,
                viewModel,
                completion
            )
        }
    }

    // Apply the SdkTheme for consistent styling
    SdkTheme {
        // Display the 3DS WebView within the bottom sheet
        val htmlString = HtmlWidgetBuilder.createHtml(
            config = WidgetConfig.ThreeDSConfigBase.Standalone3DSConfig(token = token)
        )
        val jsBridge = remember {
            Standalone3DSJSBridge { eventResult ->
                viewModel.handleEventResult(eventResult)
            }
        }

        SdkWebView(
            webUrl = MobileSDKConstants.DEFAULT_WEB_URL,
            data = htmlString,
            jsBridge = jsBridge
        ) { status, message ->
            // Invoke the onWebViewError callback with the WebView ThreeDSException exception
            completion(Result.failure(Standalone3DSException.WebViewException(status, message)))
        }
    }
}

/**
 * Handles the UI state for the 3DS process.
 *
 * Depending on the state, this function either invokes the completion callback
 * with the result or clears the state to prevent reuse.
 *
 * @param uiState The current UI state of the 3DS process.
 * @param viewModel The ViewModel managing the 3DS state.
 * @param completion A callback invoked with the result of the 3DS process.
 */
private fun handleUIState(
    uiState: Standalone3DSUIState,
    viewModel: Standalone3DSViewModel,
    completion: (Result<Standalone3DSResult>) -> Unit,
) {
    when (uiState) {
        Standalone3DSUIState.Idle,
        Standalone3DSUIState.Loading -> Unit

        is Standalone3DSUIState.Error -> {
            completion(Result.failure(uiState.exception))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }

        is Standalone3DSUIState.Success -> {
            completion(Result.success(uiState.result))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }
    }
}