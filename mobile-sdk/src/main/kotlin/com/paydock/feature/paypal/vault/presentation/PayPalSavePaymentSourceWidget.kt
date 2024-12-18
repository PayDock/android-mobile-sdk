package com.paydock.feature.paypal.vault.presentation

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.AppButtonType
import com.paydock.designsystems.components.button.SdkButton
import com.paydock.designsystems.theme.PayPalVault
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultResult
import com.paydock.feature.paypal.vault.presentation.state.PayPalVaultUIState
import com.paydock.feature.paypal.vault.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.vault.presentation.utils.getCancellationStatusExtra
import com.paydock.feature.paypal.vault.presentation.utils.putClientIdExtra
import com.paydock.feature.paypal.vault.presentation.utils.putSetupTokenExtra
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalVaultViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A Composable widget that handles the PayPal payment source linking process.
 *
 * This widget interacts with the [PayPalVaultViewModel] to trigger the PayPal linking flow. It displays
 * a button to initiate the PayPal linking process and observes the state of the flow using a `StateFlow`.
 * When a payment token is successfully created or an error occurs, the [completion] callback is invoked
 * with the corresponding result.
 *
 * @param modifier The [Modifier] to be applied to the widget.
 * @param enabled Controls the enabled state of this Widget. When false,
 * this component will not respond to user input, and it will appear visually disabled.
 * @param config The configuration for PayPal vault, including the access token and gateway ID.
 * @param loadingDelegate The delegate passed to overwrite control of showing loaders.
 * @param completion The callback invoked when the PayPal linking process completes, either with a success
 *                   containing a [PayPalVaultResult] or a failure containing a [PayPalVaultException].
 */
@Composable
fun PayPalSavePaymentSourceWidget(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: PayPalVaultConfig,
    loadingDelegate: WidgetLoadingDelegate? = null,
    completion: (Result<PayPalVaultResult>) -> Unit,
) {
    // Obtain the current context
    val context = LocalContext.current

    // Get the PayPalVaultViewModel from Koin, passing the config as a parameter
    val viewModel: PayPalVaultViewModel = koinViewModel(parameters = { parametersOf(config) })

    // Collect the current state from the ViewModel's state flow
    val uiState by viewModel.stateFlow.collectAsState()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handlePayPalVaultResult(context, result, viewModel, completion)
    }

    LaunchedEffect(uiState) {
        handleUIState(
            context,
            uiState,
            viewModel,
            loadingDelegate,
            resolvePaymentForResult,
            completion
        )
    }

    // Apply the SDK's theme to the widget
    SdkTheme {
        Box(modifier = modifier) {
            // Display a button to link the PayPal account
            SdkButton(
                modifier = Modifier
                    .testTag("linkPayPalAccount"),
                buttonColor = PayPalVault,
                buttonIcon = config.icon,
                text = config.actionText
                    ?: stringResource(id = R.string.button_link_paypal_account),
                type = AppButtonType.Outlined,
                enabled = uiState !is PayPalVaultUIState.Loading && enabled,
                isLoading = loadingDelegate == null && uiState is PayPalVaultUIState.Loading
            ) {
                viewModel.createPayPalSetupToken()
            }
        }
    }
}

/**
 * Processes the result of a PayPal Vault activity and performs the appropriate actions based on the result code.
 *
 * @param context The current application context.
 * @param result The `ActivityResult` received from the PayPal Vault activity.
 * @param viewModel The `PayPalVaultViewModel` managing the PayPal Vault flow.
 * @param completion A callback to handle the final result of the PayPal Vault operation, either success or failure.
 */
private fun handlePayPalVaultResult(
    context: Context,
    result: ActivityResult,
    viewModel: PayPalVaultViewModel,
    completion: (Result<PayPalVaultResult>) -> Unit,
) {
    result.data?.let { data ->
        when (result.resultCode) {
            // Handles the success case, invoking the ViewModel to create a payment source token.
            AppCompatActivity.RESULT_OK -> {
                viewModel.createPayPalPaymentSourceToken()
            }

            // Handles the cancellation case, determining the reason for cancellation and acting accordingly.
            AppCompatActivity.RESULT_CANCELED -> {
                when (data.getCancellationStatusExtra()) {
                    // If the cancellation was due to invalid parameters (e.g., clientId or setup token).
                    CancellationStatus.INVALID_PARAMS -> {
                        completion(
                            Result.failure(
                                PayPalVaultException.CancellationException(
                                    displayableMessage = context.getString(
                                        R.string.error_paypal_vault_invalid
                                    )
                                )
                            )
                        )
                    }
                    // If the cancellation was initiated by the user.
                    CancellationStatus.USER_INITIATED -> {
                        completion(
                            Result.failure(
                                PayPalVaultException.CancellationException(
                                    displayableMessage = context.getString(
                                        R.string.error_paypal_vault_canceled
                                    )
                                )
                            )
                        )
                    }
                    // For other cancellation reasons, handle as a PayPal SDK error.
                    else -> {
                        val status = data.getStatusExtra()
                        val message = data.getMessageExtra(MobileSDKConstants.Errors.PAY_PAL_VAULT_ERROR)
                        completion(
                            Result.failure(
                                PayPalVaultException.PayPalSDKException(
                                    status,
                                    message
                                )
                            )
                        )
                    }
                }
            }

            // Handles any other unrecognized result codes.
            else -> Unit
        }
    }
}

/**
 * Processes the current UI state of the PayPal Vault widget and executes corresponding actions
 * such as launching intents, handling success or error states, and managing loading transitions.
 *
 * @param context The current application context.
 * @param uiState The current `PayPalVaultUIState` representing the state of the PayPal Vault process.
 * @param viewModel The `PayPalVaultViewModel` managing the PayPal Vault flow and its state.
 * @param loadingDelegate An optional delegate for managing the widget's loading state transitions.
 * @param resolvePaymentForResult A `ManagedActivityResultLauncher` to handle activity results for payment resolution.
 * @param completion A callback to handle the final result of the PayPal Vault process, either success or failure.
 */
private fun handleUIState(
    context: Context,
    uiState: PayPalVaultUIState,
    viewModel: PayPalVaultViewModel,
    loadingDelegate: WidgetLoadingDelegate?,
    resolvePaymentForResult: ManagedActivityResultLauncher<Intent, ActivityResult>,
    completion: (Result<PayPalVaultResult>) -> Unit,
) {
    when (uiState) {
        // No action is needed for the Idle state.
        is PayPalVaultUIState.Idle -> Unit

        // Handle the loading state by notifying the loading delegate.
        is PayPalVaultUIState.Loading -> {
            loadingDelegate?.widgetLoadingDidStart()
        }

        // Launch the PayPal Vault activity with the client ID and setup token from the UI state.
        is PayPalVaultUIState.LaunchIntent -> {
            loadingDelegate?.widgetLoadingDidFinish()
            val (clientId, setupToken) = uiState
            val intent = Intent(context, PayPalVaultActivity::class.java)
                .putClientIdExtra(clientId)
                .putSetupTokenExtra(setupToken)
            resolvePaymentForResult.launch(intent)
        }

        // Handle the success state, passing the token and email to the completion callback.
        is PayPalVaultUIState.Success -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(
                Result.success(
                    PayPalVaultResult(
                        token = uiState.details.token,
                        email = uiState.details.email
                    )
                )
            )
            // Reset the state to ensure it’s not reused.
            viewModel.resetResultState()
        }

        // Handle the error state, passing the exception to the completion callback.
        is PayPalVaultUIState.Error -> {
            loadingDelegate?.widgetLoadingDidFinish()
            completion(Result.failure(uiState.exception))
            // Reset the state to ensure it’s not reused.
            viewModel.resetResultState()
        }
    }
}