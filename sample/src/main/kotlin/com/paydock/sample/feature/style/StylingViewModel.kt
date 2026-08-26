package com.paydock.sample.feature.style

import androidx.lifecycle.ViewModel
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ImageButtonAppearance
import com.paydock.designsystems.components.card.CardAppearance
import com.paydock.designsystems.components.icon.IconAppearance
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.link.LinkTextAppearance
import com.paydock.designsystems.components.loader.LoaderAppearance
import com.paydock.designsystems.components.loader.OverlayLoaderAppearance
import com.paydock.designsystems.components.search.DropdownAppearance
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.toggle.ToggleAppearance
import com.paydock.feature.address.presentation.AddressDetailsWidgetAppearance
import com.paydock.feature.afterpay.presentation.AfterpayWidgetAppearance
import com.paydock.feature.card.presentation.CardDetailsWidgetAppearance
import com.paydock.feature.card.presentation.GiftCardWidgetAppearance
import com.paydock.feature.colespay.presentation.ColesPayWidgetAppearance
import com.paydock.feature.googlepay.presentation.GooglePayWidgetAppearance
import com.paydock.feature.paypal.checkout.presentation.PayPalWidgetAppearance
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon
import com.paydock.feature.paypal.vault.presentation.PayPalPaymentSourceWidgetAppearance
import com.paydock.feature.src.presentation.ClickToPayWidgetAppearance
import com.paydock.feature.threeDS.integrated.presentation.ui.MPGSThreeDSWidgetAppearance
import com.paydock.feature.threeDS.standalone.presentation.ui.StandaloneThreeDSWidgetAppearance
import com.paydock.feature.zip.presentation.ZipWidgetAppearance
import com.paydock.sample.feature.style.models.StyleAppearanceComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StylingViewModel @Inject constructor() : ViewModel() {
    // Track which widgets have user customizations to preserve them on theme changes
    private val hasAddressCustomizations = MutableStateFlow(false)
    private val hasCardDetailsCustomizations = MutableStateFlow(false)
    private val hasGiftCardCustomizations = MutableStateFlow(false)
    private val hasPayPalVaultCustomizations = MutableStateFlow(false)

    private val _addressWidgetAppearance = MutableStateFlow<AddressDetailsWidgetAppearance?>(null)
    val addressWidgetAppearance: StateFlow<AddressDetailsWidgetAppearance?> =
        _addressWidgetAppearance.asStateFlow()

    private val _cardDetailsWidgetAppearance = MutableStateFlow<CardDetailsWidgetAppearance?>(null)
    val cardDetailsWidgetAppearance: StateFlow<CardDetailsWidgetAppearance?> =
        _cardDetailsWidgetAppearance.asStateFlow()

    private val _giftCardWidgetAppearance = MutableStateFlow<GiftCardWidgetAppearance?>(null)
    val giftCardWidgetAppearance: StateFlow<GiftCardWidgetAppearance?> =
        _giftCardWidgetAppearance.asStateFlow()

    private val _afterpayWidgetAppearance = MutableStateFlow<AfterpayWidgetAppearance?>(null)
    val afterpayWidgetAppearance: StateFlow<AfterpayWidgetAppearance?> =
        _afterpayWidgetAppearance.asStateFlow()

    private val _googlePayWidgetAppearance = MutableStateFlow<GooglePayWidgetAppearance?>(null)
    val googlePayWidgetAppearance: StateFlow<GooglePayWidgetAppearance?> =
        _googlePayWidgetAppearance.asStateFlow()

    private val _paypalWidgetAppearance = MutableStateFlow<PayPalWidgetAppearance?>(null)
    val paypalWidgetAppearance: StateFlow<PayPalWidgetAppearance?> =
        _paypalWidgetAppearance.asStateFlow()

    private val _paypalVaultWidgetAppearance =
        MutableStateFlow<PayPalPaymentSourceWidgetAppearance?>(null)
    val paypalVaultWidgetAppearance: StateFlow<PayPalPaymentSourceWidgetAppearance?> =
        _paypalVaultWidgetAppearance.asStateFlow()

    private val _colesPayWidgetAppearance = MutableStateFlow<ColesPayWidgetAppearance?>(null)
    val colesPayWidgetAppearance: StateFlow<ColesPayWidgetAppearance?> =
        _colesPayWidgetAppearance.asStateFlow()

    private val _clickToPayWidgetAppearance = MutableStateFlow<ClickToPayWidgetAppearance?>(null)
    val clickToPayWidgetAppearance: StateFlow<ClickToPayWidgetAppearance?> =
        _clickToPayWidgetAppearance.asStateFlow()

    private val _mpgs3dsWidgetAppearance = MutableStateFlow<MPGSThreeDSWidgetAppearance?>(null)
    val mpgs3dsWidgetAppearance: StateFlow<MPGSThreeDSWidgetAppearance?> =
        _mpgs3dsWidgetAppearance.asStateFlow()

    private val _standalone3DSWidgetAppearance = MutableStateFlow<StandaloneThreeDSWidgetAppearance?>(null)
    val standalone3DSWidgetAppearance: StateFlow<StandaloneThreeDSWidgetAppearance?> =
        _standalone3DSWidgetAppearance.asStateFlow()

    private val _zipWidgetAppearance = MutableStateFlow<ZipWidgetAppearance?>(null)
    val zipWidgetAppearance: StateFlow<ZipWidgetAppearance?> =
        _zipWidgetAppearance.asStateFlow()

    // --- Initialization ---
    fun updateInitialAddressDefaults(
        updatedDefaults: AddressDetailsWidgetAppearance,
        preserveCustomizations: Boolean = false
    ) {
        _addressWidgetAppearance.update { current ->
            if (preserveCustomizations && current != null && hasAddressCustomizations.value) {
                // Preserve user customizations (structural changes like icons, text, spacing)
                // but update theme-dependent properties (colors from defaults)
                updatedDefaults.copy(
                    title = current.title, // Preserve custom title text appearance
                    actionButton = current.actionButton, // Preserve custom button (icon, text)
                    linkButton = current.linkButton,
                    textField = current.textField, // Preserve custom text field appearance
                    searchDropdown = current.searchDropdown, // Preserve custom search dropdown appearance
                    verticalSpacing = current.verticalSpacing,
                    horizontalSpacing = current.horizontalSpacing,
                    textFieldVerticalSpacing = current.textFieldVerticalSpacing,
                    textFieldHorizontalSpacing = current.textFieldHorizontalSpacing
                )
            } else {
                updatedDefaults
            }
        }
    }

    fun updateInitialCardDetailsDefaults(
        updatedDefaults: CardDetailsWidgetAppearance,
        preserveCustomizations: Boolean = false
    ) {
        _cardDetailsWidgetAppearance.update { current ->
            if (preserveCustomizations && current != null && hasCardDetailsCustomizations.value) {
                // Preserve user customizations (text appearances, buttons, spacing)
                // but update theme-dependent properties (colors from defaults)
                updatedDefaults.copy(
                    actionButton = current.actionButton,
                    cardNameTextField = current.cardNameTextField, // Preserve custom text field appearance
                    cardNumberTextField = current.cardNumberTextField, // Preserve custom text field appearance
                    cardExpiryTextField = current.cardExpiryTextField, // Preserve custom text field appearance
                    cardSecurityCodeTextField = current.cardSecurityCodeTextField, // Preserve custom text field appearance
                    toggleText = current.toggleText, // Preserve custom toggle text appearance
                    linkText = current.linkText, // Preserve custom link text appearance
                    verticalSpacing = current.verticalSpacing,
                    horizontalSpacing = current.horizontalSpacing,
                    textFieldVerticalSpacing = current.textFieldVerticalSpacing,
                    textFieldHorizontalSpacing = current.textFieldHorizontalSpacing
                )
            } else {
                updatedDefaults
            }
        }
    }

    fun updateInitialGiftCardDetailsDefaults(
        updatedDefaults: GiftCardWidgetAppearance,
        preserveCustomizations: Boolean = false
    ) {
        _giftCardWidgetAppearance.update { current ->
            if (preserveCustomizations && current != null && hasGiftCardCustomizations.value) {
                // Preserve user customizations (text appearances, buttons, spacing)
                // but update theme-dependent properties (colors from defaults)
                updatedDefaults.copy(
                    actionButton = current.actionButton,
                    textField = current.textField, // Preserve custom text field appearance
                    verticalSpacing = current.verticalSpacing,
                    horizontalSpacing = current.horizontalSpacing,
                    textFieldVerticalSpacing = current.textFieldVerticalSpacing,
                    textFieldHorizontalSpacing = current.textFieldHorizontalSpacing
                )
            } else {
                updatedDefaults
            }
        }
    }

    fun updateInitialPayPalDefaults(updatedDefaults: PayPalWidgetAppearance) {
        _paypalWidgetAppearance.update { updatedDefaults }
    }

    fun updateInitialPayPalVaultDefaults(
        updatedDefaults: PayPalPaymentSourceWidgetAppearance,
        preserveCustomizations: Boolean = false
    ) {
        _paypalVaultWidgetAppearance.update { current ->
            if (preserveCustomizations && current != null && hasPayPalVaultCustomizations.value) {
                // Preserve user customizations (actionButton)
                updatedDefaults.copy(
                    actionButton = current.actionButton
                )
            } else {
                updatedDefaults
            }
        }
    }

    fun updateInitialAfterpayDefaults(updatedDefaults: AfterpayWidgetAppearance) {
        _afterpayWidgetAppearance.update { updatedDefaults }
    }

    fun updateInitialGooglePayDefaults(updatedDefaults: GooglePayWidgetAppearance) {
        _googlePayWidgetAppearance.update { updatedDefaults }
    }

    fun updateInitialColesPayDefaults(updatedDefaults: ColesPayWidgetAppearance) {
        _colesPayWidgetAppearance.update { updatedDefaults }
    }

    fun updateInitialClickToPayDefaults(updatedDefaults: ClickToPayWidgetAppearance) {
        _clickToPayWidgetAppearance.update { updatedDefaults }
    }

    fun updateInitialMPGS3dsDefaults(updatedDefaults: MPGSThreeDSWidgetAppearance) {
        _mpgs3dsWidgetAppearance.update { updatedDefaults }
    }

    fun updateInitialStandalone3DSDefaults(updatedDefaults: StandaloneThreeDSWidgetAppearance) {
        _standalone3DSWidgetAppearance.update { updatedDefaults }
    }

    fun updateInitialZipDefaults(updatedDefaults: ZipWidgetAppearance) {
        _zipWidgetAppearance.update { updatedDefaults }
    }

    // --- Update methods for specific appearances ---
    fun updateAddressTitleAppearance(newTitleAppearance: TextAppearance) {
        _addressWidgetAppearance.value?.let { current ->
            _addressWidgetAppearance.value = current.copy(title = newTitleAppearance)
        }
    }

    fun updateWidgetComponentAppearance(
        widgetType: WidgetType,
        component: StyleAppearanceComponent,
        newComponentAppearance: Any
    ) {
        // Mark that this widget has user customizations
        when (widgetType) {
            WidgetType.ADDRESS_DETAILS -> hasAddressCustomizations.value = true
            WidgetType.CARD_DETAILS -> hasCardDetailsCustomizations.value = true
            WidgetType.GIFT_CARD -> hasGiftCardCustomizations.value = true
            WidgetType.PAY_PAL_VAULT -> hasPayPalVaultCustomizations.value = true
            else -> { /* Other widgets don't have customization tracking yet */
            }
        }

        when (widgetType) {
            WidgetType.ADDRESS_DETAILS -> {
                _addressWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.TITLE -> current.copy(title = newComponentAppearance as TextAppearance)
                        StyleAppearanceComponent.PROPERTIES -> newComponentAppearance as AddressDetailsWidgetAppearance
                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PROPERTIES -> current.copy(
                            searchDropdown = current.searchDropdown.copy(textField = newComponentAppearance as TextFieldAppearance)
                        )

                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            searchDropdown = current.searchDropdown.copy(
                                current.searchDropdown.textField.copy(
                                    placeholder = newComponentAppearance as TextAppearance
                                )
                            )
                        )

                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_LABEL -> current.copy(
                            searchDropdown = current.searchDropdown.copy(
                                current.searchDropdown.textField.copy(
                                    label = newComponentAppearance as TextAppearance
                                )
                            )
                        )

                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            searchDropdown = current.searchDropdown.copy(
                                current.searchDropdown.textField.copy(
                                    error = newComponentAppearance as TextAppearance
                                )
                            )
                        )

                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_VALID_ICON -> current.copy(
                            searchDropdown = current.searchDropdown.copy(
                                current.searchDropdown.textField.copy(
                                    validIcon = newComponentAppearance as IconAppearance
                                )
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PROPERTIES -> current.copy(textField = newComponentAppearance as TextFieldAppearance)
                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            textField = current.textField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_LABEL -> current.copy(
                            textField = current.textField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            textField = current.textField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_VALID_ICON -> current.copy(
                            textField = current.textField.copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        // Per-field overrides: each writes into its own nullable field, seeding
                        // from the current override (or the base textField, if none yet) so a
                        // single sub-property edit doesn't lose the field's other customisations.
                        StyleAppearanceComponent.SUB_ADDRESS_FIRST_NAME_TEXT_FIELD_PROPERTIES -> current.copy(
                            firstNameTextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_FIRST_NAME_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            firstNameTextField = (current.firstNameTextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_FIRST_NAME_TEXT_FIELD_LABEL -> current.copy(
                            firstNameTextField = (current.firstNameTextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_FIRST_NAME_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            firstNameTextField = (current.firstNameTextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_FIRST_NAME_TEXT_FIELD_HINT_LABEL -> current.copy(
                            firstNameTextField = (current.firstNameTextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_FIRST_NAME_TEXT_FIELD_VALID_ICON -> current.copy(
                            firstNameTextField = (current.firstNameTextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_LAST_NAME_TEXT_FIELD_PROPERTIES -> current.copy(
                            lastNameTextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LAST_NAME_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            lastNameTextField = (current.lastNameTextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LAST_NAME_TEXT_FIELD_LABEL -> current.copy(
                            lastNameTextField = (current.lastNameTextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LAST_NAME_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            lastNameTextField = (current.lastNameTextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LAST_NAME_TEXT_FIELD_HINT_LABEL -> current.copy(
                            lastNameTextField = (current.lastNameTextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LAST_NAME_TEXT_FIELD_VALID_ICON -> current.copy(
                            lastNameTextField = (current.lastNameTextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_LINE1_TEXT_FIELD_PROPERTIES -> current.copy(
                            addressLine1TextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE1_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            addressLine1TextField = (current.addressLine1TextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE1_TEXT_FIELD_LABEL -> current.copy(
                            addressLine1TextField = (current.addressLine1TextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE1_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            addressLine1TextField = (current.addressLine1TextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE1_TEXT_FIELD_HINT_LABEL -> current.copy(
                            addressLine1TextField = (current.addressLine1TextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE1_TEXT_FIELD_VALID_ICON -> current.copy(
                            addressLine1TextField = (current.addressLine1TextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_LINE2_TEXT_FIELD_PROPERTIES -> current.copy(
                            addressLine2TextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE2_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            addressLine2TextField = (current.addressLine2TextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE2_TEXT_FIELD_LABEL -> current.copy(
                            addressLine2TextField = (current.addressLine2TextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE2_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            addressLine2TextField = (current.addressLine2TextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE2_TEXT_FIELD_HINT_LABEL -> current.copy(
                            addressLine2TextField = (current.addressLine2TextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_LINE2_TEXT_FIELD_VALID_ICON -> current.copy(
                            addressLine2TextField = (current.addressLine2TextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_CITY_TEXT_FIELD_PROPERTIES -> current.copy(
                            cityTextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_CITY_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            cityTextField = (current.cityTextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_CITY_TEXT_FIELD_LABEL -> current.copy(
                            cityTextField = (current.cityTextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_CITY_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            cityTextField = (current.cityTextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_CITY_TEXT_FIELD_HINT_LABEL -> current.copy(
                            cityTextField = (current.cityTextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_CITY_TEXT_FIELD_VALID_ICON -> current.copy(
                            cityTextField = (current.cityTextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_STATE_TEXT_FIELD_PROPERTIES -> current.copy(
                            stateTextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_STATE_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            stateTextField = (current.stateTextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_STATE_TEXT_FIELD_LABEL -> current.copy(
                            stateTextField = (current.stateTextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_STATE_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            stateTextField = (current.stateTextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_STATE_TEXT_FIELD_HINT_LABEL -> current.copy(
                            stateTextField = (current.stateTextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_STATE_TEXT_FIELD_VALID_ICON -> current.copy(
                            stateTextField = (current.stateTextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ADDRESS_POSTCODE_TEXT_FIELD_PROPERTIES -> current.copy(
                            postcodeTextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_POSTCODE_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            postcodeTextField = (current.postcodeTextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_POSTCODE_TEXT_FIELD_LABEL -> current.copy(
                            postcodeTextField = (current.postcodeTextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_POSTCODE_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            postcodeTextField = (current.postcodeTextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_POSTCODE_TEXT_FIELD_HINT_LABEL -> current.copy(
                            postcodeTextField = (current.postcodeTextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_ADDRESS_POSTCODE_TEXT_FIELD_VALID_ICON -> current.copy(
                            postcodeTextField = (current.postcodeTextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> {
                            current.copy(actionButton = newComponentAppearance as ButtonAppearance)
                        }

                        StyleAppearanceComponent.SUB_BUTTON_TEXT,
                        StyleAppearanceComponent.SUB_BUTTON_ICON,
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> {
                            current.copy(
                                actionButton = current.actionButton.copyWithSubAppearance(
                                    component,
                                    newComponentAppearance
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_TEXT_BUTTON_PROPERTIES -> {
                            current.copy(linkButton = current.linkButton.copy(actionButton = newComponentAppearance as ButtonAppearance.TextButtonAppearance))
                        }

                        StyleAppearanceComponent.SUB_LINK_BUTTON_TEXT,
                        StyleAppearanceComponent.SUB_LINK_BUTTON_ICON,
                        StyleAppearanceComponent.SUB_LINK_BUTTON_LOADER -> {
                            current.copy(
                                linkButton = current.linkButton.copy(
                                    actionButton = current.linkButton.actionButton.copyWithSubAppearance(
                                        component,
                                        newComponentAppearance
                                    ) as ButtonAppearance.TextButtonAppearance
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_DROPDOWN_ITEM -> current.copy(
                            searchDropdown = current.searchDropdown.copy(
                                dropdown = current.searchDropdown.dropdown.copy(
                                    item = newComponentAppearance as TextAppearance
                                )
                            )
                        )

                        StyleAppearanceComponent.SUB_DROP_DOWN_PROPERTIES -> (newComponentAppearance as DropdownAppearance).let {
                            current.copy(
                                searchDropdown = current.searchDropdown.copy(
                                    dropdown = current.searchDropdown.dropdown.copy(
                                        shape = newComponentAppearance.shape,
                                        itemHeight = newComponentAppearance.itemHeight,
                                        itemPadding = newComponentAppearance.itemPadding
                                    )
                                )
                            )
                        }

                        else -> current
                    }
                    _addressWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.CARD_DETAILS -> {
                _cardDetailsWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.PROPERTIES -> (newComponentAppearance as CardDetailsWidgetAppearance).let {
                            current.copy(
                                verticalSpacing = newComponentAppearance.verticalSpacing,
                                horizontalSpacing = newComponentAppearance.horizontalSpacing,
                                textFieldVerticalSpacing = newComponentAppearance.textFieldVerticalSpacing,
                                textFieldHorizontalSpacing = newComponentAppearance.textFieldHorizontalSpacing
                            )
                        }

                        StyleAppearanceComponent.TOGGLE_TEXT -> current.copy(toggleText = newComponentAppearance as TextAppearance)

                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PROPERTIES -> (newComponentAppearance as TextFieldAppearance).let {
                            current.copy(
                                cardNameTextField = current.cardNameTextField.copy(
                                    style = newComponentAppearance.style,
                                    singleLine = newComponentAppearance.singleLine,
                                    colors = newComponentAppearance.colors,
                                    shape = newComponentAppearance.shape,
                                    topMessageSpacing = newComponentAppearance.topMessageSpacing,
                                    startMessageSpacing = newComponentAppearance.startMessageSpacing,
                                    placeholderText = newComponentAppearance.placeholderText,
                                    hintText = newComponentAppearance.hintText,
                                    hintDescription = newComponentAppearance.hintDescription,
                                    clickableDescription = newComponentAppearance.clickableDescription
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PROPERTIES -> (newComponentAppearance as TextFieldAppearance).let {
                            current.copy(
                                cardNumberTextField = current.cardNumberTextField.copy(
                                    style = newComponentAppearance.style,
                                    singleLine = newComponentAppearance.singleLine,
                                    colors = newComponentAppearance.colors,
                                    shape = newComponentAppearance.shape,
                                    topMessageSpacing = newComponentAppearance.topMessageSpacing,
                                    startMessageSpacing = newComponentAppearance.startMessageSpacing,
                                    placeholderText = newComponentAppearance.placeholderText,
                                    hintText = newComponentAppearance.hintText,
                                    hintDescription = newComponentAppearance.hintDescription,
                                    clickableDescription = newComponentAppearance.clickableDescription
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PROPERTIES -> (newComponentAppearance as TextFieldAppearance).let {
                            current.copy(
                                cardExpiryTextField = current.cardExpiryTextField.copy(
                                    style = newComponentAppearance.style,
                                    singleLine = newComponentAppearance.singleLine,
                                    colors = newComponentAppearance.colors,
                                    shape = newComponentAppearance.shape,
                                    topMessageSpacing = newComponentAppearance.topMessageSpacing,
                                    startMessageSpacing = newComponentAppearance.startMessageSpacing,
                                    placeholderText = newComponentAppearance.placeholderText,
                                    hintText = newComponentAppearance.hintText,
                                    hintDescription = newComponentAppearance.hintDescription,
                                    clickableDescription = newComponentAppearance.clickableDescription
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PROPERTIES -> (newComponentAppearance as TextFieldAppearance).let {
                            current.copy(
                                cardSecurityCodeTextField = current.cardSecurityCodeTextField.copy(
                                    style = newComponentAppearance.style,
                                    singleLine = newComponentAppearance.singleLine,
                                    colors = newComponentAppearance.colors,
                                    shape = newComponentAppearance.shape,
                                    topMessageSpacing = newComponentAppearance.topMessageSpacing,
                                    startMessageSpacing = newComponentAppearance.startMessageSpacing,
                                    placeholderText = newComponentAppearance.placeholderText,
                                    hintText = newComponentAppearance.hintText,
                                    hintDescription = newComponentAppearance.hintDescription,
                                    clickableDescription = newComponentAppearance.clickableDescription
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            cardNameTextField = current.cardNameTextField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_LABEL -> current.copy(
                            cardNameTextField = current.cardNameTextField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            cardNameTextField = current.cardNameTextField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_HINT_LABEL -> current.copy(
                            cardNameTextField = current.cardNameTextField.copy(hintLabel = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_VALID_ICON -> current.copy(
                            cardNameTextField = current.cardNameTextField.copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            cardNumberTextField = current.cardNumberTextField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_LABEL -> current.copy(
                            cardNumberTextField = current.cardNumberTextField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            cardNumberTextField = current.cardNumberTextField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_HINT_LABEL -> current.copy(
                            cardNumberTextField = current.cardNumberTextField.copy(hintLabel = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_VALID_ICON -> current.copy(
                            cardNumberTextField = current.cardNumberTextField.copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            cardExpiryTextField = current.cardExpiryTextField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_LABEL -> current.copy(
                            cardExpiryTextField = current.cardExpiryTextField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            cardExpiryTextField = current.cardExpiryTextField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_HINT_LABEL -> current.copy(
                            cardExpiryTextField = current.cardExpiryTextField.copy(hintLabel = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_VALID_ICON -> current.copy(
                            cardExpiryTextField = current.cardExpiryTextField.copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            cardSecurityCodeTextField = current.cardSecurityCodeTextField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_LABEL -> current.copy(
                            cardSecurityCodeTextField = current.cardSecurityCodeTextField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            cardSecurityCodeTextField = current.cardSecurityCodeTextField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_HINT_LABEL -> current.copy(
                            cardSecurityCodeTextField = current.cardSecurityCodeTextField.copy(hintLabel = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_VALID_ICON -> current.copy(
                            cardSecurityCodeTextField = current.cardSecurityCodeTextField.copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> {
                            current.copy(actionButton = newComponentAppearance as ButtonAppearance)
                        }

                        StyleAppearanceComponent.SUB_BUTTON_TEXT,
                        StyleAppearanceComponent.SUB_BUTTON_ICON,
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> {
                            current.copy(
                                actionButton = current.actionButton.copyWithSubAppearance(
                                    component,
                                    newComponentAppearance
                                )
                            )
                        }

                        StyleAppearanceComponent.TOGGLE -> (newComponentAppearance as ToggleAppearance).let {
                            current.copy(
                                switch = current.toggle.copy(
                                    colors = newComponentAppearance.colors.copy(
                                        checkedThumbColor = newComponentAppearance.colors.checkedThumbColor,
                                        checkedIconColor = newComponentAppearance.colors.checkedIconColor,
                                        checkedTrackColor = newComponentAppearance.colors.checkedTrackColor,
                                        checkedBorderColor = newComponentAppearance.colors.checkedBorderColor,
                                        uncheckedThumbColor = newComponentAppearance.colors.uncheckedThumbColor,
                                        uncheckedIconColor = newComponentAppearance.colors.uncheckedIconColor,
                                        uncheckedTrackColor = newComponentAppearance.colors.uncheckedTrackColor,
                                        uncheckedBorderColor = newComponentAppearance.colors.uncheckedBorderColor,
                                    )
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_LINK_TEXT -> current.copy(
                            linkText = current.linkText.copy(
                                textAppearance = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_LINK_TEXT_PROPERTIES -> current.copy(
                            linkText = newComponentAppearance as LinkTextAppearance
                        )

                        else -> current
                    }
                    _cardDetailsWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.GIFT_CARD -> {
                _giftCardWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.PROPERTIES -> (newComponentAppearance as GiftCardWidgetAppearance).let {
                            current.copy(
                                verticalSpacing = newComponentAppearance.verticalSpacing,
                                horizontalSpacing = newComponentAppearance.horizontalSpacing,
                                textFieldVerticalSpacing = newComponentAppearance.textFieldVerticalSpacing,
                                textFieldHorizontalSpacing = newComponentAppearance.textFieldHorizontalSpacing
                            )
                        }

                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PROPERTIES -> (newComponentAppearance as TextFieldAppearance).let {
                            current.copy(
                                textField = current.textField.copy(
                                    style = newComponentAppearance.style,
                                    singleLine = newComponentAppearance.singleLine,
                                    colors = newComponentAppearance.colors,
                                    shape = newComponentAppearance.shape,
                                    topMessageSpacing = newComponentAppearance.topMessageSpacing,
                                    startMessageSpacing = newComponentAppearance.startMessageSpacing,
                                    placeholderText = newComponentAppearance.placeholderText,
                                    hintText = newComponentAppearance.hintText,
                                    hintDescription = newComponentAppearance.hintDescription,
                                    clickableDescription = newComponentAppearance.clickableDescription
                                )
                            )
                        }

                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            textField = current.textField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_LABEL -> current.copy(
                            textField = current.textField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            textField = current.textField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_HINT_LABEL -> current.copy(
                            textField = current.textField.copy(hintLabel = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_VALID_ICON -> current.copy(
                            textField = current.textField.copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        // Per-field overrides: each writes into its own nullable field, seeding
                        // from the current override (or the base textField, if none yet) so a
                        // single sub-property edit doesn't lose the field's other customisations.
                        StyleAppearanceComponent.SUB_GIFT_CARD_NUMBER_TEXT_FIELD_PROPERTIES -> current.copy(
                            cardNumberTextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_NUMBER_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            cardNumberTextField = (current.cardNumberTextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_NUMBER_TEXT_FIELD_LABEL -> current.copy(
                            cardNumberTextField = (current.cardNumberTextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_NUMBER_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            cardNumberTextField = (current.cardNumberTextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_NUMBER_TEXT_FIELD_HINT_LABEL -> current.copy(
                            cardNumberTextField = (current.cardNumberTextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_NUMBER_TEXT_FIELD_VALID_ICON -> current.copy(
                            cardNumberTextField = (current.cardNumberTextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_GIFT_CARD_PIN_TEXT_FIELD_PROPERTIES -> current.copy(
                            pinTextField = newComponentAppearance as TextFieldAppearance
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_PIN_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            pinTextField = (current.pinTextField ?: current.textField).copy(
                                placeholder = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_PIN_TEXT_FIELD_LABEL -> current.copy(
                            pinTextField = (current.pinTextField ?: current.textField).copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_PIN_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            pinTextField = (current.pinTextField ?: current.textField).copy(
                                error = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_PIN_TEXT_FIELD_HINT_LABEL -> current.copy(
                            pinTextField = (current.pinTextField ?: current.textField).copy(
                                hintLabel = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_GIFT_CARD_PIN_TEXT_FIELD_VALID_ICON -> current.copy(
                            pinTextField = (current.pinTextField ?: current.textField).copy(
                                validIcon = newComponentAppearance as IconAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> {
                            current.copy(actionButton = newComponentAppearance as ButtonAppearance)
                        }

                        StyleAppearanceComponent.SUB_BUTTON_TEXT,
                        StyleAppearanceComponent.SUB_BUTTON_ICON,
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> {
                            current.copy(
                                actionButton = current.actionButton.copyWithSubAppearance(
                                    component,
                                    newComponentAppearance
                                )
                            )
                        }

                        else -> current
                    }
                    _giftCardWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.AFTER_PAY -> {
                _afterpayWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.PROPERTIES -> (newComponentAppearance as AfterpayWidgetAppearance).let {
                            current.copy(
                                buttonText = newComponentAppearance.buttonText,
                                style = newComponentAppearance.style
                            )
                        }

                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _afterpayWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.COLES_PAY -> {
                _colesPayWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.IMAGE_BUTTON -> current.copy(imageButton = newComponentAppearance as ImageButtonAppearance)
                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _colesPayWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.GOOGLE_PAY -> {
                _googlePayWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.PROPERTIES -> (newComponentAppearance as GooglePayWidgetAppearance).let {
                            current.copy(
                                type = newComponentAppearance.type,
                                cornerRadius = newComponentAppearance.cornerRadius
                            )
                        }

                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _googlePayWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.MPGS_3DS -> {
                _mpgs3dsWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _mpgs3dsWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.CLICK_TO_PAY -> {
                _clickToPayWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _clickToPayWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.PAY_PAL -> {
                _paypalWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.PROPERTIES -> (newComponentAppearance as PayPalWidgetAppearance)
                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _paypalWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.PAY_PAL_VAULT -> {
                _paypalVaultWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> {
                            current.copy(actionButton = newComponentAppearance as ButtonAppearance)
                        }

                        StyleAppearanceComponent.SUB_BUTTON_TEXT,
                        StyleAppearanceComponent.SUB_BUTTON_ICON,
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> {
                            current.copy(
                                actionButton = current.actionButton.copyWithSubAppearance(
                                    component,
                                    newComponentAppearance
                                )
                            )
                        }

                        else -> current
                    }
                    _paypalVaultWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.STANDALONE_3DS -> {
                _standalone3DSWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_PROPERTIES -> current.copy(loader = newComponentAppearance as OverlayLoaderAppearance)
                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_CARD -> current.copy(
                            loader = current.loader.copy(
                                cardAppearance = newComponentAppearance as CardAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_INDICATOR -> current.copy(
                            loader = current.loader.copy(
                                loaderAppearance = newComponentAppearance as LoaderAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_TEXT -> current.copy(
                            loader = current.loader.copy(
                                loaderTextAppearance = newComponentAppearance as TextAppearance
                            )
                        )

                        else -> current
                    }
                    _standalone3DSWidgetAppearance.value = updatedAppearance
                }
            }

            WidgetType.ZIP -> {
                _zipWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.PROPERTIES -> (newComponentAppearance as ZipWidgetAppearance).let {
                            current.copy(
                                buttonStyle = newComponentAppearance.buttonStyle
                            )
                        }

                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _zipWidgetAppearance.value = updatedAppearance
                }
            }
        }
    }
}

private fun ButtonAppearance.copyWithSubAppearance(
    component: StyleAppearanceComponent,
    subAppearance: Any
): ButtonAppearance {
    return when (this) {
        is ButtonAppearance.FilledButtonAppearance -> when (component) {
            StyleAppearanceComponent.SUB_BUTTON_TEXT -> this.copy(textAppearance = subAppearance as TextAppearance)
            StyleAppearanceComponent.SUB_BUTTON_ICON -> when (subAppearance) {
                is IconAppearance -> this.copy(iconAppearance = subAppearance)
                is ButtonIcon? -> this.copy(icon = subAppearance as ButtonIcon?)
                else -> this
            }

            StyleAppearanceComponent.SUB_BUTTON_LOADER -> this.copy(loaderAppearance = subAppearance as LoaderAppearance)
            else -> this
        }

        is ButtonAppearance.OutlineButtonAppearance -> when (component) {
            StyleAppearanceComponent.SUB_BUTTON_TEXT -> this.copy(textAppearance = subAppearance as TextAppearance)
            StyleAppearanceComponent.SUB_BUTTON_ICON -> when (subAppearance) {
                is IconAppearance -> this.copy(iconAppearance = subAppearance)
                is ButtonIcon? -> this.copy(icon = subAppearance as ButtonIcon?)
                else -> this
            }

            StyleAppearanceComponent.SUB_BUTTON_LOADER -> this.copy(loaderAppearance = subAppearance as LoaderAppearance)
            else -> this
        }

        is ButtonAppearance.TextButtonAppearance -> when (component) {
            StyleAppearanceComponent.SUB_LINK_BUTTON_TEXT,
            StyleAppearanceComponent.SUB_BUTTON_TEXT -> this.copy(textAppearance = subAppearance as TextAppearance)

            StyleAppearanceComponent.SUB_LINK_BUTTON_ICON,
            StyleAppearanceComponent.SUB_BUTTON_ICON -> when (subAppearance) {
                is IconAppearance -> this.copy(iconAppearance = subAppearance)
                is ButtonIcon? -> this.copy(icon = subAppearance as ButtonIcon?)
                else -> this
            }

            StyleAppearanceComponent.SUB_LINK_BUTTON_LOADER,
            StyleAppearanceComponent.SUB_BUTTON_LOADER -> this.copy(loaderAppearance = subAppearance as LoaderAppearance)

            else -> this
        }

        else -> this // Should not happen
    }
}