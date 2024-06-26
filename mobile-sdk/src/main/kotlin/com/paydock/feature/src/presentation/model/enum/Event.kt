package com.paydock.feature.src.presentation.model.enum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents different events related to Mastercard SRC.
 */
@Serializable
enum class Event {
    /**
     * Event indicating that the iframe has been loaded.
     */
    @SerialName("iframeLoaded")
    IFRAME_LOADED,

    /**
     * Event indicating that the checkout process is ready.
     */
    @SerialName("checkoutReady")
    CHECKOUT_READY,

    /**
     * Event indicating that the checkout process has been completed successfully.
     */
    @SerialName("checkoutCompleted")
    CHECKOUT_COMPLETED,

    /**
     * Event sent when SRC Checkout flow is started, regardless of embedded or windowed mode.
     */
    @SerialName("checkoutPopupOpen")
    CHECKOUT_POPUP_OPEN,

    /**
     * Event sent when SRC Checkout flow is closed, regardless of embedded or windowed mode.
     */
    @SerialName("checkoutPopupClose")
    CHECKOUT_POPUP_CLOSE,

    /**
     * Event indicating that an error occurred during the checkout process.
     */
    @SerialName("checkoutError")
    CHECKOUT_ERROR
}
