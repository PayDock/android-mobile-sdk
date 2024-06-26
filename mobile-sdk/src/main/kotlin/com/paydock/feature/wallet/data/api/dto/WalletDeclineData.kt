package com.paydock.feature.wallet.data.api.dto

import kotlinx.serialization.Serializable

/**
 * Serializable data class representing wallet decline information.
 *
 * @property status The status of the wallet.
 */
@Serializable
data class WalletDeclineData(
    val status: String
)