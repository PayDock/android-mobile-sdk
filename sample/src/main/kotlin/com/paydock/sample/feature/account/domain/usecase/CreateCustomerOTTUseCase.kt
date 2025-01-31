package com.paydock.sample.feature.account.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.account.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.account.domain.repository.AccountRepository
import javax.inject.Inject

class CreateCustomerOTTUseCase @Inject constructor(private val repository: AccountRepository) {

    suspend operator fun invoke(request: CreateCustomerOTTRequest) =
        suspendRunCatching {
            repository.createCustomer(request)
        }
}