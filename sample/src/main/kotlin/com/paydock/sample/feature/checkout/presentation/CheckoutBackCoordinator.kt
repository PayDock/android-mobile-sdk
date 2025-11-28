package com.paydock.sample.feature.checkout.presentation

object CheckoutBackCoordinator {
    // Return true if back was handled inside checkout (e.g., step back or loading blocks)
    var onToolbarBack: (() -> Boolean)? = null
}


