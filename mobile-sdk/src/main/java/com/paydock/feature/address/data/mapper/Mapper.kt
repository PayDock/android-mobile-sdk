/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
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

package com.paydock.feature.address.data.mapper

import android.location.Address
import com.paydock.feature.address.domain.model.BillingAddress

/**
 * Converts an Android [Address] object to a [BillingAddress] entity.
 *
 * @return A [BillingAddress] entity representing the converted address.
 */
internal fun Address.asEntity(): BillingAddress {
    // Extract street details from the Address object
    val streetNumber = featureName
    val streetName = thoroughfare

    // Construct the street address based on available information
    val streetAddress = if (!streetNumber.isNullOrBlank() && !streetName.isNullOrBlank()) {
        "$streetNumber $streetName"
    } else if (!streetNumber.isNullOrBlank() && streetName.isNullOrBlank()) {
        streetNumber
    } else {
        streetName
    }

    // Create and return a BillingAddress entity
    return BillingAddress(
        addressLine1 = streetAddress ?: "",
        city = locality ?: subLocality ?: "",
        state = adminArea ?: subAdminArea ?: "",
        postalCode = postalCode ?: "",
        country = countryName ?: ""
    )
}