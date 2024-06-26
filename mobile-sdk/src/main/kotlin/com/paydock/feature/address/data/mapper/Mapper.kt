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