package com.paydock.core.domain.model.meta

import com.paydock.core.utils.serializer.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Represents the amount and currency of a transaction.
 *
 * @property transactionAmount The amount of the transaction.
 * @property transactionCurrencyCode The currency code of the transaction in 3-letter ISO code format.
 */
@Serializable
data class TransactionAmount(
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("transaction_amount") val transactionAmount: BigDecimal? = null,
    @SerialName("transaction_currency_code") val transactionCurrencyCode: String? = null
)
