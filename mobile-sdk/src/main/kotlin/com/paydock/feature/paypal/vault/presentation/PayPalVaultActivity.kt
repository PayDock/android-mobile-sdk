package com.paydock.feature.paypal.vault.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.extensions.positionAwareImePadding
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.paypal.vault.presentation.state.PayPalWebVaultState
import com.paydock.feature.paypal.vault.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.vault.presentation.utils.getClientIdExtra
import com.paydock.feature.paypal.vault.presentation.utils.getSetupTokenExtra
import com.paydock.feature.paypal.vault.presentation.utils.putCancellationStatusExtra
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalWebVaultViewModel
import org.koin.android.ext.android.inject

/**
 * Placeholder Activity responsible for handling the PayPal Vault process.
 *
 * This activity initializes the PayPal vault flow by using the provided client ID and setup token,
 * monitors the vault result via the [PayPalWebVaultViewModel], and manages the result accordingly (success, failure, or cancellation).
 */
internal class PayPalVaultActivity : AppCompatActivity() {

    // Injects an instance of the PayPalWebVaultViewModel
    private val viewModel by inject<PayPalWebVaultViewModel>()

    /**
     * Called when the activity is starting. This is where most initialization should go, including
     * setting up the UI and starting the PayPal vault process.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge()
            // Apply the SDK theme for consistent styling
            // Collect the current vault result state from the ViewModel
            val vaultResult by viewModel.vaultResult.collectAsState()

            // Tracks whether the PayPal vault flow should be initiated
            var shouldStartPayPal by remember { mutableStateOf(true) }

            // SideEffect to start PayPal vault when the activity first loads
            SideEffect {
                if (shouldStartPayPal) {
                    // Retrieve client ID and setup token from the Intent extras
                    val clientId = intent.getClientIdExtra() ?: ""
                    val setupToken = intent.getSetupTokenExtra() ?: ""
                    viewModel.initiatePayPalVault(
                        this@PayPalVaultActivity,
                        clientId,
                        setupToken
                    )
                    shouldStartPayPal = false
                }
            }

            // Box layout to show UI based on the current vault result state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .positionAwareImePadding(),
                contentAlignment = Alignment.Center
            ) {
                when (vaultResult) {
                    // Show a loader during the idle state
                    is PayPalWebVaultState.Idle -> SdkLoader()

                    // Handle user-initiated cancellation and finish the activity
                    is PayPalWebVaultState.Canceled -> {
                        setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED))
                        finish()
                    }

                    // Handle failure state by extracting the error and returning a failure result
                    is PayPalWebVaultState.Failure -> {
                        val error = (vaultResult as PayPalWebVaultState.Failure).error
                        setResult(
                            Activity.RESULT_CANCELED,
                            Intent().apply {
                                putExtra("STATUS", error.code)
                                putExtra("DESCRIPTION", error.errorDescription)
                            }
                        )
                        finish()
                    }

                    // Handle success state by returning the approval session ID and finishing the activity
                    is PayPalWebVaultState.Success -> {
                        val approvalSessionId =
                            (vaultResult as PayPalWebVaultState.Success).approvalSessionId
                        setResult(
                            RESULT_OK,
                            Intent().putExtra("PAYPAL_APPROVAL_SESSION_ID", approvalSessionId)
                        )
                        finish()
                    }
                }
            }
        }
    }

    /**
     * Called when new data is passed to this activity through an Intent.
     * This is necessary to handle the PayPal vault callbacks correctly.
     *
     * @param intent The new Intent that was started for this activity.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Update the intent with the new data to ensure PayPalVault callbacks are triggered
        this.intent = intent
    }
}