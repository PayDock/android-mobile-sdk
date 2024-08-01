package com.paydock.feature.afterpay.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.afterpay.android.Afterpay
import com.afterpay.android.view.AfterpayPaymentButton
import com.paydock.R
import com.paydock.core.domain.error.exceptions.AfterpayException
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.core.utils.jwt.JwtHelper
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.address.domain.model.BillingAddress
import com.paydock.feature.afterpay.presentation.mapper.mapFromBillingAddress
import com.paydock.feature.afterpay.presentation.mapper.mapFromShippingOption
import com.paydock.feature.afterpay.presentation.mapper.mapToAfterpayV2Options
import com.paydock.feature.afterpay.presentation.model.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOption
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.state.AfterpayViewState
import com.paydock.feature.afterpay.presentation.utils.CheckoutHandler
import com.paydock.feature.afterpay.presentation.viewmodels.AfterpayViewModel
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.feature.wallet.domain.model.WalletCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * A Composable function representing the Afterpay payment widget.
 *
 * This widget handles the integration with the Afterpay SDK, including initiating checkout,
 * managing shipping address and options, handling payment callbacks, and displaying the payment button.
 *
 * @param modifier The modifier for styling and layout.
 * @param config The configuration for the Afterpay SDK.
 * @param token The callback to obtain the authentication token asynchronously.
 * @param selectAddress The callback to handle selection of shipping address.
 * @param selectShippingOption The callback to handle selection of shipping option.
 * @param completion The callback to handle the result of the payment operation.
 */
@Composable
fun AfterpayWidget(
    modifier: Modifier = Modifier,
    config: AfterpaySDKConfig,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    selectAddress: (address: BillingAddress, provideShippingOptions: (List<AfterpayShippingOption>) -> Unit) -> Unit = { _, _ -> },
    selectShippingOption: (
        shippingOption: AfterpayShippingOption,
        provideShippingOptionUpdateResult: (AfterpayShippingOptionUpdate?) -> Unit
    ) -> Unit = { _, _ -> },
    completion: (Result<ChargeResponse>) -> Unit
) {
    val viewModel: AfterpayViewModel = koinViewModel()

    // Get the current context and coroutine scope
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Collect the UI state from the ViewModel
    val uiState by viewModel.stateFlow.collectAsState()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        // Handle the wallet response
        handleWalletResponse(context, uiState, result, viewModel, completion)
    }

    LaunchedEffect(Unit) {
        // Configure Afterpay SDK and set up checkout handler
        viewModel.configureAfterpaySdk(config.config)
        val checkoutHandler = setupCheckoutHandler(viewModel, selectAddress, selectShippingOption)
        Afterpay.setCheckoutV2Handler(checkoutHandler)

        // Observe commands from ViewModel and react accordingly
        scope.launch {
            viewModel.commands().collectLatest { command ->
                when (command) {
                    is AfterpayViewModel.Command.ProvideCheckoutTokenResult -> checkoutHandler.provideTokenResult(
                        command.tokenResult
                    )

                    is AfterpayViewModel.Command.ProvideShippingOptionUpdateResult -> checkoutHandler.provideShippingOptionUpdateResult(
                        command.shippingOptionUpdateResult,
                    )

                    is AfterpayViewModel.Command.ProvideShippingOptionsResult -> checkoutHandler.provideShippingOptionsResult(
                        command.shippingOptionsResult
                    )
                }
            }
        }
    }

    // Handle wallet response result and reset state
    LaunchedEffect(uiState) {
        uiState.chargeData?.let { response ->
            when (response.resource.data?.status) {
                "complete" -> completion(Result.success(response))
                "failed" -> uiState.error?.let { completion(Result.failure(it)) }
            }
            viewModel.resetResultState()
        }
    }

    // Reset form state when the widget is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetResultState()
        }
    }

    // Display the Afterpay payment button
    SdkTheme {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            AfterpayWidgetContent(
                scope = scope,
                config = config,
                tokenProvider = token,
                resolvePaymentForResult = resolvePaymentForResult,
                viewModel = viewModel
            )
            if (uiState.isLoading) {
                // This is to ensure we show a loading state when finalising charge
                SdkLoader()
            }
        }
    }
}

/**
 * Composable function representing the content of the Afterpay payment widget.
 *
 * This function displays the payment button and handles the token acquisition process.
 *
 * @param scope The coroutine scope.
 * @param config The configuration for the Afterpay SDK.
 * @param tokenProvider The callback to obtain the authentication token asynchronously.
 * @param resolvePaymentForResult The ActivityResultLauncher for resolving payment.
 * @param viewModel The AfterpayViewModel instance.
 */
@Composable
private fun AfterpayWidgetContent(
    scope: CoroutineScope,
    config: AfterpaySDKConfig,
    tokenProvider: (onTokenReceived: (String) -> Unit) -> Unit,
    resolvePaymentForResult: ActivityResultLauncher<Intent>,
    viewModel: AfterpayViewModel,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.stateFlow.collectAsState().value.validLocale) {
            // Display Afterpay payment button
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Theme.dimensions.buttonHeight),
                factory = { context ->
                    AfterpayPaymentButton(context).apply {
                        this.buttonText = config.buttonTheme.buttonText
                        this.colorScheme = config.buttonTheme.colorScheme
                        setOnClickListener {
                            // Obtain token asynchronously
                            scope.launch {
                                tokenProvider { obtainedToken ->
                                    // Handle obtained token
                                    handleTokenObtained(
                                        context,
                                        obtainedToken,
                                        viewModel,
                                        config.options,
                                        resolvePaymentForResult
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Handles the obtained token and initiates the payment process.
 *
 * @param context The context.
 * @param token The obtained token.
 * @param viewModel The AfterpayViewModel instance.
 * @param checkoutOptions The checkout options.
 * @param resolvePaymentForResult The ActivityResultLauncher for resolving payment.
 */
private fun handleTokenObtained(
    context: Context,
    token: String,
    viewModel: AfterpayViewModel,
    checkoutOptions: AfterpaySDKConfig.CheckoutOptions?,
    resolvePaymentForResult: ActivityResultLauncher<Intent>
) {
    viewModel.setWalletToken(token)
    val intent = checkoutOptions?.let {
        Afterpay.createCheckoutV2Intent(
            context,
            it.mapToAfterpayV2Options()
        )
    }
        ?: Afterpay.createCheckoutV2Intent(context)
    resolvePaymentForResult.launch(intent)
}

/**
 * Handles the wallet response.
 *
 * @param context The context.
 * @param uiState The UI state.
 * @param result The activity result.
 * @param viewModel The AfterpayViewModel instance.
 * @param completion The callback to handle the result of the payment operation.
 */
private fun handleWalletResponse(
    context: Context,
    uiState: AfterpayViewState,
    result: ActivityResult,
    viewModel: AfterpayViewModel,
    completion: (Result<ChargeResponse>) -> Unit
) {
    val walletToken = requireNotNull(uiState.token) {
        context.getString(R.string.error_wallet_cancelled)
    }

    try {
        when (result.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                handleWalletSuccess(context, uiState.callbackData, walletToken, viewModel)
            }

            AppCompatActivity.RESULT_CANCELED -> {
                handleWalletCancellation(context, result, walletToken, viewModel)
            }
        }
    } catch (e: IllegalStateException) {
        e.message?.let { completion(Result.failure(AfterpayException.InvalidResultException(it))) }
    }
}

/**
 * Handles the successful wallet response.
 *
 * @param context The context.
 * @param callbackData The wallet callback data.
 * @param walletToken The wallet token.
 * @param viewModel The AfterpayViewModel instance.
 */
private fun handleWalletSuccess(
    context: Context,
    callbackData: WalletCallback?,
    walletToken: String,
    viewModel: AfterpayViewModel
) {
    val checkoutToken = requireNotNull(callbackData?.refToken) {
        context.getString(R.string.error_afterpay_sdk_internal_token)
    }

    if (walletToken.isNotBlank() && checkoutToken.isNotBlank()) {
        handleAfterpaySuccessResult(walletToken, checkoutToken, viewModel)
    }
}

/**
 * Handles the canceled wallet response.
 *
 * @param context The context.
 * @param result The activity result.
 * @param walletToken The wallet token.
 * @param viewModel The AfterpayViewModel instance.
 */
private fun handleWalletCancellation(
    context: Context,
    result: ActivityResult,
    walletToken: String,
    viewModel: AfterpayViewModel
) {
    val intent = requireNotNull(result.data) {
        context.getString(R.string.error_afterpay_sdk_internal_data)
    }

    val status = checkNotNull(Afterpay.parseCheckoutCancellationResponse(intent)) {
        context.getString(R.string.error_afterpay_sdk_internal_status)
    }

    viewModel.updateCancellationState(status)

    if (walletToken.isNotBlank()) {
        val chargeId = JwtHelper.getChargeIdToken(walletToken)
        chargeId?.let { handleAfterpayFailureResult(walletToken, it, viewModel) }
    }
}

/**
 * Sets up the checkout handler.
 *
 * @param viewModel The AfterpayViewModel instance.
 * @param selectAddress The callback to handle selection of shipping address.
 * @param selectShippingOption The callback to handle selection of shipping option.
 * @return The configured CheckoutHandler.
 */
private fun setupCheckoutHandler(
    viewModel: AfterpayViewModel,
    selectAddress: (address: BillingAddress, provideShippingOptions: (List<AfterpayShippingOption>) -> Unit) -> Unit = { _, _ -> },
    selectShippingOption: (
        shippingOption: AfterpayShippingOption,
        provideShippingOptionUpdateResult: (AfterpayShippingOptionUpdate?) -> Unit
    ) -> Unit = { _, _ -> },
): CheckoutHandler {
    return CheckoutHandler(
        onDidCommenceCheckout = { viewModel.loadCheckoutToken() },
        onShippingAddressDidChange = {
            selectAddress(it.mapFromBillingAddress()) { shippingOptions ->
                viewModel.provideShippingOptions(shippingOptions)
            }
        },
        onShippingOptionDidChange = {
            selectShippingOption(it.mapFromShippingOption()) { shippingOptionUpdate ->
                viewModel.provideShippingOptionUpdate(shippingOptionUpdate)
            }
        },
    )
}

/**
 * Handle a successful Afterpay payment
 *
 * @param walletToken The wallet token provided for initiating the wallet charge
 * @param afterPayToken Afterpay checkout token
 * @param viewModel [AfterpayViewModel] responsible for capturing the wallet charge
 */
private fun handleAfterpaySuccessResult(
    walletToken: String,
    afterPayToken: String,
    viewModel: AfterpayViewModel
) {
    viewModel.captureWalletTransaction(
        walletToken = walletToken,
        afterPayToken = afterPayToken
    )
}

/**
 * Handle a failure Afterpay payment
 *
 * @param walletToken The wallet token provided for initiating the wallet charge
 * @param chargeId The chargeId required for the transaction.
 * @param viewModel [AfterpayViewModel] responsible for capturing the wallet charge
 */
private fun handleAfterpayFailureResult(
    walletToken: String,
    chargeId: String,
    viewModel: AfterpayViewModel
) {
    viewModel.declineWalletTransaction(
        walletToken = walletToken,
        chargeId = chargeId
    )
}

@LightDarkPreview
@Composable
private fun PreviewAfterpayWidget() {
    SdkTheme {
        AndroidView(factory = { context ->
            AfterpayPaymentButton(context).apply {
                this.buttonText = buttonText
                this.colorScheme = colorScheme
                setOnClickListener {}
            }
        })
    }
}