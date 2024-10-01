package com.paydock.sample.feature.checkout.models

import com.paydock.sample.designsystems.components.list.DisplayableListItem

enum class CheckoutType : DisplayableListItem {
    STANDALONE;

    override fun displayName(): String = when (this) {
        STANDALONE -> "Standalone Widget"
    }

    override fun displayDescription(): String = when (this) {
        STANDALONE -> "Checkout demo built using standalone widgets"
    }
}