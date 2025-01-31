package com.paydock.feature.wallet.data.dto

import kotlinx.serialization.Serializable

/**
 * Serializable data class representing wallet decline information.
 *
 * @property status The status of the wallet.
 */
@Serializable
internal data class WalletDeclineData(
    val status: String
)