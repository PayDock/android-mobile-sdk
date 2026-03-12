package com.paydock.feature.zip.data.mapper

import com.paydock.core.MobileSDKConstants
import com.paydock.feature.zip.data.dto.ExternalBillingAddress
import com.paydock.feature.zip.data.dto.ExternalCheckoutCharge
import com.paydock.feature.zip.data.dto.ExternalCheckoutItem
import com.paydock.feature.zip.data.dto.ExternalCheckoutMeta
import com.paydock.feature.zip.data.dto.ExternalCheckoutRequest
import com.paydock.feature.zip.data.dto.ExternalCheckoutStatistics
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
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            gender = gender,
            dateOfBirth = dateOfBirth,
            tokenize = tokenize,
            charge = ExternalCheckoutCharge(
                amount = amount.toDouble(),
                currency = currency,
                shippingType = shippingType,
                billingAddress = billing?.toExternalBillingAddress(),
                shippingAddress = shipping?.toExternalBillingAddress(),
                items = items?.map { it.toExternalCheckoutItem() }
            ),
            statistics = statistics?.toExternalCheckoutStatistics()
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
        addressLine2 = line2,
        addressCity = city,
        addressState = state,
        addressCountry = country,
        addressPostcode = postcode
    )
}

/**
 * Maps a [ZipWidgetConfig.Item] to an [ExternalCheckoutItem].
 *
 * @return The mapped item object.
 */
internal fun ZipWidgetConfig.Item.toExternalCheckoutItem(): ExternalCheckoutItem {
    return ExternalCheckoutItem(
        name = name,
        amount = amount,
        quantity = quantity,
        reference = reference
    )
}

/**
 * Maps a [ZipWidgetConfig.Statistics] to an [ExternalCheckoutStatistics].
 *
 * @return The mapped statistics object.
 */
internal fun ZipWidgetConfig.Statistics.toExternalCheckoutStatistics(): ExternalCheckoutStatistics {
    return ExternalCheckoutStatistics(
        accountCreated = accountCreated,
        salesTotalNumber = salesTotalNumber,
        salesTotalAmount = salesTotalAmount,
        salesAvgValue = salesAvgValue,
        salesMaxValue = salesMaxValue,
        refundsTotalAmount = refundsTotalAmount,
        previousChargeback = previousChargeback,
        currency = currency,
        lastLogin = lastLogin
    )
}
