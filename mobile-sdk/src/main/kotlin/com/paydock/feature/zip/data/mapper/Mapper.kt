package com.paydock.feature.zip.data.mapper

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.zip.data.dto.ExternalBillingAddress
import com.paydock.feature.zip.data.dto.ExternalCheckoutCharge
import com.paydock.feature.zip.data.dto.ExternalCheckoutMeta
import com.paydock.feature.zip.data.dto.ExternalCheckoutRequest
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig

/**
 * Maps a [ZipWidgetConfig] to an [ExternalCheckoutRequest].
 *
 * @return The mapped request object ready for API submission.
 */
internal fun ZipWidgetConfig.toExternalCheckoutRequest(): ExternalCheckoutRequest {
    return ExternalCheckoutRequest(
        gatewayId = gatewayId,
        meta = ExternalCheckoutMeta(
            firstName = shopper.firstName,
            lastName = shopper.lastName,
            email = shopper.email,
            tokenize = true,
            charge = ExternalCheckoutCharge(
                amount = amount.toDouble(),
                currency = currency,
                billingAddress = billing?.toExternalBillingAddress()
            )
        ),
        successRedirectUrl = MobileSDKConstants.ZipConfig.ZIP_REDIRECT_URL,
        errorRedirectUrl = MobileSDKConstants.ZipConfig.ZIP_REDIRECT_URL,
        redirectUrl = MobileSDKConstants.ZipConfig.ZIP_REDIRECT_URL
    )
}

/**
 * Maps a [ZipWidgetConfig.Address] to an [ExternalBillingAddress].
 *
 * @return The mapped billing address object.
 */
internal fun ZipWidgetConfig.Address.toExternalBillingAddress(): ExternalBillingAddress {
    return ExternalBillingAddress(
        firstName = firstName,
        lastName = lastName,
        addressLine1 = line1,
        addressCity = city,
        addressState = state,
        addressCountry = countryCode,
        addressPostcode = postalCode
    )
}
