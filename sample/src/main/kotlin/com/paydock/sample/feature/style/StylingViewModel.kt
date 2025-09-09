package com.paydock.sample.feature.style

import androidx.lifecycle.ViewModel
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ImageButtonAppearance
import com.paydock.designsystems.components.icon.IconAppearance
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.loader.LoaderAppearance
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
import com.paydock.feature.paypal.vault.presentation.PayPalPaymentSourceWidgetAppearance
import com.paydock.feature.src.presentation.ClickToPayWidgetAppearance
import com.paydock.feature.threeDS.common.presentation.ui.ThreeDSWidgetAppearance
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
    private val _addressWidgetAppearance = MutableStateFlow<AddressDetailsWidgetAppearance?>(null)
    val addressWidgetAppearance: StateFlow<AddressDetailsWidgetAppearance?> = _addressWidgetAppearance.asStateFlow()

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

    private val _integrated3DSWidgetAppearance = MutableStateFlow<ThreeDSWidgetAppearance?>(null)
    val integrated3DSWidgetAppearance: StateFlow<ThreeDSWidgetAppearance?> =
        _integrated3DSWidgetAppearance.asStateFlow()

    private val _standalone3DSWidgetAppearance = MutableStateFlow<ThreeDSWidgetAppearance?>(null)
    val standalone3DSWidgetAppearance: StateFlow<ThreeDSWidgetAppearance?> =
        _standalone3DSWidgetAppearance.asStateFlow()

    // --- Initialization ---
    fun updateInitialAddressDefaults(updatedDefaults: AddressDetailsWidgetAppearance) {
        _addressWidgetAppearance.update { updatedDefaults }
    }
    fun updateInitialCardDetailsDefaults(updatedDefaults: CardDetailsWidgetAppearance) {
        _cardDetailsWidgetAppearance.update { updatedDefaults }
    }
    fun updateInitialGiftCardDetailsDefaults(updatedDefaults: GiftCardWidgetAppearance) {
        _giftCardWidgetAppearance.update { updatedDefaults }
    }
    fun updateInitialPayPalDefaults(updatedDefaults: PayPalWidgetAppearance) {
        _paypalWidgetAppearance.update { updatedDefaults }
    }
    fun updateInitialPayPalVaultDefaults(updatedDefaults: PayPalPaymentSourceWidgetAppearance) {
        _paypalVaultWidgetAppearance.update { updatedDefaults }
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
    fun updateInitialIntegrated3DSDefaults(updatedDefaults: ThreeDSWidgetAppearance) {
        _integrated3DSWidgetAppearance.update { updatedDefaults }
    }
    fun updateInitialStandalone3DSDefaults(updatedDefaults: ThreeDSWidgetAppearance) {
        _standalone3DSWidgetAppearance.update { updatedDefaults }
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

                        StyleAppearanceComponent.SUB_TEXT_FIELD_PROPERTIES -> current.copy(textField = newComponentAppearance as TextFieldAppearance)
                        StyleAppearanceComponent.SUB_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            textField = current.textField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )
                        StyleAppearanceComponent.SUB_TEXT_FIELD_LABEL -> current.copy(
                            textField = current.textField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )
                        StyleAppearanceComponent.SUB_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            textField = current.textField.copy(error = newComponentAppearance as TextAppearance)
                        )
                        StyleAppearanceComponent.SUB_TEXT_FIELD_VALID_ICON -> current.copy(
                            textField = current.textField.copy(
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
                                horizontalSpacing = newComponentAppearance.horizontalSpacing
                            )
                        }
                        StyleAppearanceComponent.TITLE -> current.copy(title = newComponentAppearance as TextAppearance)
                        StyleAppearanceComponent.TOGGLE_TEXT -> current.copy(toggleText = newComponentAppearance as TextAppearance)
                        StyleAppearanceComponent.SUB_TEXT_FIELD_PROPERTIES -> (newComponentAppearance as TextFieldAppearance).let {
                            current.copy(
                                textField = current.textField.copy(
                                    style = newComponentAppearance.style,
                                    singleLine = newComponentAppearance.singleLine,
                                    colors = newComponentAppearance.colors,
                                    shape = newComponentAppearance.shape
                                )
                            )
                        }
                        StyleAppearanceComponent.SUB_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            textField = current.textField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_TEXT_FIELD_LABEL -> current.copy(
                            textField = current.textField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            textField = current.textField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_TEXT_FIELD_VALID_ICON -> current.copy(
                            textField = current.textField.copy(
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
                            current.copy(switch = current.toggle.copy(
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
                            ))
                        }
                        StyleAppearanceComponent.SUB_LINK_TEXT -> current.copy(
                            linkText = current.linkText.copy(
                                newComponentAppearance as TextAppearance
                            )
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
                                horizontalSpacing = newComponentAppearance.horizontalSpacing
                            )
                        }

                        StyleAppearanceComponent.SUB_TEXT_FIELD_PROPERTIES -> (newComponentAppearance as TextFieldAppearance).let {
                            current.copy(
                                textField = current.textField.copy(
                                    style = newComponentAppearance.style,
                                    singleLine = newComponentAppearance.singleLine,
                                    colors = newComponentAppearance.colors,
                                    shape = newComponentAppearance.shape
                                )
                            )
                        }
                        StyleAppearanceComponent.SUB_TEXT_FIELD_PLACEHOLDER -> current.copy(
                            textField = current.textField.copy(placeholder = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_TEXT_FIELD_LABEL -> current.copy(
                            textField = current.textField.copy(
                                label = newComponentAppearance as TextAppearance
                            )
                        )

                        StyleAppearanceComponent.SUB_TEXT_FIELD_ERROR_LABEL -> current.copy(
                            textField = current.textField.copy(error = newComponentAppearance as TextAppearance)
                        )

                        StyleAppearanceComponent.SUB_TEXT_FIELD_VALID_ICON -> current.copy(
                            textField = current.textField.copy(
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
                                colorScheme = newComponentAppearance.colorScheme
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

            WidgetType.INTEGRATED_3DS -> {
                _integrated3DSWidgetAppearance.value?.let { current ->
                    val updatedAppearance = when (component) {
                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _integrated3DSWidgetAppearance.value = updatedAppearance
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
                        StyleAppearanceComponent.LOADER -> current.copy(loader = newComponentAppearance as LoaderAppearance)
                        else -> current
                    }
                    _standalone3DSWidgetAppearance.value = updatedAppearance
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
            StyleAppearanceComponent.SUB_BUTTON_ICON -> this.copy(iconAppearance = subAppearance as IconAppearance)
            StyleAppearanceComponent.SUB_BUTTON_LOADER -> this.copy(loaderAppearance = subAppearance as LoaderAppearance)
            else -> this
        }

        is ButtonAppearance.OutlineButtonAppearance -> when (component) {
            StyleAppearanceComponent.SUB_BUTTON_TEXT -> this.copy(textAppearance = subAppearance as TextAppearance)
            StyleAppearanceComponent.SUB_BUTTON_ICON -> this.copy(iconAppearance = subAppearance as IconAppearance)
            StyleAppearanceComponent.SUB_BUTTON_LOADER -> this.copy(loaderAppearance = subAppearance as LoaderAppearance)
            else -> this
        }

        is ButtonAppearance.TextButtonAppearance -> when (component) {
            StyleAppearanceComponent.SUB_LINK_BUTTON_TEXT,
            StyleAppearanceComponent.SUB_BUTTON_TEXT -> this.copy(textAppearance = subAppearance as TextAppearance)

            StyleAppearanceComponent.SUB_LINK_BUTTON_ICON,
            StyleAppearanceComponent.SUB_BUTTON_ICON -> this.copy(iconAppearance = subAppearance as IconAppearance)

            StyleAppearanceComponent.SUB_LINK_BUTTON_LOADER,
            StyleAppearanceComponent.SUB_BUTTON_LOADER -> this.copy(loaderAppearance = subAppearance as LoaderAppearance)
            else -> this
        }

        else -> this // Should not happen
    }
}