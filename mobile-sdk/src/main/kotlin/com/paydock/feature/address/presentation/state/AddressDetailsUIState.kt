package com.paydock.feature.address.presentation.state

/**
 * Represents the different states of the Address Details UI.
 *
 * This sealed class defines the possible states that the address details screen can be in,
 * allowing for clear and concise state management in the UI layer.
 */
internal sealed class AddressDetailsUIState {
    /**
     * Represents the idle state of the address details UI, where no specific action or loading is in progress.
     * This is typically the initial state or the state after an operation has completed.
     */
    data object Idle : AddressDetailsUIState()

    /**
     * Represents the state when address details are being loaded.
     */
    data object Loading : AddressDetailsUIState()
}