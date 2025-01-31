package com.paydock.sample.feature.threeDS.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.threeDS.data.api.dto.CreateStandaloneThreeDSTokenRequest
import com.paydock.sample.feature.threeDS.domain.repository.ThreeDSRepository
import javax.inject.Inject

class CreateStandaloneThreeDSTokenUseCase @Inject constructor(private val repository: ThreeDSRepository) {

    suspend operator fun invoke(request: CreateStandaloneThreeDSTokenRequest) = suspendRunCatching {
        repository.createStandalone3dsToken(request)
    }
}