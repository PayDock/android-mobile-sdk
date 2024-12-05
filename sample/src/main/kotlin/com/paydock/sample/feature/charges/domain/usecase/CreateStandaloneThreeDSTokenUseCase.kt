package com.paydock.sample.feature.charges.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.charges.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.charges.domain.repository.ChargesRepository
import javax.inject.Inject

class CreateStandaloneThreeDSTokenUseCase @Inject constructor(private val repository: ChargesRepository) {

    suspend operator fun invoke(request: CreateStandaloneThreeDSTokenRequest) = suspendRunCatching {
        repository.createStandalone3dsToken(request)
    }
}