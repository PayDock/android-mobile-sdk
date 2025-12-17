package com.paydock.sample.feature.account.data.repository

import com.paydock.sample.feature.account.data.api.AccountApi
import com.paydock.sample.feature.account.data.api.dto.CreateCustomerOTTRequest
import com.paydock.sample.feature.account.data.mapper.toDomain
import com.paydock.sample.feature.account.domain.model.Customer
import com.paydock.sample.feature.account.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AccountRepositoryImpl(
    private val dispatcher: CoroutineDispatcher,
    private val accountApi: AccountApi,
) : AccountRepository {
    override suspend fun createCustomer(
        accessToken: String,
        request: CreateCustomerOTTRequest
    ): Customer =
        withContext(dispatcher) {
            accountApi.createCustomer(accessToken, request = request).toDomain()
        }
}