package com.paydock.feature.threeDS.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.paydock.feature.threeDS.domain.model.ThreeDSResult
import com.paydock.feature.threeDS.presentation.utils.ThreeDSJSBridge
import com.paydock.feature.threeDS.presentation.viewmodels.ThreeDSViewModel
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function to initiate 3D Secure (3DS) processing.
 *
 * @param token The 3DS token used for 3DS widget initialization.
 * @param completion Callback function to handle the result of 3DS processing.
 */
@Composable
fun ThreeDSWidget(
    token: String,
    completion: (Result<ThreeDSResult>) -> Unit
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
        // Handle error flow and display
        uiState.error?.let {
            // Send error state to the completion callback
            completion(Result.failure(it))
            viewModel.resetResultState()
        }

        // Handle OTT result and reset state
        uiState.result?.let { result ->
            if (uiState.status == "success" || uiState.status == "authenticated") {
                // Send success state to the completion callback
                completion(Result.success(result))
                viewModel.resetResultState()
            }
        }
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
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
