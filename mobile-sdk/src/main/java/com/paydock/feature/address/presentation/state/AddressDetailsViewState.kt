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

package com.paydock.feature.address.presentation.state

import com.paydock.feature.address.domain.model.BillingAddress

/**
 * UI State that represents address details input and processing state.
 *
 * @property addressLine1 The first line of the address.
 * @property addressLine2 The second line of the address.
 * @property city The city of the address.
 * @property state The state of the address.
 * @property postalCode The postal code of the address.
 * @property country The country of the address.
 */
internal data class AddressDetailsViewState(
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = ""
) {
    /**
     * Get the [BillingAddress] representation of the input address.
     */
    val billingAddress: BillingAddress
        get() = BillingAddress(
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country
        )

    /**
     * Get the validity status of all input data.
     */
    val isDataValid: Boolean
        get() = addressLine1.isNotBlank() &&
            city.isNotBlank() &&
            state.isNotBlank() &&
            postalCode.isNotBlank() &&
            country.isNotBlank()
}