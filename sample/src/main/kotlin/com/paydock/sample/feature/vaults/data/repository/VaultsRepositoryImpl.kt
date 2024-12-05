package com.paydock.sample.feature.vaults.data.repository

import com.paydock.sample.feature.vaults.data.api.VaultsApi
import com.paydock.sample.feature.vaults.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.vaults.domain.repository.VaultsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VaultsRepositoryImpl @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val vaultsApi: VaultsApi,
) : VaultsRepository {

    override suspend fun createCardVaultToken(request: VaultTokenRequest): String =
        withContext(dispatcher) {
            vaultsApi.createVaultToken(request = request).resource.resourceData.token
        }
}