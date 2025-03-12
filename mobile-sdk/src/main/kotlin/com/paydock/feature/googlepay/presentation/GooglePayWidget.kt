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
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.googlepay.presentation.state.GooglePayUIState
import com.paydock.feature.googlepay.presentation.viewmodels.GooglePayViewModel
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A Composable for handling Google Pay payments and related interactions.
 *
 * This Composable integrates with Google Pay, providing a button to initiate payment requests,
 * handles user interactions, and manages the payment lifecycle through state management.
 *
 * @param modifier Modifier for customizing the appearance and behavior of the Composable.
 * @param token A callback to asynchronously retrieve the wallet token.
 * @param isReadyToPayRequest The JSON object defining the request to check if the user is ready to pay.
 * @param paymentRequest The JSON object containing the payment request details.
 * @param loadingDelegate An optional delegate to manage loading indicators externally.
 * @param completion A callback to handle the result of the Google Pay operation, either success or failure.
 */
@Composable
fun GooglePayWidget(
    modifier: Modifier = Modifier,
    token: (onTokenReceived: (String) -> Unit) -> Unit,
    isReadyToPayRequest: JSONObject,
    paymentRequest: JSONObject,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<ChargeResponse>) -> Unit
) {
    // Retrieve the GooglePayViewModel using Koin
    val viewModel: GooglePayViewModel =
        koinViewModel(parameters = { parametersOf(isReadyToPayRequest) })

    val scope = rememberCoroutineScope()

    // Collect the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val googlePayAvailable by viewModel.googlePayAvailable.collectAsState()
    val allowedPaymentMethods = viewModel.extractAllowedPaymentMethods(paymentRequest)

    val resolvablePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        handleResolvableResult(result, viewModel)
    }

    // Set up a launcher for handling Google Pay resolution
    val paymentDataTaskResult = rememberLauncherForActivityResult(
        contract = TaskResultContracts.GetPaymentDataResult()
    ) { result ->
        handleGooglePayResult(result, viewModel, resolvablePaymentForResult)
    }

    LaunchedEffect(uiState) {
        handleUIState(
            uiState,
            viewModel,
            loadingDelegate,
            completion
        )
    }

    SdkTheme {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = modifier.background(Theme.colors.background),
                verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            type = ButtonType.Pay,
                            onClick = {
                                // Use the callback to obtain the token asynchronously
                                token { obtainedToken ->
                                    scope.launch {
                                        viewModel.setWalletToken(obtainedToken)
                                        val task = viewModel.getLoadPaymentDataTask(paymentRequest)
                                        task.addOnCompleteListener(paymentDataTaskResult::launch)
                                    }
                                }
                            },
                            radius = Theme.dimensions.buttonCornerRadius,
                            allowedPaymentMethods = allowedPaymentMethods
                        )
                    }
                }
            }
            if (loadingDelegate == null && uiState is GooglePayUIState.Loading) {
                SdkLoader()
            }
        }
    }
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
 * This function processes the current UI state of Google Pay, invoking loading delegates, and
 * completing the payment transaction with success or error results as appropriate.
 *
 * @param uiState The current UI state of the Google Pay operation.
 * @param viewModel The ViewModel managing Google Pay state and operations.
 * @param loadingDelegate An optional delegate to manage loading indicators externally.
 * @param completion A callback to complete the Google Pay transaction with success or error.
 */
private fun handleUIState(
    uiState: GooglePayUIState,
    viewModel: GooglePayViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    completion: (Result<ChargeResponse>) -> Unit,
) {
    when (uiState) {
        is GooglePayUIState.Idle -> Unit
        is GooglePayUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
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