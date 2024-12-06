package com.paydock.sample.feature.vaults.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.vaults.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.vaults.domain.repository.VaultsRepository
import javax.inject.Inject

class CreateCardSessionVaultTokenUseCase @Inject constructor(private val repository: VaultsRepository) {

    suspend operator fun invoke(request: VaultTokenRequest.CreateCardSessionVaultTokenRequest) =
        suspendRunCatching {
            repository.createCardVaultToken(request)
        }
}