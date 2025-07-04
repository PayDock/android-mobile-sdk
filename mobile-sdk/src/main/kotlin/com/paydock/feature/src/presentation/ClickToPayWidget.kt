package com.paydock.feature.src.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.ClickToPayException
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.components.web.config.WidgetConfig
import com.paydock.designsystems.components.web.utils.HtmlWidgetBuilder
import com.paydock.designsystems.core.WidgetDefaults
import com.paydock.feature.src.domain.model.integration.ClickToPayWidgetConfig
import com.paydock.feature.src.presentation.utils.ClickToPayJSBridge
import com.paydock.feature.src.presentation.viewmodels.ClickToPayViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function to render the SRC (Secure Remote Commerce) Click to Pay widget.
 *
 * @param modifier The modifier for the composable.
 * @param config Configuration data required to initialise the Click to Pay widget.
 * @param appearance Custom appearance options for the Click to Pay widget.
 * @param completion Callback function invoked upon completion of the Click to Pay flow.
 */
@Composable
fun ClickToPayWidget(
    modifier: Modifier = Modifier,
    config: ClickToPayWidgetConfig,
    appearance: ClickToPayWidgetAppearance = ClickToPayAppearanceDefaults.appearance(),
    completion: (Result<String>) -> Unit
) {
    // Obtain instances of view models
    val viewModel: ClickToPayViewModel = koinViewModel()

    // Collect states for Click to PayC view models
    val uiState by viewModel.stateFlow.collectAsState()

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

    // Render Click to Pay widget
    Box(contentAlignment = Alignment.Center) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(WidgetDefaults.Spacing, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            // Generate HTML string for Click to Pay checkout
            val htmlString = HtmlWidgetBuilder.createHtml(
                config = WidgetConfig.ClickToPayConfig(
                    accessToken = config.accessToken,
                    serviceId = config.serviceId,
                    meta = config.meta
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
                loaderAppearance = appearance.loader
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

/**
 * Represents the appearance configuration for the Click to Pay widget.
 *
 * @property loader The [LoaderAppearance] configuration for the loader shown within the widget.
 */
@Immutable
class ClickToPayWidgetAppearance(val loader: LoaderAppearance) {

    /**
     * Creates a copy of the [ClickToPayWidgetAppearance] with optionally updated properties.
     *
     * @param loader The [LoaderAppearance] to use for the copy. Defaults to the current loader.
     * @return A new [ClickToPayWidgetAppearance] instance with the specified properties.
     */
    fun copy(loader: LoaderAppearance = this.loader): ClickToPayWidgetAppearance =
        ClickToPayWidgetAppearance(
            loader = loader.copy()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClickToPayWidgetAppearance

        return loader == other.loader
    }

    override fun hashCode(): Int {
        return loader.hashCode()
    }
}

/**
 * Default appearance settings for the Click to Pay widget.
 *
 * This object provides a default [ClickToPayWidgetAppearance] which can be used
 * when a specific appearance is not provided for the [ClickToPayWidget].
 */
object ClickToPayAppearanceDefaults {

    /**
     * Creates a default appearance configuration for the Click to Pay widget.
     *
     * @return The default [ClickToPayWidgetAppearance].
     */
    @Composable
    fun appearance(): ClickToPayWidgetAppearance = ClickToPayWidgetAppearance(
        loader = LoaderAppearanceDefaults.appearance()
    )
}