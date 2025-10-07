package com.paydock.feature.googlepay.presentation

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.takeOrElse
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.GooglePayException
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.LoaderAppearanceDefaults
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.googlepay.domain.model.GooglePayWidgetConfig
import com.paydock.feature.googlepay.presentation.state.GooglePayUIState
import com.paydock.feature.googlepay.presentation.viewmodels.GooglePayViewModel
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A Composable for handling Google Pay payments and related interactions.
 *
 * This Composable integrates with Google Pay, providing a button to initiate payment requests,
 * handles user interactions, and manages the payment lifecycle through state management.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param enabled A boolean indicating whether the Google Pay button is enabled. Defaults to `true`.
 * @param config The configuration for the Google Pay widget.
 * @param appearance The appearance configuration for the Google Pay widget, including button theme,
 * type, corner radius, and loader appearance. Defaults to [GooglePayAppearanceDefaults.appearance].
 * @param tokenRequest A callback to asynchronously retrieve the wallet token. This callback should call
 * the provided `onTokenReceived` lambda with the retrieved token.
 * @param loadingDelegate An optional delegate to manage loading indicators externally.
 * If provided, the widget will not display its internal loader.
 * @param completion A callback to handle the result of the Google Pay operation, either success or failure.
 */
@Composable
fun GooglePayWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: GooglePayWidgetConfig,
    appearance: GooglePayWidgetAppearance = GooglePayAppearanceDefaults.appearance(),
    tokenRequest: (tokenResult: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<ChargeResponse>) -> Unit
) {
    // Retrieve the GooglePayViewModel using Koin
    val viewModel: GooglePayViewModel = koinViewModel(parameters = { parametersOf(config) })

    // Collect the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val googlePayAvailable by viewModel.googlePayAvailable.collectAsState()
    val allowedPaymentMethods = viewModel.extractAllowedPaymentMethods()

    val resolvablePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        handleResolvableResult(result, viewModel)
    }

    // Set up a launcher for handling Google Pay resolution
    val paymentDataLauncher = rememberLauncherForActivityResult(
        contract = TaskResultContracts.GetPaymentDataResult()
    ) { result ->
        handleGooglePayResult(result, viewModel, resolvablePaymentForResult)
    }

    LaunchedEffect(uiState::class) {
        handleUIState(
            uiState,
            viewModel,
            paymentDataLauncher,
            loadingDelegate,
            completion
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!allowedPaymentMethods.isNullOrBlank()) {
            // Show the Google Pay button when it's available
            AnimatedVisibility(
                visible = googlePayAvailable,
                enter = fadeIn(animationSpec = tween(MobileSDKConstants.General.DEFAULT_ANIMATION_DURATION)),
                exit = fadeOut(animationSpec = tween(MobileSDKConstants.General.DEFAULT_ANIMATION_DURATION))
            ) {
                // Google Pay Button - https://developers.google.com/pay/api/android/reference/request-objects#ButtonOptions
                PayButton(
                    modifier = Modifier
                        .testTag("payButton")
                        .fillMaxWidth(),
                    theme = if (isSystemInDarkTheme()) ButtonTheme.Dark else ButtonTheme.Light,
                    type = appearance.type,
                    onClick = {
                        viewModel.startGooglePayPaymentFlow(tokenRequest)
                    }, radius = appearance.cornerRadius,
                    allowedPaymentMethods = allowedPaymentMethods,
                    onError = {
                        completion(
                            Result.failure(
                                GooglePayException.InitialisationException(
                                    it.message
                                        ?: MobileSDKConstants.GooglePayConfig.Errors.INITIALISATION_ERROR
                                )
                            )
                        )
                    },
                    enabled = uiState !is GooglePayUIState.Loading && enabled,
                )
            }
        }
        if (loadingDelegate == null && uiState is GooglePayUIState.Loading) {
            SdkLoader(appearance = appearance.loader)
        }
    }
}

/**
 * Represents the appearance configuration for the Google Pay widget.
 *
 * @property cornerRadius The corner radius of the Google Pay button.
 * @property type The type of the Google Pay button.
 * @property loader The appearance configuration for the loader displayed during processing.
 */
@Immutable
class GooglePayWidgetAppearance(
    val cornerRadius: Dp,
    val type: ButtonType,
    val loader: LoaderAppearance
) {
    /**
     * Creates a copy of this [GooglePayWidgetAppearance] with optionally updated values.
     *
     * @param cornerRadius The corner radius for the copied appearance, defaults to the current value.
     * @param type The button type for the copied appearance, defaults to the current value.
     * @param loader The loader appearance for the copied appearance, defaults to the current value.
     * @return A new [GooglePayWidgetAppearance] with the specified or default values.
     */
    fun copy(
        cornerRadius: Dp = this.cornerRadius,
        type: ButtonType = this.type,
        loader: LoaderAppearance = this.loader
    ): GooglePayWidgetAppearance = GooglePayWidgetAppearance(
        cornerRadius = cornerRadius.takeOrElse { this.cornerRadius }, type = type, loader = loader.copy()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GooglePayWidgetAppearance

        if (cornerRadius != other.cornerRadius) return false
        if (type != other.type) return false
        if (loader != other.loader) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cornerRadius.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + loader.hashCode()
        return result
    }
}

/**
 * Provides default values for the appearance configuration of the Google Pay widget.
 * These defaults can be used to quickly set up the visual style of the Google Pay button
 * and associated loader, aligning with Material Design principles and considering
 * the system's dark theme setting.
 */
object GooglePayAppearanceDefaults {

    /**
     * Provides a default appearance for the Google Pay widget.
     *
     * This Composable function creates a [GooglePayWidgetAppearance] with default values:
     * - Uses [ButtonAppearanceDefaults.ButtonCornerRadius] for the corner radius.
     * - Sets the button type to [ButtonType.Pay].
     * - Uses [LoaderAppearanceDefaults.appearance] for the loader appearance.
     *
     * @return The default [GooglePayWidgetAppearance].
     */
    @Composable
    fun appearance(): GooglePayWidgetAppearance = GooglePayWidgetAppearance(
        cornerRadius = ButtonAppearanceDefaults.ButtonCornerRadius,
        type = ButtonType.Pay,
        loader = LoaderAppearanceDefaults.appearance()
    )
}

/**
 * Handles the result of a resolvable Google Pay payment issue.
 *
 * This function processes the result of a payment attempt that required user resolution, such as updating payment details.
 *
 * @param result The result of the activity launched to resolve the payment issue.
 * @param viewModel The ViewModel managing Google Pay state and operations.
 */
private fun handleResolvableResult(
    result: ActivityResult,
    viewModel: GooglePayViewModel
) {
    when (result.resultCode) {
        RESULT_OK -> {
            result.data?.let { intent ->
                PaymentData.getFromIntent(intent)?.let { paymentData ->
                    viewModel.processGooglePayPaymentResult(paymentData)
                }
            }
        }

        WalletConstants.RESULT_ERROR -> {
            val status = AutoResolveHelper.getStatusFromIntent(result.data)
            viewModel.handleWalletResultErrors(status)
        }

        RESULT_CANCELED -> {
            // The user cancelled the payment attempt
            viewModel.handleCancellationResult()
        }
    }
}

/**
 * Processes the result of a Google Pay payment task.
 *
 * This function handles the success or failure of the payment task, resolving any issues if possible,
 * and delegates processing to the ViewModel.
 *
 * @param taskResult The result of the Google Pay payment task.
 * @param viewModel The ViewModel managing Google Pay state and operations.
 * @param resolvePaymentForResult The launcher to handle resolvable payment issues.
 */
private fun handleGooglePayResult(
    taskResult: ApiTaskResult<PaymentData>,
    viewModel: GooglePayViewModel,
    resolvePaymentForResult: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
) {
    when (taskResult.status.statusCode) {
        CommonStatusCodes.SUCCESS -> {
            taskResult.result?.let { paymentData ->
                viewModel.processGooglePayPaymentResult(paymentData)
            }
        }

        else -> {
            if (taskResult.status.hasResolution()) {
                // Resolve Google Pay payment issues using a launcher
                taskResult.status.startResolutionForResult(resolvePaymentForResult)
            } else {
                viewModel.handleGooglePayResultErrors(taskResult.status.statusCode)
            }
        }
    }
}

/**
 * Handles the UI state for Google Pay.
 *
 * This function processes the current UI state of Google Pay, invoking loading delegates, launching Google Pay tasks, and
 * completing the payment transaction with success or error results as appropriate.
 *
 * @param uiState The current UI state of the Google Pay operation.
 * @param viewModel The ViewModel managing Google Pay state and operations.
 * @param paymentDataLauncher A launcher for the Google Pay payment data task.
 * @param loadingDelegate An optional delegate to manage loading indicators externally.
 * @param completion A callback to complete the Google Pay transaction with success or error.
 */
private fun handleUIState(
    uiState: GooglePayUIState,
    viewModel: GooglePayViewModel,
    paymentDataLauncher: ManagedActivityResultLauncher<Task<PaymentData>, ApiTaskResult<PaymentData>>, // Updated parameter
    loadingDelegate: WidgetLoadingDelegate?,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    when (uiState) {
        is GooglePayUIState.Idle -> Unit
        is GooglePayUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
        }
        is GooglePayUIState.LaunchGooglePayTask -> {
            loadingDelegate?.widgetLoadingDidFinish()
            uiState.paymentDataTask.addOnCompleteListener(paymentDataLauncher::launch)
        }
        // Handle success state, notify the loading delegate, and complete the transaction with success
        is GooglePayUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.success(uiState.chargeData))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
        }
        // Handle error state, notify the loading delegate, and complete the transaction with failure
        is GooglePayUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            // Reset the state to ensure it’s not reused
            viewModel.resetResultState()
        }

    }
}