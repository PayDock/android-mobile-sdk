package com.paydock.feature.threeDS.standalone.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.Standalone3DSException
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.loader.SdkOverlayLoader
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.feature.threeDS.common.domain.integration.ThreeDSConfig
import com.paydock.feature.threeDS.common.domain.model.ui.enums.TokenFormat
import com.paydock.feature.threeDS.common.presentation.utils.ThreeDSTokenUtils
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.presentation.state.Standalone3DSUIState
import com.paydock.feature.threeDS.standalone.presentation.ui.StandaloneThreeDSWebView
import com.paydock.feature.threeDS.standalone.presentation.ui.StandaloneThreeDSWidgetAppearance
import com.paydock.feature.threeDS.standalone.presentation.ui.StandaloneThreeDSWidgetAppearanceDefaults
import com.paydock.feature.threeDS.standalone.presentation.utils.Standalone3DSJSBridge
import com.paydock.feature.threeDS.standalone.presentation.utils.Standalone3DSLoadingController
import com.paydock.feature.threeDS.standalone.presentation.viewmodels.Standalone3DSViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable widget for handling the Standalone 3D Secure (3DS) flow.
 *
 * This widget displays a WebView for the 3DS authentication process and manages UI state changes
 * using a `ThreeDSViewModel`. It supports theming, back button handling, and error reporting.
 *
 * Loading: the loader (or [loadingDelegate]) starts when the widget launches and stays up through
 * device fingerprinting and while a challenge page is loading. It finishes once the challenge page is
 * visible, starts again once the shopper completed the challenge while the result is confirmed, and
 * finishes on the final result (success, reject or error). A decoupled authentication finishes it
 * (use [onProgress] to show the shopper how to approve). [loadingDelegate] start/finish calls are
 * always balanced.
 *
 * @param modifier The modifier to apply to this widget.
 * @param config The configuration for the 3DS process, including the token.
 * @param appearance The appearance settings for the widget.
 * @param loadingDelegate An optional [WidgetLoadingDelegate] for overriding the default loader
 *    behavior during tokenization or other async operations.
 * @param onProgress An optional callback invoked with intermediate [Standalone3DSProgress] events
 *    (challenge started, challenge loaded, challenge completed, decoupled). It never receives the final
 *    result, which is always delivered to [completion].
 * @param completion A callback invoked with the result of the 3DS process, either success or failure.
 */
@Composable
fun Standalone3DSWidget(
    modifier: Modifier = Modifier,
    config: ThreeDSConfig,
    appearance: StandaloneThreeDSWidgetAppearance = StandaloneThreeDSWidgetAppearanceDefaults.appearance(),
    loadingDelegate: WidgetLoadingDelegate? = null,
    onProgress: ((Standalone3DSProgress) -> Unit)? = null,
    completion: (Result<Standalone3DSResult>) -> Unit,
) {
    val parsedToken = ThreeDSTokenUtils.extractToken(config.token)

    if (parsedToken == null) {
        // Handle the case where the token is invalid
        completion(
            Result.failure(
                Standalone3DSException.InvalidTokenException(
                    displayableMessage = MobileSDKConstants.Standalone3DSConfig.Errors.INVALID_TOKEN_ERROR
                )
            )
        )
        return
    } else if (parsedToken.format != TokenFormat.STANDALONE_3DS) {
        // Handle the case where the token is invalid format
        completion(
            Result.failure(
                Standalone3DSException.InvalidTokenException(
                    displayableMessage = MobileSDKConstants.Standalone3DSConfig.Errors.INVALID_TOKEN_FORMAT_ERROR
                )
            )
        )
        return
    }

    // Obtain instances of view models
    val viewModel: Standalone3DSViewModel = koinViewModel()
    // Loading starts as soon as the widget launches (see LaunchedEffect below)
    val loadingController = remember { Standalone3DSLoadingController() }
    val currentLoadingDelegate by rememberUpdatedState(loadingDelegate)
    val currentOnProgress by rememberUpdatedState(onProgress)
    val currentCompletion by rememberUpdatedState(completion)

    // Only notifies on actual transitions, so delegate start/finish calls always stay balanced
    val updateLoading: (Boolean) -> Unit = { loading ->
        loadingController.update(loading, currentLoadingDelegate)
    }

    LaunchedEffect(Unit) {
        loadingController.start(currentLoadingDelegate)
        viewModel.eventFlow.collect { state ->
            handleUIState(
                state,
                viewModel,
                onLoadingChange = updateLoading,
                onProgress = { progress -> currentOnProgress?.invoke(progress) },
                completion = { result -> currentCompletion(result) }
            )
        }
    }

    // Apply the SdkTheme for consistent styling
    // Display the 3DS WebView within the bottom sheet
    val htmlString = HtmlWidgetBuilder.createHtml(
        config = WidgetConfig.ThreeDSConfigBase.Standalone3DSConfig(token = config.token)
    )
    val jsBridge = remember {
        Standalone3DSJSBridge { eventResult ->
            viewModel.handleEventResult(eventResult)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        StandaloneThreeDSWebView(
            webUrl = MobileSDKConstants.DEFAULT_WEB_URL,
            data = htmlString,
            jsBridge = jsBridge
        ) { status, message ->
            updateLoading(false)
            // Invoke the onWebViewError callback with the WebView ThreeDSException exception
            currentCompletion(Result.failure(Standalone3DSException.WebViewException(status, message)))
        }
        if (loadingController.isLoading && loadingDelegate == null) {
            SdkOverlayLoader(
                appearance = appearance.loader
            )
        }
    }
}

/**
 * Handles the UI state for the 3DS process.
 *
 * Depending on the state, this function either updates the loading state, forwards progress,
 * or invokes the completion callback with the result and clears the state to prevent reuse.
 *
 * @param uiState The current UI state of the 3DS process.
 * @param viewModel The ViewModel managing the 3DS state.
 * @param onLoadingChange Invoked when the loading indicator should start (`true`) or finish (`false`).
 * @param onProgress Invoked with intermediate progress events.
 * @param completion A callback invoked with the result of the 3DS process.
 */
private fun handleUIState(
    uiState: Standalone3DSUIState,
    viewModel: Standalone3DSViewModel,
    onLoadingChange: (Boolean) -> Unit,
    onProgress: (Standalone3DSProgress) -> Unit,
    completion: (Result<Standalone3DSResult>) -> Unit,
) {
    when (uiState) {
        is Standalone3DSUIState.Idle -> Unit // No action needed for idle state.
        is Standalone3DSUIState.Loading -> onLoadingChange(uiState.isLoading)
        is Standalone3DSUIState.Progress -> onProgress(uiState.progress)

        is Standalone3DSUIState.Success -> {
            // Invoke completion with the event result (loading changes are emitted beforehand).
            completion(Result.success(uiState.result))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }

        is Standalone3DSUIState.Error -> {
            // Invoke completion with failure result (the loader is finished beforehand).
            completion(Result.failure(uiState.exception))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }
    }
}
