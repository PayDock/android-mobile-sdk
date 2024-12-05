package com.paydock.sample.feature.customer.domain.repository

import com.paydock.sample.feature.customer.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.customer.domain.model.Customer

interface CustomerRepository {

    suspend fun createCustomer(
        request: CreateCustomerOTTRequest,
    ): Customer

}