package com.paydock.sample.feature.checkout.data.api.dto

import com.paydock.sample.BuildConfig
import com.paydock.sample.core.AMOUNT
import com.paydock.sample.core.AU_CURRENCY_CODE
import java.math.BigDecimal
import java.util.UUID

sealed class CreateIntentRequest(
    val amount: BigDecimal = BigDecimal(AMOUNT),
    val version: Int = 1,
    val currency: String = AU_CURRENCY_CODE,
    val reference: String = UUID.randomUUID().toString()
) {

    data class TemplateIntentRequest(
        val customisation: Customisation = Customisation(
            templateId = BuildConfig.CUSTOMISATION_TEMPLATE_ID,
            base = null
        ),
        val configuration: Configuration = Configuration(
            templateId = BuildConfig.CONFIGURATION_TEMPLATE_ID,
            paymentMethodOptions = null
        ),
    ) : CreateIntentRequest()

    data class DirectIntentRequest(
        val customisation: Customisation = Customisation(templateId = null, base = Base()),
        val configuration: Configuration = Configuration(
            templateId = null,
            paymentMethodOptions = PaymentMethodOptions()
        ),
    ) : CreateIntentRequest()
}