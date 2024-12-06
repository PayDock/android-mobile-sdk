package com.paydock.feature.address.presentation.state

internal sealed class AddressDetailsUIState {
    data object Idle : AddressDetailsUIState()
    data object Loading : AddressDetailsUIState()
}