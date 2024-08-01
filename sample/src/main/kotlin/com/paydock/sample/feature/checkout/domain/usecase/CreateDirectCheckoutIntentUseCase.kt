package com.paydock.sample.feature.checkout.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.checkout.data.api.dto.CreateIntentRequest
import com.paydock.sample.feature.checkout.domain.repository.CheckoutRepository
import javax.inject.Inject

class CreateDirectCheckoutIntentUseCase @Inject constructor(private val repository: CheckoutRepository) {

    suspend operator fun invoke(request: CreateIntentRequest.DirectIntentRequest) =
        suspendRunCatching {
            repository.createCheckoutIntentToken(request)
        }
}