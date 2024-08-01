package com.paydock.sample.feature.checkout.domain.repository

import com.paydock.sample.feature.checkout.data.api.dto.CreateIntentRequest

interface CheckoutRepository {
    suspend fun createCheckoutIntentToken(request: CreateIntentRequest): String
}