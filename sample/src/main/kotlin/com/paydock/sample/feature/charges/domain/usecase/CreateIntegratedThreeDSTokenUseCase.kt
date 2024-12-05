package com.paydock.sample.feature.charges.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.charges.data.api.dto.CreateIntegratedThreeDSTokenRequest
import com.paydock.sample.feature.charges.domain.repository.ChargesRepository
import javax.inject.Inject

class CreateIntegratedThreeDSTokenUseCase @Inject constructor(private val repository: ChargesRepository) {

    suspend operator fun invoke(accessToken: String, request: CreateIntegratedThreeDSTokenRequest) =
        suspendRunCatching {
            repository.createIntegrated3dsToken(accessToken, request)
        }
}