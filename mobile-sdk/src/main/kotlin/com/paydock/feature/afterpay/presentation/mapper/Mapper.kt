package com.paydock.feature.afterpay.presentation.mapper

import com.afterpay.android.AfterpayCheckoutV2Options
import com.afterpay.android.CancellationStatus
import com.afterpay.android.model.Money
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionUpdate
import com.afterpay.android.model.ShippingOptionUpdateSuccessResult
import com.afterpay.android.model.ShippingOptionsSuccessResult
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.address.domain.model.BillingAddress
import com.paydock.feature.afterpay.presentation.model.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOption
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOptionUpdate
import java.util.Locale

/**
 * Maps a list of AfterpayShippingOption to SDK ShippingOptions.
 *
 * @return List of SDK ShippingOption objects.
 */
fun List<AfterpayShippingOption>.mapToSDKShippingOptions(): List<ShippingOption> =
    this.map { shipping -> shipping.mapToSDKShippingOption() }

/**
 * Maps an AfterpayShippingOption to an SDK ShippingOption.
 *
 * @return SDK ShippingOption object.
 */
fun AfterpayShippingOption.mapToSDKShippingOption(): ShippingOption = ShippingOption(
    this.id,
    this.name,
    this.description,
    Money(this.shippingAmount, this.currency),
    Money(this.orderAmount, this.currency),
    this.taxAmount?.let { Money(it, this.currency) },
)

/**
 * Maps an SDK ShippingOption to an AfterpayShippingOption.
 *
 * @return AfterpayShippingOption object.
 */
fun ShippingOption.mapFromShippingOption(): AfterpayShippingOption = AfterpayShippingOption(
    this.id,
    this.name,
    this.description,
    this.orderAmount.currency,
    this.shippingAmount.amount,
    this.orderAmount.amount,
    this.taxAmount?.amount
)

/**
 * Maps a list of AfterpayShippingOption to SDK ShippingOptionsSuccessResult.
 *
 * @return ShippingOptionsSuccessResult object.
 */
fun List<AfterpayShippingOption>.mapToSDKShippingOptionResult(): ShippingOptionsSuccessResult =
    ShippingOptionsSuccessResult(this.mapToSDKShippingOptions())

/**
 * Maps an AfterpayShippingOptionUpdate to SDK ShippingOptionUpdateSuccessResult.
 *
 * @return ShippingOptionUpdateSuccessResult object.
 */
fun AfterpayShippingOptionUpdate.mapToSDKShippingOptionUpdateResult(): ShippingOptionUpdateSuccessResult =
    ShippingOptionUpdateSuccessResult(this.mapToSDKShippingOptionUpdate())

/**
 * Maps an AfterpayShippingOptionUpdate to SDK ShippingOptionUpdate.
 *
 * @return ShippingOptionUpdate object.
 */
fun AfterpayShippingOptionUpdate.mapToSDKShippingOptionUpdate(): ShippingOptionUpdate =
    ShippingOptionUpdate(
        this.id,
        Money(this.shippingAmount, this.currency),
        Money(this.orderAmount, this.currency),
        this.taxAmount?.let { Money(it, this.currency) },
    )

/**
 * Maps an SDK ShippingAddress to BillingAddress.
 *
 * @return BillingAddress object.
 */
fun ShippingAddress.mapFromBillingAddress(): BillingAddress =
    BillingAddress(
        name = this.name,
        addressLine1 = this.address1,
        addressLine2 = this.address2,
        city = this.suburb,
        state = this.state,
        country = this.countryCode?.let {
            Locale("", it).displayCountry
        },
        postalCode = this.postcode,
        phoneNumber = this.phoneNumber
    )

/**
 * Maps checkout options to Afterpay V2 options.
 *
 * @return Afterpay V2 options based on the checkout options.
 */
fun AfterpaySDKConfig.CheckoutOptions.mapToAfterpayV2Options(): AfterpayCheckoutV2Options =
    AfterpayCheckoutV2Options(
        pickup = this.pickup,
        buyNow = this.buyNow,
        shippingOptionRequired = this.shippingOptionRequired,
        enableSingleShippingOptionUpdate = this.enableSingleShippingOptionUpdate
    )

fun CancellationStatus.mapMessage(): String =
    when (this) {
        CancellationStatus.USER_INITIATED -> MobileSDKConstants.Afterpay.USER_INITIATED_ERROR_MESSAGE
        CancellationStatus.NO_CHECKOUT_URL -> MobileSDKConstants.Afterpay.NO_CHECKOUT_URL_ERROR_MESSAGE
        CancellationStatus.INVALID_CHECKOUT_URL -> MobileSDKConstants.Afterpay.INVALID_CHECKOUT_URL_ERROR_MESSAGE
        CancellationStatus.NO_CHECKOUT_HANDLER -> MobileSDKConstants.Afterpay.NO_CHECKOUT_HANDLER_ERROR_MESSAGE
        CancellationStatus.NO_CONFIGURATION -> MobileSDKConstants.Afterpay.NO_CONFIGURATION_ERROR_MESSAGE
        CancellationStatus.LANGUAGE_NOT_SUPPORTED -> MobileSDKConstants.Afterpay.LANGUAGE_NOT_SUPPORTED_ERROR_MESSAGE
    }