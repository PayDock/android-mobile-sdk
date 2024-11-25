package com.paydock.sample.feature.card.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class VaultTokenResponse(
    val resource: Resource<VaultData>,
    val status: Int,
)
