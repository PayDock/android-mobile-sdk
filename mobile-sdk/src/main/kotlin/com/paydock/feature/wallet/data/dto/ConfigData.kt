package com.paydock.feature.wallet.data.dto

import kotlinx.serialization.Serializable

/**
 * Represents the configuration data for the PayPal wallet.
 *
 * This data class holds the configuration details for the wallet, including the mode (e.g., sandbox or production)
 * and the credentials needed for authentication.
 *
 * @property mode The environment mode for the PayPal wallet, such as "sandbox" or "production".
 * @property credentials The credentials required to authenticate the wallet.
 */
@Serializable
internal data class ConfigData(
    val mode: String,
    val credentials: CredentialsData
)