package com.paydock.sample.feature.threeDS.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.threeDS.data.api.dto.Capture3DSChargeRequest
import com.paydock.sample.feature.threeDS.domain.repository.ThreeDSRepository
import javax.inject.Inject

class CaptureThreeDSChargeTokenUseCase @Inject constructor(private val repository: ThreeDSRepository) {

    suspend operator fun invoke(request: Capture3DSChargeRequest) =
        suspendRunCatching {
            repository.capture3DSCharge(request)
        }
}