package com.paydock.feature.wallet.data.dto

import com.paydock.core.utils.serializer.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Serializable data class representing wallet capture information.
 * This class holds details regarding the wallet, including its status, ID, amount, and currency.
 *
 * @property status The status of the wallet.
 * @property id The unique identifier of the wallet.
 * @property amount The amount available in the wallet.
 * @property currency The currency type used in the wallet.
 */
@Serializable
internal data class CaptureData(
    val status: String,
    val id: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val currency: String
)