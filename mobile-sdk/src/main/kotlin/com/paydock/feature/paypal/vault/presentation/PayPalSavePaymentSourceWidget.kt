package com.paydock.feature.paypal.vault.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.error.exceptions.PayPalVaultException
import com.paydock.core.presentation.extensions.getMessageExtra
import com.paydock.core.presentation.extensions.getStatusExtra
import com.paydock.designsystems.components.button.AppButtonType
import com.paydock.designsystems.components.button.SdkButton
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
 * @param config The configuration for PayPal vault, including the access token and gateway ID.
 * @param completion The callback invoked when the PayPal linking process completes, either with a success
 *                   containing a [PayPalVaultResult] or a failure containing a [PayPalVaultException].
 */
@Suppress("LongMethod")
@Composable
fun PayPalSavePaymentSourceWidget(
    modifier: Modifier = Modifier,
    config: PayPalVaultConfig,
    completion: (Result<PayPalVaultResult>) -> Unit,
) {
    // Obtain the current context
    val context = LocalContext.current

    // Get the PayPalVaultViewModel from Koin, passing the config as a parameter
    val viewModel: PayPalVaultViewModel = koinViewModel(parameters = { parametersOf(config) })
    var isPayPalFlowLaunched by remember { mutableStateOf(false) }

    // Collect the current state from the ViewModel's state flow
    val uiState by viewModel.stateFlow.collectAsState()

    // ActivityResultLauncher for handling payment resolution
    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        result.data?.let { data ->
            when (result.resultCode) {
                // Handles the success case when the result code is RESULT_OK, passing the order ID.
                AppCompatActivity.RESULT_OK -> {
                    viewModel.createPayPalPaymentSourceToken()
                }

                // Handles the cancellation case when the result code is RESULT_CANCELED.
                AppCompatActivity.RESULT_CANCELED -> {
                    when (data.getCancellationStatusExtra()) {
                        // If the cancellation was caused by invalid clientId and/or setup token.
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
                        // If the cancellation was user-initiated, the completion is invoked with a failure result.
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

                        // If the cancellation was due to another reason, process the SDK error status and message.
                        else -> {
                            val status = data.getStatusExtra()
                            val message =
                                data.getMessageExtra(MobileSDKConstants.Errors.PAY_PAL_VAULT_ERROR)
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

                // If no specific result code is handled, do nothing.
                else -> Unit
            }
        }

    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PayPalVaultUIState.Idle,
            is PayPalVaultUIState.Loading -> Unit
            is PayPalVaultUIState.LaunchIntent -> {
                if (!isPayPalFlowLaunched) {
                    val (clientId, setupToken) = state // Assuming destructuring is supported
                    val intent = Intent(context, PayPalVaultActivity::class.java)
                        .putClientIdExtra(clientId)
                        .putSetupTokenExtra(setupToken)
                    resolvePaymentForResult.launch(intent)
                    isPayPalFlowLaunched = true
                }
            }

            is PayPalVaultUIState.Success -> {
                completion(Result.success(PayPalVaultResult(token = state.details.token, email = state.details.email)))
                // This ensures that we clear the state so it's not reused
                viewModel.resetResultState()
            }

            is PayPalVaultUIState.Error -> {
                completion(Result.failure(state.exception as Throwable))
                // This ensures that we clear the state so it's not reused
                viewModel.resetResultState()
            }
        }
    }

    // Apply the SDK's theme to the widget
    SdkTheme {
        // Display a button to link the PayPal account
        SdkButton(
            modifier = modifier
                .testTag("linkPayPalAccount"),
            iconDrawable = R.drawable.ic_link,
            text = config.actionText ?: stringResource(id = R.string.button_link_paypal_account),
            type = AppButtonType.Outlined,
            enabled = uiState !is PayPalVaultUIState.Loading,
            isLoading = uiState is PayPalVaultUIState.Loading
        ) {
            viewModel.createCustomerSessionAuthToken()
            isPayPalFlowLaunched = false
        }
    }
}