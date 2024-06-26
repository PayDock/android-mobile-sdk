package com.paydock.sample.feature.threeDS.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.threeDS.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.repository.ThreeDSRepository
import javax.inject.Inject

class CreateIntegratedThreeDSTokenUseCase @Inject constructor(private val repository: ThreeDSRepository) {

    suspend operator fun invoke(request: CreateIntegratedThreeDSTokenRequest) = suspendRunCatching {
        repository.createIntegrated3dsToken(request)
    }
}