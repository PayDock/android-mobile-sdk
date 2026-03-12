package com.paydock.feature.threeDS.integrated.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.MPGS3dsException
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.common.domain.model.ui.enums.TokenFormat
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSAppearanceDefaults
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSWidgetAppearance
import com.paydock.feature.threeDS.common.presentation.utils.ThreeDSTokenUtils
import com.paydock.feature.threeDS.integrated.domain.model.integration.MPGS3dsResult
import com.paydock.feature.threeDS.integrated.presentation.state.MPGS3dsUIState
import com.paydock.feature.threeDS.integrated.presentation.utils.MPGS3dsJSBridge
import com.paydock.feature.threeDS.integrated.presentation.viewmodels.MPGS3dsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable widget for handling the MPGS 3D Secure (3DS) flow.
 *
 * This widget displays a WebView for the MPGS 3DS authentication process and manages UI state changes
 * using a `MPGS3dsViewModel`. It supports theming, back button handling, and error reporting.
 *
 * @param config The configuration for the MPGS 3DS process, including the token.
 * @param completion A callback invoked with the result of the 3DS process, either success or failure.
 */
@Composable
fun MPGS3dsWidget(
    config: ThreeDSConfig,
    appearance: ThreeDSWidgetAppearance = ThreeDSAppearanceDefaults.appearance(),
    completion: (Result<MPGS3dsResult>) -> Unit,
) {
    val parsedToken = ThreeDSTokenUtils.extractToken(config.token)

    if (parsedToken == null) {
        // Handle the case where the token is invalid
        completion(
            Result.failure(
                MPGS3dsException.InvalidTokenException(
                    displayableMessage = MobileSDKConstants.MPGS3dsConfig.Errors.INVALID_TOKEN_ERROR
                )
            )
        )
        return
    } else if (parsedToken.format == TokenFormat.STANDALONE_3DS) {
        // Handle the case where the token is invalid format
        completion(
            Result.failure(
                MPGS3dsException.InvalidTokenException(
                    displayableMessage = MobileSDKConstants.MPGS3dsConfig.Errors.INVALID_TOKEN_FORMAT_ERROR
                )
            )
        )
        return
    }

    // Obtain instances of view models
    val viewModel: MPGS3dsViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { state ->
            handleUIState(state, viewModel, completion)
        }
    }

    // Apply the SdkTheme for consistent styling
    // Display the 3DS WebView within the bottom sheet
    val htmlString = HtmlWidgetBuilder.createHtml(
        config = WidgetConfig.ThreeDSConfigBase.MPGS3dsConfig(
            token = config.token
        )
    )
    val jsBridge = remember {
        MPGS3dsJSBridge { eventResult ->
            viewModel.handleEventResult(eventResult)
        }
    }

    SdkWebView(
        webUrl = MobileSDKConstants.DEFAULT_WEB_URL,
        data = htmlString,
        jsBridge = jsBridge,
        loaderAppearance = appearance.loader
    ) { status, message ->
        // Invoke the onWebViewError callback with the WebView MPGS3dsException exception
        completion(Result.failure(MPGS3dsException.WebViewException(status, message)))
    }
}

/**
 * Handles the UI state for the MPGS 3DS process.
 *
 * Depending on the state, this function either invokes the completion callback
 * with the result or clears the state to prevent reuse.
 *
 * @param uiState The current UI state of the 3DS process.
 * @param viewModel The ViewModel managing the 3DS state.
 * @param completion A callback invoked with the result of the 3DS process.
 */
private fun handleUIState(
    uiState: MPGS3dsUIState,
    viewModel: MPGS3dsViewModel,
    completion: (Result<MPGS3dsResult>) -> Unit,
) {
    when (uiState) {
        MPGS3dsUIState.Idle,
        MPGS3dsUIState.Loading -> Unit

        is MPGS3dsUIState.Error -> {
            completion(Result.failure(uiState.exception))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }

        is MPGS3dsUIState.Success -> {
            completion(Result.success(uiState.result))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }
    }
}