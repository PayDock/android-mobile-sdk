package com.paydock.sample.feature.vaults.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.vaults.data.api.dto.VaultTokenRequest
import com.paydock.sample.feature.vaults.domain.repository.VaultsRepository
import javax.inject.Inject

class CreateCardVaultTokenUseCase @Inject constructor(private val repository: VaultsRepository) {

    suspend operator fun invoke(request: VaultTokenRequest.CreateCardVaultTokenRequest) =
        suspendRunCatching {
            repository.createCardVaultToken(request)
        }
}