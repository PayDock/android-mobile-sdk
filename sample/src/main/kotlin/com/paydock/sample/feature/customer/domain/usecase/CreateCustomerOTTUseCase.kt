package com.paydock.sample.feature.customer.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.customer.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.customer.domain.repository.CustomerRepository
import javax.inject.Inject

class CreateCustomerOTTUseCase @Inject constructor(private val repository: CustomerRepository) {

    suspend operator fun invoke(request: CreateCustomerOTTRequest) =
        suspendRunCatching {
            repository.createCustomer(request)
        }
}