package com.paydock.core.presentation.util

import com.paydock.core.domain.model.Event

/**
 * Interface for managing widget event tracking.
 *
 * This interface defines a method to handle widget events such as button clicks,
 * toggle interactions, and link clicks. Implementations can use this to track
 * user interactions within widgets.
 */
interface WidgetEventDelegate {

    /**
     * Called when a widget event occurs.
     *
     * @param event The event that occurred, containing the event type and properties.
     */
    fun widgetEvent(event: Event)
}
