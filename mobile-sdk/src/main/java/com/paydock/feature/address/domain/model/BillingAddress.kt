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

package com.paydock.feature.address.domain.model

/**
 * Represents a billing address with its components.
 *
 * @property addressLine1 The first line of the billing address, typically containing street information.
 * @property addressLine2 The optional second line of the billing address, often used for additional details.
 * @property city The city or locality of the billing address.
 * @property state The state or region of the billing address.
 * @property postalCode The postal or ZIP code of the billing address.
 * @property country The country of the billing address.
 */
data class BillingAddress(
    val addressLine1: String = "",
    val addressLine2: String? = null,
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = ""
)