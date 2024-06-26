package com.paydock.sample.feature.threeDS.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class ThreeDSTokenResponse(
    val resource: Resource<ThreeDSResourceData>,
    val status: Int
)
