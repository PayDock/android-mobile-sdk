package com.paydock.feature.paypal.core.presentation

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import com.paydock.MobileSDK
import com.paydock.core.domain.mapper.mapToPayPalEnv
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient

/**
 * Manager for PayPal Web Client lifecycle and configuration.
 *
 * Handles client initialization, recreation after process death, and cleanup.
 * This class centralizes the common client management logic used by both checkout and vault flows.
 *
 * Note: PayPal SDK team is considering renaming PayPalWebCheckoutClient to PayPalWebClient
 * in the next major version since it now handles both checkout and vaulting.
 *
 * @property savedStateHandle Handle for persisting client configuration across process death.
 * @property returnUrl The deep link URL scheme for PayPal redirects.
 */
internal class PayPalWebClientManager(
    private val savedStateHandle: SavedStateHandle,
    private val returnUrl: String
) {
    private var client: PayPalWebCheckoutClient? = null

    // Persisted configuration for client recreation after process death
    private var clientId: String?
        get() = savedStateHandle[KEY_CLIENT_ID]
        set(value) { savedStateHandle[KEY_CLIENT_ID] = value }

    /**
     * Initializes or retrieves the PayPal Web Client.
     *
     * If the client is null (e.g., after process death), it will be recreated using
     * persisted configuration.
     *
     * @param activity The hosting activity.
     * @param newClientId The PayPal client ID (optional - uses persisted if not provided).
     * @return The initialized PayPalWebCheckoutClient, or null if configuration is missing.
     */
    fun getOrCreateClient(
        activity: AppCompatActivity,
        newClientId: String? = null
    ): PayPalWebCheckoutClient? {
        // Update stored clientId if provided
        newClientId?.let { clientId = it }

        // Return existing client if available
        if (client != null) return client

        // Recreate client if we have persisted configuration
        val savedClientId = clientId ?: return null

        val coreConfig = CoreConfig(
            clientId = savedClientId,
            environment = MobileSDK.getInstance().environment.mapToPayPalEnv()
        )

        return PayPalWebCheckoutClient(activity, coreConfig, returnUrl).also {
            client = it
        }
    }

    /**
     * Explicitly sets the client instance.
     * Useful for dependency injection in tests.
     */
    fun setClient(newClient: PayPalWebCheckoutClient) {
        client = newClient
    }

    /**
     * Clears the client instance.
     * Should be called in ViewModel.onCleared().
     */
    fun clear() {
        client = null
    }

    private companion object {
        const val KEY_CLIENT_ID: String = "paypal.web.client_id"
    }
}
