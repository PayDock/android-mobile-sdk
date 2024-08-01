package com.paydock.sample.feature.checkout.models

import com.paydock.sample.designsystems.components.list.DisplayableListItem

enum class CheckoutType : DisplayableListItem {
    STANDALONE, PAYMENT_WORKFLOW;

    override fun displayName(): String = when (this) {
        STANDALONE -> "Standalone Widget"
        PAYMENT_WORKFLOW -> "Payment Workflow"
    }

    override fun displayDescription(): String = when (this) {
        STANDALONE -> "Checkout demo built using standalone widgets"
        PAYMENT_WORKFLOW -> "Checkout demo built using out of the box checkout solution"
    }
}