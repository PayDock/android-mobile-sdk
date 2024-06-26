package com.paydock.feature.afterpay.presentation.utils

import com.afterpay.android.AfterpayCheckoutV2Handler
import com.afterpay.android.model.ShippingAddress
import com.afterpay.android.model.ShippingOption
import com.afterpay.android.model.ShippingOptionUpdateResult
import com.afterpay.android.model.ShippingOptionsResult

/**
 * Handles checkout-related events and actions.
 *
 * @param onDidCommenceCheckout Callback invoked when checkout process begins.
 * @param onShippingAddressDidChange Callback invoked when shipping address changes.
 * @param onShippingOptionDidChange Callback invoked when shipping option changes.
 */
class CheckoutHandler(
    val onDidCommenceCheckout: () -> Unit,
    val onShippingAddressDidChange: (ShippingAddress) -> Unit,
    val onShippingOptionDidChange: (ShippingOption) -> Unit,
) : AfterpayCheckoutV2Handler {

    private var onTokenLoaded: (Result<String>) -> Unit = {}

    /**
     * Initiates the checkout process.
     *
     * @param onTokenLoaded Callback invoked when token loading is complete.
     */
    override fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit) =
        onDidCommenceCheckout().also { this.onTokenLoaded = onTokenLoaded }

    /**
     * Provides the result of token loading.
     *
     * @param tokenResult The result of token loading.
     */
    fun provideTokenResult(tokenResult: Result<String>) =
        onTokenLoaded(tokenResult).also { onTokenLoaded = {} }

    private var onProvideShippingOptions: (ShippingOptionsResult) -> Unit = {}

    /**
     * Notifies changes in the shipping address and provides shipping options.
     *
     * @param address The updated shipping address.
     * @param onProvideShippingOptions Callback to provide shipping options.
     */
    override fun shippingAddressDidChange(
        address: ShippingAddress,
        onProvideShippingOptions: (ShippingOptionsResult) -> Unit,
    ) = onShippingAddressDidChange(address).also {
        this.onProvideShippingOptions = onProvideShippingOptions
    }

    /**
     * Provides the result of shipping options.
     *
     * @param shippingOptionsResult The result of shipping options.
     */
    fun provideShippingOptionsResult(shippingOptionsResult: ShippingOptionsResult) =
        onProvideShippingOptions(shippingOptionsResult).also { onProvideShippingOptions = {} }

    private var onProvideShippingOptionUpdate: (ShippingOptionUpdateResult?) -> Unit = {}

    /**
     * Notifies changes in the shipping option and provides updates.
     *
     * @param shippingOption The updated shipping option.
     * @param onProvideShippingOption Callback to provide shipping option update.
     */
    override fun shippingOptionDidChange(
        shippingOption: ShippingOption,
        onProvideShippingOption: (ShippingOptionUpdateResult?) -> Unit,
    ) = onShippingOptionDidChange(shippingOption).also {
        this.onProvideShippingOptionUpdate = onProvideShippingOption
    }

    /**
     * Provides the result of shipping option update.
     *
     * @param shippingOptionUpdateResult The result of shipping option update.
     */
    fun provideShippingOptionUpdateResult(shippingOptionUpdateResult: ShippingOptionUpdateResult?) =
        onProvideShippingOptionUpdate(shippingOptionUpdateResult).also {
            onProvideShippingOptionUpdate = {}
        }
}
