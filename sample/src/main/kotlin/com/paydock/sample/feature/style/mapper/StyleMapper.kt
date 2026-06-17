package com.paydock.sample.feature.style.mapper

import com.paydock.sample.feature.style.models.StyleAppearanceComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType

fun WidgetType.mapWidgetTypeToAppearanceComponents(): List<StyleAppearanceComponent> {
    return when (this) {
        WidgetType.ADDRESS_DETAILS -> listOf(
            StyleAppearanceComponent.PROPERTIES,
            StyleAppearanceComponent.TITLE,
            StyleAppearanceComponent.ADDRESS_TEXT_FIELD,
            StyleAppearanceComponent.COMPLETE_ACTION_BUTTON,
            StyleAppearanceComponent.LINK_BUTTON,
            StyleAppearanceComponent.SEARCH,
        )

        WidgetType.AFTER_PAY -> listOf(
            StyleAppearanceComponent.PROPERTIES,
            StyleAppearanceComponent.LOADER
        )

        WidgetType.CARD_DETAILS -> listOf(
            StyleAppearanceComponent.PROPERTIES,
            StyleAppearanceComponent.CARD_NAME_TEXT_FIELD,
            StyleAppearanceComponent.CARD_NUMBER_TEXT_FIELD,
            StyleAppearanceComponent.CARD_EXPIRY_TEXT_FIELD,
            StyleAppearanceComponent.CARD_SECURITY_TEXT_FIELD,
            StyleAppearanceComponent.COMPLETE_ACTION_BUTTON,
            StyleAppearanceComponent.TOGGLE,
            StyleAppearanceComponent.TOGGLE_TEXT, // Used for toggle text
            StyleAppearanceComponent.LINK_TEXT
        )

        WidgetType.COLES_PAY -> listOf(
            StyleAppearanceComponent.IMAGE_BUTTON,
            StyleAppearanceComponent.LOADER
        )

        WidgetType.CLICK_TO_PAY -> listOf(
            StyleAppearanceComponent.LOADER
        )

        WidgetType.GIFT_CARD -> listOf(
            StyleAppearanceComponent.PROPERTIES,
            StyleAppearanceComponent.GIFT_CARD_TEXT_FIELD,
            StyleAppearanceComponent.COMPLETE_ACTION_BUTTON,
        )

        WidgetType.GOOGLE_PAY -> listOf(
            StyleAppearanceComponent.PROPERTIES,
            StyleAppearanceComponent.LOADER
        )

        WidgetType.MPGS_3DS -> listOf(
            StyleAppearanceComponent.LOADER
        )

        WidgetType.PAY_PAL -> listOf(
            StyleAppearanceComponent.PROPERTIES,
            StyleAppearanceComponent.LOADER
        )

        WidgetType.PAY_PAL_VAULT -> listOf(
            StyleAppearanceComponent.COMPLETE_ACTION_BUTTON
        )

        WidgetType.STANDALONE_3DS -> listOf(
            StyleAppearanceComponent.OVERLAY_LOADER
        )

        WidgetType.ZIP -> listOf(
            StyleAppearanceComponent.PROPERTIES,
            StyleAppearanceComponent.LOADER
        )
    }
}

fun StyleAppearanceComponent.mapAppearanceComponentToSubComponents(): List<StyleAppearanceComponent>? {
    return when (this) {
        StyleAppearanceComponent.SEARCH -> listOf(
            StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD,
            StyleAppearanceComponent.DROP_DOWN
        )

        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD -> listOf(
            StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PROPERTIES,
            StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_LABEL,
            StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PLACEHOLDER,
            StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_ERROR_LABEL,
            StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_VALID_ICON,
        )

        StyleAppearanceComponent.LINK_BUTTON -> listOf(
            StyleAppearanceComponent.SUB_TEXT_BUTTON_PROPERTIES,
            StyleAppearanceComponent.SUB_LINK_BUTTON_TEXT
        )

        StyleAppearanceComponent.LINK_TEXT -> listOf(
            StyleAppearanceComponent.SUB_LINK_TEXT_PROPERTIES,
            StyleAppearanceComponent.SUB_LINK_TEXT
        )

        StyleAppearanceComponent.DEFAULT_ACTION_BUTTON -> listOf(
            StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES,
            StyleAppearanceComponent.SUB_BUTTON_TEXT,
            StyleAppearanceComponent.SUB_BUTTON_LOADER
        )

        StyleAppearanceComponent.COMPLETE_ACTION_BUTTON -> listOf(
            StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES,
            StyleAppearanceComponent.SUB_BUTTON_TEXT,
            StyleAppearanceComponent.SUB_BUTTON_LOADER,
            StyleAppearanceComponent.SUB_BUTTON_ICON
        )

        StyleAppearanceComponent.DROP_DOWN -> listOf(
            StyleAppearanceComponent.SUB_DROP_DOWN_PROPERTIES,
            StyleAppearanceComponent.SUB_DROPDOWN_ITEM
        )

        StyleAppearanceComponent.ADDRESS_TEXT_FIELD -> listOf(
            StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PROPERTIES,
            StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_LABEL,
            StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PLACEHOLDER,
            StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_ERROR_LABEL,
            StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_HINT_LABEL,
            StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_VALID_ICON,
        )

        StyleAppearanceComponent.GIFT_CARD_TEXT_FIELD -> listOf(
            StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PROPERTIES,
            StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_LABEL,
            StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PLACEHOLDER,
            StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_ERROR_LABEL,
            StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_HINT_LABEL,
            StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_VALID_ICON,
        )

        StyleAppearanceComponent.CARD_NAME_TEXT_FIELD -> listOf(
            StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PROPERTIES,
            StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_LABEL,
            StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PLACEHOLDER,
            StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_ERROR_LABEL,
            StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_HINT_LABEL,
            StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_VALID_ICON,
        )

        StyleAppearanceComponent.CARD_NUMBER_TEXT_FIELD -> listOf(
            StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PROPERTIES,
            StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_LABEL,
            StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PLACEHOLDER,
            StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_ERROR_LABEL,
            StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_HINT_LABEL,
            StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_VALID_ICON,
        )

        StyleAppearanceComponent.CARD_EXPIRY_TEXT_FIELD -> listOf(
            StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PROPERTIES,
            StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_LABEL,
            StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PLACEHOLDER,
            StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_ERROR_LABEL,
            StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_HINT_LABEL,
            StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_VALID_ICON,
        )

        StyleAppearanceComponent.CARD_SECURITY_TEXT_FIELD -> listOf(
            StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PROPERTIES,
            StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_LABEL,
            StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PLACEHOLDER,
            StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_ERROR_LABEL,
            StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_HINT_LABEL,
            StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_VALID_ICON,
        )

        StyleAppearanceComponent.OVERLAY_LOADER -> listOf(
            StyleAppearanceComponent.SUB_OVERLAY_LOADER_PROPERTIES,
            StyleAppearanceComponent.SUB_OVERLAY_LOADER_CARD,
            StyleAppearanceComponent.SUB_OVERLAY_LOADER_INDICATOR,
            StyleAppearanceComponent.SUB_OVERLAY_LOADER_TEXT,
        )

        else -> null // No sub-components
    }
}