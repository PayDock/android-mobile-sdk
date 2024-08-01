package com.paydock.sample.feature.checkout.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class CheckoutIntentResponse(
    val resource: Resource<CheckoutData>,
    val status: Int
)