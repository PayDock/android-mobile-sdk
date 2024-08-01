package com.paydock.core.domain.model.meta

import com.paydock.core.domain.model.meta.base.BaseDPAOptions
import com.paydock.core.domain.model.meta.enum.ClickToPayDPAShippingBillingPreference
import com.paydock.core.domain.model.meta.enum.ClickToPayOrderType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents options related to Mastercard's Digital Payment Application (DPA).
 *
 * @property dpaBillingPreference Billing preferences for DPA, options are 'FULL', 'POSTAL_COUNTRY', 'NONE'.
 * @property paymentOptions Payment options included in the transaction.
 * @property orderType Type of the order, options are 'SPLIT_SHIPMENT', 'PREFERRED_CARD'.
 * @property threeDSPreference Preference for 3DS usage in the transaction.
 * @property confirmPayment Indicates if payment confirmation is required.
 */
@Serializable
data class ClickToPayDPAOptions(
    @SerialName("dpa_billing_preference") val dpaBillingPreference: ClickToPayDPAShippingBillingPreference? = null,
    @SerialName("payment_options") val paymentOptions: List<PaymentOption>? = null,
    @SerialName("order_type") val orderType: ClickToPayOrderType? = null,
    @SerialName("three_ds_preference") val threeDSPreference: String? = null,
    @SerialName("confirm_payment") val confirmPayment: Boolean? = null
) : BaseDPAOptions()
