package com.paydock.sample.designsystems.components.list

// Define an interface for displayable items
interface DisplayableListItem {
    fun displayName(): String
    fun displayDescription(): String
}