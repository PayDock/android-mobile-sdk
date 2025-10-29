package com.paydock.feature.address.domain.mapper.integration

import android.location.Address
import com.paydock.feature.address.domain.model.integration.BillingAddress

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
    // Check if values are identical to avoid duplicates (e.g., "Main Street Main Street")
    val streetAddress = when {
        !streetNumber.isNullOrBlank() && !streetName.isNullOrBlank() -> {
            // If both exist but are identical, use only one
            if (streetNumber.trim().equals(streetName.trim(), ignoreCase = true)) {
                streetName
            } else {
                "$streetNumber $streetName"
            }
        }
        !streetNumber.isNullOrBlank() && streetName.isNullOrBlank() -> streetNumber
        else -> streetName
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