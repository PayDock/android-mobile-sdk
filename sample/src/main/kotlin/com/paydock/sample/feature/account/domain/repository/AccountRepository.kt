package com.paydock.sample.feature.account.domain.repository

import com.paydock.sample.feature.account.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.account.domain.model.Customer

interface AccountRepository {

    suspend fun createCustomer(
        request: CreateCustomerOTTRequest,
    ): Customer

}