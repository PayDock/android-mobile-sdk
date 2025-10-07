package com.paydock.feature.paypal.vault.presentation

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
import com.paydock.core.presentation.extensions.putMessageExtra
import com.paydock.core.presentation.extensions.putStatusExtra
import com.paydock.designsystems.components.loader.SdkLoader
import com.paydock.feature.paypal.vault.presentation.state.PayPalWebVaultState
import com.paydock.feature.paypal.vault.presentation.utils.CancellationStatus
import com.paydock.feature.paypal.vault.presentation.utils.getClientIdExtra
import com.paydock.feature.paypal.vault.presentation.utils.getSetupTokenExtra
import com.paydock.feature.paypal.vault.presentation.utils.putApprovalSessionIdExtra
import com.paydock.feature.paypal.vault.presentation.utils.putCancellationStatusExtra
import com.paydock.feature.paypal.vault.presentation.viewmodel.PayPalWebVaultViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * An internal [AppCompatActivity] that handles the PayPal vaulting process.
 *
 * This activity is responsible for initiating the PayPal vaulting flow,
 * handling deep links from the PayPal authentication process, and returning the result
 * to the calling activity.
 *
 * It observes the [PayPalWebVaultViewModel] to manage the state of the vaulting process
 * and updates the UI accordingly.
 *
 * The activity expects the following extras in its launch intent:
 * - `EXTRA_CLIENT_ID`: The PayPal client ID.
 * - `EXTRA_SETUP_TOKEN`: The PayPal setup token.
 *
 * Upon completion, the activity will finish with a result code:
 * - [android.app.Activity.RESULT_OK]: If the vaulting was successful. The result [Intent] will contain
 *   the `EXTRA_APPROVAL_SESSION_ID`.
 * - [android.app.Activity.RESULT_CANCELED]: If the vaulting was canceled or failed.
 *   - If canceled by the user, the result [Intent] will contain `EXTRA_CANCELLATION_STATUS` set to [CancellationStatus.USER_INITIATED].
 *   - If an error occurred, the result [Intent] will contain `EXTRA_STATUS` (error code) and `EXTRA_MESSAGE` (error description).
 */
internal class PayPalVaultActivity : AppCompatActivity() {

    private val viewModel: PayPalWebVaultViewModel by viewModel()
    private var hasStartedVault: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasStartedVault = savedInstanceState?.getBoolean(KEY_HAS_STARTED) ?: false
        setContent {
            enableEdgeToEdge()
            val vaultResult by viewModel.vaultResult.collectAsState()

            var shouldStartPayPal by remember { mutableStateOf(!hasStartedVault) }

            SideEffect {
                if (shouldStartPayPal) {
                    val clientId = intent.getClientIdExtra() ?: ""
                    val setupToken = intent.getSetupTokenExtra() ?: ""
                    viewModel.initiatePayPalVault(
                        this@PayPalVaultActivity,
                        clientId,
                        setupToken
                    )
                    shouldStartPayPal = false
                    hasStartedVault = true
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .positionAwareImePadding(),
                contentAlignment = Alignment.Center
            ) {
                when (vaultResult) {
                    is PayPalWebVaultState.Idle -> SdkLoader()
                    is PayPalWebVaultState.Canceled -> {
                        setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED))
                        finish()
                    }
                    is PayPalWebVaultState.Failure -> {
                        val error = (vaultResult as PayPalWebVaultState.Failure).error
                        setResult(
                            RESULT_CANCELED,
                            Intent().apply {
                                putStatusExtra(error.code)
                                putMessageExtra(error.errorDescription)
                            }
                        )
                        finish()
                    }
                    is PayPalWebVaultState.Success -> {
                        val approvalSessionId = (vaultResult as PayPalWebVaultState.Success).approvalSessionId
                        setResult(
                            RESULT_OK,
                            Intent().putApprovalSessionIdExtra(approvalSessionId)
                        )
                        finish()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_HAS_STARTED, hasStartedVault)
    }

    /**
     * Handles new intents received by the activity, typically from deep links.
     *
     * This method is called when the activity is re-launched while it's already running,
     * such as when PayPal redirects back to the app after authentication.
     *
     * It checks for a specific deep link pattern (`vault/cancel`) which indicates user cancellation
     * from the PayPal flow. If this pattern is detected, the activity finishes with a
     * `RESULT_CANCELED` and a [CancellationStatus.USER_INITIATED] status.
     *
     * Otherwise, it delegates the handling of the deep link result to the [PayPalWebVaultViewModel].
     *
     * @param intent The new intent that was started for the activity.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        val data = intent.data
        val host = data?.host
        val pathSegment = data?.pathSegments?.firstOrNull()
        if (host == "vault" && pathSegment == "cancel") {
            setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED))
            finish()
            return
        }
        viewModel.handleDeeplinkResult(this, intent)
    }

    /**
     * Handles the activity resuming.
     *
     * This method checks for specific deep link scenarios (e.g., user cancellation from PayPal)
     * and also implements a fallback mechanism to handle cases where the activity resumes
     * without a redirect intent (e.g., if the browser was closed manually by the user)
     * while the vaulting process is still in an Idle state. In such cases, it assumes
     * the user initiated a cancellation.
     */
    override fun onResume() {
        super.onResume()
        val data = intent?.data
        val host = data?.host
        val pathSegment = data?.pathSegments?.firstOrNull()
        if (host == "vault" && pathSegment == "cancel") {
            setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED))
            finish()
            return
        }

        // Fallback: if returning without redirect intent and vault flow was already started, finish as canceled
        if (hasStartedVault && data == null) {
            setResult(RESULT_CANCELED, Intent().putCancellationStatusExtra(CancellationStatus.USER_INITIATED))
            finish()
            return
        }
    }

    private companion object {
        const val KEY_HAS_STARTED: String = "paypal.vault.has_started"
    }
}
