package com.paydock.sample.feature.customer.data.repository

import com.paydock.sample.feature.customer.data.api.CustomerApi
import com.paydock.sample.feature.customer.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.customer.data.mapper.toDomain
import com.paydock.sample.feature.customer.domain.model.Customer
import com.paydock.sample.feature.customer.domain.repository.CustomerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CustomerRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val customerApi: CustomerApi,
) : CustomerRepository {
    override suspend fun createCustomer(request: CreateCustomerOTTRequest): Customer =
        withContext(dispatcher) {
            customerApi.createCustomer(request = request).toDomain()
        }
}