package com.paydock.core.domain.model

/**
 * Sealed class representing different types of widget events.
 *
 * Each event type contains specific properties relevant to that event.
 */
sealed class Event(
    open val type: String,
    open val properties: Map<String, Any>
) {

    /**
     * Represents a button event.
     *
     * @property name The name of the button (e.g., "TokenisationButton")
     * @property action The action performed on the button
     * @property text The optional text displayed on the button
     */
    data class ButtonEvent(
        val name: String,
        val action: EventAction,
        val text: String? = null
    ) : Event(
        type = "Button",
        properties = buildMap {
            put("name", name)
            put("action", action.value)
            text?.let { put("text", it) }
        }
    )

    /**
     * Represents a toggle event.
     *
     * @property name The name of the toggle (e.g., "SaveCardToggle")
     * @property action The action performed on the toggle
     * @property state The current state of the toggle (true/false)
     */
    data class ToggleEvent(
        val name: String,
        val action: EventAction,
        val state: Boolean
    ) : Event(
        type = "Toggle",
        properties = mapOf(
            "name" to name,
            "action" to action.value,
            "state" to state
        )
    )

    /**
     * Represents a link text event.
     *
     * @property name The name of the link (e.g., "PrivacyPolicyLink")
     * @property action The action performed on the link
     * @property url The URL associated with the link
     */
    data class LinkTextEvent(
        val name: String,
        val action: EventAction,
        val url: String
    ) : Event(
        type = "LinkText",
        properties = mapOf(
            "name" to name,
            "action" to action.value,
            "url" to url
        )
    )
}

/**
 * Enum representing possible actions for widget events.
 */
enum class EventAction(val value: String) {
    CLICK("click")
}
