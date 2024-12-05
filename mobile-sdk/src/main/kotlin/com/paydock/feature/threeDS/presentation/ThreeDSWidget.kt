package com.paydock.feature.threeDS.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.paydock.R
import com.paydock.core.domain.error.exceptions.ThreeDSException
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.threeDS.domain.model.integration.ThreeDSResult
import com.paydock.feature.threeDS.presentation.state.ThreeDSUIState
import com.paydock.feature.threeDS.presentation.utils.ThreeDSJSBridge
import com.paydock.feature.threeDS.presentation.viewmodels.ThreeDSViewModel
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable widget for handling the 3D Secure (3DS) flow.
 *
 * This widget displays a WebView for the 3DS authentication process and manages UI state changes
 * using a `ThreeDSViewModel`. It supports theming, back button handling, and error reporting.
 *
 * @param token The 3DS token required for authentication.
 * @param completion A callback invoked with the result of the 3DS process, either success or failure.
 */
@Composable
fun ThreeDSWidget(
    token: String,
    completion: (Result<ThreeDSResult>) -> Unit,
) {
    val context = LocalContext.current

    // Obtain instances of view models
    val viewModel: ThreeDSViewModel = koinViewModel()

    // Collect states for Click to Pay view models
    val uiState by viewModel.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()

    // Handle back button press
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var backPressHandled by remember { mutableStateOf(false) }
    BackHandler(enabled = !backPressHandled) {
        backPressHandled = true
        scope.launch {
            awaitFrame()
            onBackPressedDispatcher?.onBackPressed()
            backPressHandled = false
            completion(
                Result.failure(
                    ThreeDSException.CancellationException(
                        displayableMessage = context.getString(
                            R.string.error_3ds_cancelled_error
                        )
                    )
                )
            )
        }
    }

    // Handle result and reset state
    LaunchedEffect(uiState) {
        handleUIState(uiState, viewModel, completion)
    }

    // Apply the SdkTheme for consistent styling
    SdkTheme {
        // Display the 3DS WebView within the bottom sheet
        val htmlString = HtmlWidgetBuilder.createHtml(
            config = WidgetConfig.ThreeDSConfig(
                token = token
            )
        )
        SdkWebView(
            webUrl = "https://paydock.com",
            data = htmlString,
            jsBridge = ThreeDSJSBridge {
                viewModel.updateThreeDSEvent(it)
            }
        ) { status, message ->
            // Invoke the onWebViewError callback with the WebView ThreeDSException exception
            completion(Result.failure(ThreeDSException.WebViewException(status, message)))
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
    uiState: ThreeDSUIState,
    viewModel: ThreeDSViewModel,
    completion: (Result<ThreeDSResult>) -> Unit,
) {
    when (uiState) {
        ThreeDSUIState.Idle,
        ThreeDSUIState.Loading, -> Unit

        is ThreeDSUIState.Error -> {
            completion(Result.failure(uiState.exception))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }

        is ThreeDSUIState.Success -> {
            completion(Result.success(uiState.result))
            // This ensures that we clear the state so it's not reused
            viewModel.resetResultState()
        }
    }
}
