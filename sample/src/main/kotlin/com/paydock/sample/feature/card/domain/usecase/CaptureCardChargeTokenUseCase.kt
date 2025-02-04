package com.paydock.sample.feature.card.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.card.data.api.dto.CaptureCardChargeRequest
import com.paydock.sample.feature.card.domain.repository.CardRepository
import javax.inject.Inject

class CaptureCardChargeTokenUseCase @Inject constructor(private val repository: CardRepository) {

    suspend operator fun invoke(request: CaptureCardChargeRequest) =
        suspendRunCatching {
            repository.captureCardCharge(request)
        }
}