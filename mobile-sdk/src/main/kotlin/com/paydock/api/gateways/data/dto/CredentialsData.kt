package com.paydock.api.gateways.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the credentials needed for authenticating with the PayPal wallet.
 *
 * This data class contains the client authentication token used to authorize PayPal wallet interactions.
 *
 * @property clientAuth The client authentication token used for PayPal wallet operations.
 */
@Serializable
internal data class CredentialsData(
    @SerialName("client_auth") val clientAuth: String
)