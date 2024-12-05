package com.paydock.feature.threeDS.presentation.state

import com.paydock.core.domain.error.exceptions.SdkException
import com.paydock.feature.threeDS.domain.model.integration.ThreeDSResult

internal sealed class ThreeDSUIState {
    data object Idle : ThreeDSUIState()
    data object Loading : ThreeDSUIState()
    data class Success(val result: ThreeDSResult) : ThreeDSUIState()
    data class Error(val exception: SdkException) : ThreeDSUIState()
}