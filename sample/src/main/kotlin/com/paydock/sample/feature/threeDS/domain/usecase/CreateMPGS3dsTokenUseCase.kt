package com.paydock.sample.feature.threeDS.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.threeDS.data.api.dto.CreateMPGS3dsTokenRequest
import com.paydock.sample.feature.threeDS.domain.repository.ThreeDSRepository
import javax.inject.Inject

class CreateMPGS3dsTokenUseCase @Inject constructor(private val repository: ThreeDSRepository) {

    suspend operator fun invoke(accessToken: String, request: CreateMPGS3dsTokenRequest) =
        suspendRunCatching {
            repository.createMPGS3dsToken(accessToken, request)
        }
}