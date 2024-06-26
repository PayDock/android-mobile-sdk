package com.paydock.feature.charge.domain.model

import com.paydock.core.utils.serializer.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Represents a charge response containing status and resource information.
 *
 * @property status The status code of the charge response.
 * @property resource The resource information containing type and charge data.
 */
@Serializable
data class ChargeResponse(
    val status: Int,
    val resource: ChargeResource
) {
    /**
     * Represents the resource information.
     *
     * @property type The type of the resource.
     * @property data The charge data associated with the resource.
     */
    @Serializable
    data class ChargeResource(
        val type: String,
        val data: ChargeData?
    )

    /**
     * Represents the charge data, including status, amount, and currency.
     *
     * @property status The status of the charge data.
     * @property id The charge id.
     * @property amount The amount of the charge.
     * @property currency The currency in which the charge is made.
     */
    @Serializable
    data class ChargeData(
        val status: String,
        val id: String? = null,
        @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal? = null,
        val currency: String? = null
    )
}
