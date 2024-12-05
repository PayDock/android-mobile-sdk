package com.paydock.sample.feature.customer.data.api.dto

import com.google.gson.annotations.SerializedName

data class CustomerDTO(
    @SerializedName("company_id") val companyId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val phone: String,
    @SerializedName("_check_expire_date") val checkExpireDate: Boolean,
    @SerializedName("_service") val service: ServiceDTO,
    val statistics: StatisticsDTO,
    val archived: Boolean,
    val status: Boolean,
    @SerializedName("payment_sources") val paymentSources: List<PaymentSourceDTO>,
    @SerializedName("default_source") val defaultSource: String,
    @SerializedName("_source_ip_address") val sourceIpAddress: String,
    @SerializedName("_id") val id: String,
    @SerializedName("payment_destinations") val paymentDestinations: List<String>?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
) {

    data class PaymentSourceDTO(
        val type: String,
        @SerializedName("checkout_holder") val checkoutHolder: String,
        @SerializedName("checkout_email") val checkoutEmail: String,
        @SerializedName("external_payer_id") val externalPayerId: String,
        val status: String,
        @SerializedName("gateway_id") val gatewayId: String,
        @SerializedName("gateway_name") val gatewayName: String,
        @SerializedName("gateway_type") val gatewayType: String,
        @SerializedName("gateway_mode") val gatewayMode: String,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("updated_at") val updatedAt: String,
        @SerializedName("ref_token") val refToken: String,
        @SerializedName("_id") val id: String,
    )

}
