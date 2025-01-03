package com.paydock.sample.feature.charges.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.charges.domain.repository.ChargesRepository
import com.paydock.sample.feature.tokens.data.api.dto.Capture3DSChargeRequest
import javax.inject.Inject

class CaptureThreeDSChargeTokenUseCase @Inject constructor(private val repository: ChargesRepository) {

    suspend operator fun invoke(request: Capture3DSChargeRequest) =
        suspendRunCatching {
            repository.capture3DSCharge(request)
        }
}