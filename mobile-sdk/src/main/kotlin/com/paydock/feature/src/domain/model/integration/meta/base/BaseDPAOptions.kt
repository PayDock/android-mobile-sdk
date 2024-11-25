package com.paydock.feature.src.domain.model.integration.meta.base

import com.paydock.feature.src.domain.model.integration.meta.TransactionAmount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the base options for a Direct Payment Application (DPA).
 *
 * @property dpaLocale The DPA’s preferred locale, example en_US.
 * @property dpaAcceptedBillingCountries List of accepted billing countries for DPA in ISO 3166-1 alpha-2 format.
 * @property consumerNameRequested Indicates if consumer name is requested.
 * @property consumerEmailAddressRequested Indicates if consumer email address is requested.
 * @property consumerPhoneNumberRequested Indicates if consumer phone number is requested.
 * @property transactionAmount Details of the transaction amount.
 * @property merchantOrderId Merchant's order ID.
 * @property merchantCategoryCode Merchant's category code.
 * @property merchantCountryCode Merchant's country code in ISO 3166-1 alpha-2 format.
 */
@Serializable
abstract class BaseDPAOptions(
    @SerialName("dpa_locale") val dpaLocale: String? = null,
    @SerialName("dpa_accepted_billing_countries") val dpaAcceptedBillingCountries: List<String>? = null,
    @SerialName("consumer_name_requested") val consumerNameRequested: Boolean? = null,
    @SerialName("consumer_email_address_requested") val consumerEmailAddressRequested: Boolean? = null,
    @SerialName("consumer_phone_number_requested") val consumerPhoneNumberRequested: Boolean? = null,
    @SerialName("transaction_amount") val transactionAmount: TransactionAmount? = null,
    @SerialName("merchant_order_id") val merchantOrderId: String? = null,
    @SerialName("merchant_category_code") val merchantCategoryCode: String? = null,
    @SerialName("merchant_country_code") val merchantCountryCode: String? = null
)
