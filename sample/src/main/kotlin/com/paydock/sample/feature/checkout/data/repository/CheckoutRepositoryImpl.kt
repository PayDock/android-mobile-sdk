package com.paydock.sample.feature.checkout.data.repository

import com.paydock.sample.feature.checkout.data.api.CheckoutApi
import com.paydock.sample.feature.checkout.data.api.dto.CreateIntentRequest
import com.paydock.sample.feature.checkout.domain.repository.CheckoutRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckoutRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val checkoutApi: CheckoutApi
) : CheckoutRepository {
    override suspend fun createCheckoutIntentToken(request: CreateIntentRequest): String =
        withContext(dispatcher) {
            checkoutApi.createCheckoutIntentToken(request = request).resource.resourceData.token
        }
}