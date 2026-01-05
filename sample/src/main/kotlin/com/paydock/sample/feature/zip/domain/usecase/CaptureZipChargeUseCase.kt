package com.paydock.sample.feature.zip.domain.usecase

import com.paydock.sample.core.extensions.suspendRunCatching
import com.paydock.sample.feature.zip.data.api.dto.CaptureZipChargeRequest
import com.paydock.sample.feature.zip.domain.repository.ZipRepository
import javax.inject.Inject

class CaptureZipChargeUseCase @Inject constructor(
    private val repository: ZipRepository,
) {

    suspend operator fun invoke(accessToken: String, request: CaptureZipChargeRequest) =
        suspendRunCatching {
            repository.captureZipCharge(accessToken, request)
        }
}

