package com.paydock.feature.wallet.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the credentials needed for authenticating with the wallet ie. PayPal.
 *
 * This data class contains the client authentication token used to authorize wallet interactions.
 *
 * @property clientAuth The client authentication token used for wallet operations.
 */
@Serializable
internal data class CredentialsData(
    @SerialName("client_auth") val clientAuth: String
)