package com.paydock.feature.flypay.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.sp
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.FlyPayException
import com.paydock.core.presentation.ui.extensions.alpha40
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.loader.SdkButtonLoader
import com.paydock.designsystems.components.sheet.SdkBottomSheet
import com.paydock.designsystems.components.web.SdkWebView
import com.paydock.designsystems.theme.FlyPayBlue
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.designsystems.theme.typography.ArialFontFamily
import com.paydock.feature.flypay.presentation.viewmodels.FlyPayViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Composable function for rendering the FlyPay widget.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param token A callback to obtain the wallet token asynchronously.
 * @param completion A callback to handle the result of the FlyPay operation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun FlyPayWidget(
    modifier: Modifier = Modifier,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    completion: (Result<String>) -> Unit
) {
    val context = LocalContext.current
    // Obtain instances of view models
    val viewModel: FlyPayViewModel = koinViewModel()

    // Collect states for FlyPay and Wallet view models
    val uiState by viewModel.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )

    // Handle back button press when the bottom sheet is open
    BackHandler(bottomSheetState.isVisible) {
        scope.launch {
            bottomSheetState.hide()
            completion(
                Result.failure(
                    FlyPayException.CancellationException(
                        displayableMessage = context.getString(
                            R.string.error_flypay_cancelled
                        )
                    )
                )
            )
        }
    }

    // Handle wallet response result and reset state
    LaunchedEffect(uiState) {
        // Handle wallet error flow and display
        uiState.error?.let {
            // Send error state to the completion callback
            completion(Result.failure(it))
            viewModel.resetResultState()
        }

        // Handle wallet callback response result to show the bottom sheet
        uiState.callbackData?.let {
            openBottomSheet = true
        }
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    // Composable content rendering
    SdkTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Button to initiate FlyPay transaction
                Button(
                    onClick = {
                        // Use the callback to obtain the token asynchronously
                        token { obtainedToken ->
                            scope.launch {
                                viewModel.setWalletToken(obtainedToken)
                                viewModel.getWalletCallback(
                                    walletToken = obtainedToken
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("flypayButton")
                        .fillMaxWidth()
                        .height(Theme.dimensions.buttonHeight),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlyPayBlue,
                        disabledContainerColor = FlyPayBlue.alpha40
                    ),
                    shape = Theme.shapes.small
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            Theme.dimensions.buttonSpacing,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.fillMaxHeight(),
                            text = stringResource(R.string.button_pay_with),
                            style = Theme.typography.button.copy(
                                fontFamily = ArialFontFamily,
                                fontSize = 22.sp,
                                lineHeight = 48.sp,
                                color = Color.White,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_flypay_button),
                            contentDescription = stringResource(id = R.string.content_desc_flypay_button_icon)
                        )
                        // Show a progress indicator if wallet is loading
                        if (uiState.isLoading) {
                            SdkButtonLoader()
                        }
                    }
                }
            }
        }
        // Sheet content
        if (openBottomSheet) {
            uiState.callbackData?.callbackId?.let { flyPayOrderId ->
                // Create and load the FlyPay URL
                val flyPayUrl: String by rememberSaveable {
                    mutableStateOf(
                        viewModel.createFlyPayUrl(flyPayOrderId)
                    )
                }
                // Display WebView in a BottomSheet
                SdkBottomSheet(
                    bottomSheetState = bottomSheetState,
                    onDismissRequest = {
                        openBottomSheet = false
                        completion(
                            Result.failure(
                                FlyPayException.CancellationException(
                                    displayableMessage = context.getString(
                                        R.string.error_flypay_cancelled
                                    )
                                )
                            )
                        )
                    }
                ) {
                    // WebView for displaying the FlyPay URL
                    SdkWebView<Unit>(
                        webUrl = flyPayUrl,
                        showLoader = false,
                        // This is required for FlyPay Web to work
                        onShouldOverrideUrlLoading = { request ->
                            val requestUrl = request?.url.toString()
                            // Handle redirection URLs
                            return@SdkWebView if (requestUrl == MobileSDKConstants.FlyPayConfig.FLY_PAY_REDIRECT_URL) {
                                // Dismiss the bottom sheet on successful URL redirection
                                openBottomSheet = false
                                completion(Result.success(flyPayOrderId))
                                viewModel.resetResultState()
                                true
                            } else {
                                false
                            }
                        }
                    ) { status, message ->
                        // Hide the bottom sheet on WebView error and handle the sheet dismissal
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                openBottomSheet = false
                            }
                        }
                        // Invoke the onWebViewError callback with the FlyPay exception
                        Result.failure<FlyPayException.WebViewException>(
                            FlyPayException.WebViewException(
                                status,
                                message
                            )
                        )
                    }
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewFlyPayWidget() {
    SdkTheme {
        FlyPayWidget(token = {}, completion = {})
    }
}