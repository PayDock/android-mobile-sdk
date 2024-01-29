/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.charge.domain.model

import com.paydock.core.utils.BigDecimalSerializer
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
        val id: String,
        @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
        val currency: String
    )
}
