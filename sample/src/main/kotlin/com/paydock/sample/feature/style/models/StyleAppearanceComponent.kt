package com.paydock.sample.feature.style.models

enum class StyleAppearanceComponent(val hasSubComponents: Boolean = false) {
    // Text
    TITLE, // TextAppearance
    TOGGLE_TEXT, // TextAppearance
    SUB_LINK_TEXT, // LinkTextAppearance.TextAppearance
    SUB_DROPDOWN_ITEM, // DropdownAppearance.TextAppearance
    SUB_BUTTON_TEXT, // ButtonAppearance.TextAppearance
    SUB_LINK_BUTTON_TEXT, // LinkButtonAppearance.ButtonAppearance.TextAppearance
    SUB_TEXT_FIELD_PLACEHOLDER,  // TextFieldAppearance.TextAppearance
    SUB_TEXT_FIELD_LABEL, // TextFieldAppearance.TextAppearance
    SUB_TEXT_FIELD_ERROR_LABEL, // TextFieldAppearance.TextAppearance
    SUB_SEARCH_TEXT_FIELD_PLACEHOLDER,  // SearchDropDownAppearance.TextFieldAppearance.TextAppearance
    SUB_SEARCH_TEXT_FIELD_LABEL, // SearchDropDownAppearance.TextFieldAppearance.TextAppearance
    SUB_SEARCH_TEXT_FIELD_ERROR_LABEL, // SearchDropDownAppearance.TextFieldAppearance.TextAppearance

    // TextField
    TEXT_FIELD(hasSubComponents = true),  // TextFieldAppearance
    SUB_SEARCH_TEXT_FIELD(hasSubComponents = true),  // SearchDropDownAppearance.TextFieldAppearance

    // Search (TextField)
    SEARCH(hasSubComponents = true),  // SearchDropDownAppearance

    // Buttons
    DEFAULT_ACTION_BUTTON(hasSubComponents = true), // ButtonAppearance
    COMPLETE_ACTION_BUTTON(hasSubComponents = true), // ButtonAppearance

    // ImageButtonAppearance
    IMAGE_BUTTON,

    // DropdownAppearance
    DROP_DOWN(hasSubComponents = true),

    // ToggleAppearance
    TOGGLE,

    // LinkTextAppearance
    LINK_TEXT(hasSubComponents = true),

    // LinkButtonAppearance
    LINK_BUTTON(hasSubComponents = true), // LinkButtonAppearance.ButtonAppearance

    // Icon
    ICON, // IconAppearance
    SUB_TEXT_FIELD_VALID_ICON, // TextFieldAppearance.IconAppearance
    SUB_SEARCH_TEXT_FIELD_VALID_ICON, // SearchDropDownAppearance.TextFieldAppearance.IconAppearance
    SUB_BUTTON_ICON, // ButtonAppearance.IconAppearance
    SUB_LINK_BUTTON_ICON, // LinkButtonAppearance.ButtonAppearance.IconAppearance

    // Loader
    LOADER, // LoaderAppearance
    SUB_BUTTON_LOADER, // ButtonAppearance.LoaderAppearance
    SUB_LINK_BUTTON_LOADER, // LinkButtonAppearance.ButtonAppearance.LoaderAppearance

    // Component Properties
    PROPERTIES, // All other appearance components
    SUB_ACTION_BUTTON_PROPERTIES, // ButtonAppearance
    SUB_TEXT_BUTTON_PROPERTIES, // ButtonAppearance.TextButton
    SUB_DROP_DOWN_PROPERTIES, // DropdownAppearance
    SUB_TEXT_FIELD_PROPERTIES, // TextFieldAppearance
    SUB_SEARCH_TEXT_FIELD_PROPERTIES; // SearchDropDownAppearance.TextFieldAppearance

    fun displayName(): String = when (this) {
        TITLE -> "Title"
        TOGGLE_TEXT -> "Toggle Text"
        SUB_SEARCH_TEXT_FIELD_PLACEHOLDER,
        SUB_TEXT_FIELD_PLACEHOLDER -> "Placeholder Label"
        SUB_SEARCH_TEXT_FIELD_LABEL,
        SUB_TEXT_FIELD_LABEL -> "Floating Label"
        SUB_SEARCH_TEXT_FIELD_ERROR_LABEL,
        SUB_TEXT_FIELD_ERROR_LABEL -> "Error Label"
        SUB_DROPDOWN_ITEM -> "Dropdown Item"
        SUB_LINK_TEXT,
        SUB_LINK_BUTTON_TEXT,
        SUB_BUTTON_TEXT -> "Text"
        DROP_DOWN -> "Dropdown"
        SUB_SEARCH_TEXT_FIELD,
        TEXT_FIELD -> "Text Field"
        SEARCH -> "Search Dropdown"
        DEFAULT_ACTION_BUTTON,
        COMPLETE_ACTION_BUTTON -> "Action Button"
        IMAGE_BUTTON -> "Image Button"
        SUB_LINK_BUTTON_LOADER,
        SUB_BUTTON_LOADER,
        LOADER -> "Loader"
        TOGGLE -> "Toggle"
        LINK_TEXT -> "Link Text"
        LINK_BUTTON ->  "Link Button"
        SUB_LINK_BUTTON_ICON,
        ICON -> "Icon"
        SUB_SEARCH_TEXT_FIELD_VALID_ICON,
        SUB_TEXT_FIELD_VALID_ICON -> "Valid Icon"
        SUB_BUTTON_ICON -> "Button Icon"
        SUB_DROP_DOWN_PROPERTIES,
        SUB_SEARCH_TEXT_FIELD_PROPERTIES,
        SUB_TEXT_FIELD_PROPERTIES,
        SUB_ACTION_BUTTON_PROPERTIES,
        SUB_TEXT_BUTTON_PROPERTIES,
        PROPERTIES -> "Default Properties"
    }
}