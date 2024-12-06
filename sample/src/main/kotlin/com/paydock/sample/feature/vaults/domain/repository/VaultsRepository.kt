package com.paydock.sample.feature.vaults.domain.repository

import com.paydock.sample.feature.vaults.data.api.dto.VaultTokenRequest

interface VaultsRepository {
    suspend fun createCardVaultToken(request: VaultTokenRequest): String
}