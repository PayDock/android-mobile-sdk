package com.paydock.feature.threeDS.integrated.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.Integrated3DSException
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.common.domain.model.ui.enums.TokenFormat
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSAppearanceDefaults
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSWidgetAppearance
import com.paydock.feature.threeDS.common.presentation.utils.ThreeDSTokenUtils
import com.paydock.feature.threeDS.integrated.domain.model.integration.Integrated3DSResult
import com.paydock.feature.threeDS.integrated.presentation.state.Integrated3DSUIState
import com.paydock.feature.threeDS.integrated.presentation.utils.Integrated3DSJSBridge
import com.paydock.feature.threeDS.integrated.presentation.viewmodels.Integrated3DSViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable widget for handling the Integrated 3D Secure (3DS) flow.
 *
 * This widget displays a WebView for the 3DS authentication process and manages UI state changes
 * using a `ThreeDSViewModel`. It supports theming, back button handling, and error reporting.
 *
 * @param config The configuration for the Integrated 3DS process, including the token.
 * @param completion A callback invoked with the result of the 3DS process, either success or failure.
 */
@Composable
fun Integrated3DSWidget(
    config: ThreeDSConfig,
    appearance: ThreeDSWidgetAppearance = ThreeDSAppearanceDefaults.appearance(),
    completion: (Result<Integrated3DSResult>) -> Unit,
) {
    val parsedToken = ThreeDSTokenUtils.extractToken(config.token)

    if (parsedToken == null) {
        // Handle the case where the token is invalid
        completion(
            Result.failure(
                Integrated3DSException.InvalidTokenException(
                    displayableMessage = MobileSDKConstants.Integrated3DSConfig.Errors.INVALID_TOKEN_ERROR
                )
            )
        )
        return
    } else if (parsedToken.format == TokenFormat.STANDALONE_3DS) {
        // Handle the case where the token is invalid format
        completion(
            Result.failure(
                Integrated3DSException.InvalidTokenException(
                    displayableMessage = MobileSDKConstants.Integrated3DSConfig.Errors.INVALID_TOKEN_FORMAT_ERROR
                )
            )
        )
        return
    }

    // Obtain instances of view models
    val viewModel: Integrated3DSViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { state ->
            handleUIState(state, viewModel, completion)
        }
    }

    // Apply the SdkTheme for consistent styling
    // Display the 3DS WebView within the bottom sheet
    val htmlString = HtmlWidgetBuilder.createHtml(
        config = WidgetConfig.ThreeDSConfigBase.Integrated3DSConfig(
            token = config.token
        )
    )
    val jsBridge = remember {
        Integrated3DSJSBridge { eventResult ->
            viewModel.handleEventResult(eventResult)
        }
    }

    SdkWebView(
        webUrl = MobileSDKConstants.DEFAULT_WEB_URL,
        data = htmlString,
        jsBridge = jsBridge,
        loaderAppearance = appearance.loader
    ) { status, message ->
        // Invoke the onWebViewError callback with the WebView ThreeDSException exception
        completion(Result.failure(Integrated3DSException.WebViewException(status, message)))
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
    uiState: Integrated3DSUIState,
    viewModel: Integrated3DSViewModel,
    completion: (Result<Integrated3DSResult>) -> Unit,
) {
    when (uiState) {
        Integrated3DSUIState.Idle,
        Integrated3DSUIState.Loading -> Unit

        is Integrated3DSUIState.Error -> {
            completion(Result.failure(uiState.exception))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }

        is Integrated3DSUIState.Success -> {
            completion(Result.success(uiState.result))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }
    }
}