package com.paydock.sample.feature.style.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.paydock.feature.googlepay.presentation.GooglePayWidgetAppearance
import com.paydock.feature.paypal.checkout.presentation.PayPalWidgetAppearance
import com.paydock.feature.zip.presentation.ZipWidgetAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.button.AppButton
import com.paydock.sample.designsystems.components.button.AppButtonShape
import com.paydock.sample.designsystems.components.button.AppButtonVariant
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.style.models.StyleAppearanceComponent
import com.paydock.sample.feature.style.ui.components.section.ButtonAppearanceStyleEditor
import com.paydock.sample.feature.style.ui.components.section.StyleAddressDetailsMiscSection
import com.paydock.sample.feature.style.ui.components.section.StyleAfterpayMiscSection
import com.paydock.sample.feature.style.ui.components.section.StyleCardDetailsMiscSection
import com.paydock.sample.feature.style.ui.components.section.StyleCardSection
import com.paydock.sample.feature.style.ui.components.section.StyleDropdownMiscSection
import com.paydock.sample.feature.style.ui.components.section.StyleGiftCardMiscSection
import com.paydock.sample.feature.style.ui.components.section.StyleGooglePayMiscSection
import com.paydock.sample.feature.style.ui.components.section.StyleIconSection
import com.paydock.sample.feature.style.ui.components.section.StyleImageButtonSection
import com.paydock.sample.feature.style.ui.components.section.StyleLinkTextSection
import com.paydock.sample.feature.style.ui.components.section.StyleLoaderSection
import com.paydock.sample.feature.style.ui.components.section.StyleOverlayLoaderMiscSection
import com.paydock.sample.feature.style.ui.components.section.StylePayPalMiscSection
import com.paydock.sample.feature.style.ui.components.section.StyleTextFieldSection
import com.paydock.sample.feature.style.ui.components.section.StyleTextSection
import com.paydock.sample.feature.style.ui.components.section.StyleToggleSection
import com.paydock.sample.feature.style.ui.components.section.StyleZipMiscSection
import com.paydock.sample.feature.style.ui.components.section.TextButtonAppearanceStyleEditor
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun StylePropertiesScreen(
    widgetContext: WidgetType,
    styleItemName: StyleAppearanceComponent,
    stylingViewModel: StylingViewModel
) {
    val relevantAppearanceState: State<Any?> =
        getAppearanceForWidgetComponent(widgetContext, styleItemName, stylingViewModel)
    val componentAppearance = relevantAppearanceState.value

    val initialAppearanceValue = remember(widgetContext, styleItemName) {
        componentAppearance
    }

    val hasChanges = componentAppearance != initialAppearanceValue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("style_properties_screen")
    ) {
        if (componentAppearance == null) {
            Text(
                text = "Appearance data not available for $styleItemName in $widgetContext. Ensure ViewModel is initialized.",
                modifier = Modifier.testTag("style_properties_error_message")
            )
        } else {
            when (styleItemName) {
                StyleAppearanceComponent.TITLE,
                StyleAppearanceComponent.TOGGLE_TEXT,
                StyleAppearanceComponent.SUB_LINK_TEXT,
                StyleAppearanceComponent.SUB_DROPDOWN_ITEM,
                StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PLACEHOLDER,
                StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_LABEL,
                StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_ERROR_LABEL,
                StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PLACEHOLDER,
                StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_LABEL,
                StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_ERROR_LABEL,
                StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_HINT_LABEL,
                StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PLACEHOLDER,
                StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_LABEL,
                StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_ERROR_LABEL,
                StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_HINT_LABEL,
                StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PLACEHOLDER,
                StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_LABEL,
                StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_ERROR_LABEL,
                StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_HINT_LABEL,
                StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PLACEHOLDER,
                StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_LABEL,
                StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_ERROR_LABEL,
                StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_HINT_LABEL,
                StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PLACEHOLDER,
                StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_LABEL,
                StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_ERROR_LABEL,
                StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_HINT_LABEL,
                StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PLACEHOLDER,
                StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_LABEL,
                StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_ERROR_LABEL,
                StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_HINT_LABEL,
                StyleAppearanceComponent.SUB_BUTTON_TEXT,
                StyleAppearanceComponent.SUB_LINK_BUTTON_TEXT,
                StyleAppearanceComponent.SUB_OVERLAY_LOADER_TEXT,
                    -> {
                    (componentAppearance as? TextAppearance)?.let { currentTitleAppearance ->
                        StyleTextSection(
                            currentAppearance = currentTitleAppearance,
                            onAppearanceChange = { newTitleAppearance: TextAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newTitleAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.LOADER,
                StyleAppearanceComponent.SUB_BUTTON_LOADER,
                StyleAppearanceComponent.SUB_LINK_BUTTON_LOADER,
                StyleAppearanceComponent.SUB_OVERLAY_LOADER_INDICATOR -> {
                    (componentAppearance as? LoaderAppearance)?.let { currentLoaderAppearance ->
                        StyleLoaderSection(
                            currentAppearance = currentLoaderAppearance,
                            onAppearanceChange = { newLoaderAppearance: LoaderAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newLoaderAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.IMAGE_BUTTON -> {
                    (componentAppearance as? ImageButtonAppearance)?.let { currentImageButtonAppearance ->
                        StyleImageButtonSection(
                            currentAppearance = currentImageButtonAppearance,
                            onAppearanceChange = { newImageButtonAppearance: ImageButtonAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newImageButtonAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.TOGGLE -> {
                    (componentAppearance as? ToggleAppearance)?.let { currentToggleAppearance ->
                        StyleToggleSection(
                            currentAppearance = currentToggleAppearance,
                            onAppearanceChange = { newToggleAppearance: ToggleAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newToggleAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.ICON,
                StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_VALID_ICON,
                StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_VALID_ICON,
                StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_VALID_ICON,
                StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_VALID_ICON,
                StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_VALID_ICON,
                StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_VALID_ICON,
                StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_VALID_ICON,
                StyleAppearanceComponent.SUB_BUTTON_ICON,
                StyleAppearanceComponent.SUB_LINK_BUTTON_ICON -> {
                    (componentAppearance as? IconAppearance)?.let { currentIconAppearance ->
                        StyleIconSection(
                            currentAppearance = currentIconAppearance,
                            onAppearanceChange = { newIconAppearance: IconAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newIconAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.PROPERTIES -> {
                    when (widgetContext) {
                        WidgetType.AFTER_PAY -> {
                            (componentAppearance as? AfterpayWidgetAppearance)?.let { currentAppearance ->
                                StyleAfterpayMiscSection(
                                    currentAppearance = currentAppearance,
                                    onAppearanceChange = { newAppearance: AfterpayWidgetAppearance ->
                                        stylingViewModel.updateWidgetComponentAppearance(
                                            widgetContext,
                                            styleItemName,
                                            newAppearance
                                        )
                                    }
                                )
                            }
                                ?: Text("$styleItemName appearance not available for $widgetContext")
                        }

                        WidgetType.GOOGLE_PAY -> {
                            (componentAppearance as? GooglePayWidgetAppearance)?.let { currentAppearance ->
                                StyleGooglePayMiscSection(
                                    currentAppearance = currentAppearance,
                                    onAppearanceChange = { newAppearance: GooglePayWidgetAppearance ->
                                        stylingViewModel.updateWidgetComponentAppearance(
                                            widgetContext,
                                            styleItemName,
                                            newAppearance
                                        )
                                    }
                                )
                            }
                                ?: Text("$styleItemName appearance not available for $widgetContext")
                        }

                        WidgetType.PAY_PAL -> {
                            (componentAppearance as? PayPalWidgetAppearance)?.let { currentAppearance ->
                                StylePayPalMiscSection(
                                    currentAppearance = currentAppearance,
                                    onAppearanceChange = { newAppearance ->
                                        stylingViewModel.updateWidgetComponentAppearance(
                                            widgetContext,
                                            styleItemName,
                                            newAppearance
                                        )
                                    }
                                )
                            }
                                ?: Text("$styleItemName appearance not available for $widgetContext")
                        }

                        WidgetType.ADDRESS_DETAILS -> {
                            (componentAppearance as? AddressDetailsWidgetAppearance)?.let { currentAppearance ->
                                StyleAddressDetailsMiscSection(
                                    currentAppearance = currentAppearance,
                                    onAppearanceChange = { newAppearance: AddressDetailsWidgetAppearance ->
                                        stylingViewModel.updateWidgetComponentAppearance(
                                            widgetContext,
                                            styleItemName,
                                            newAppearance
                                        )
                                    }
                                )
                            }
                                ?: Text("$styleItemName appearance not available for $widgetContext")
                        }

                        WidgetType.CARD_DETAILS -> {
                            (componentAppearance as? CardDetailsWidgetAppearance)?.let { currentAppearance ->
                                StyleCardDetailsMiscSection(
                                    currentAppearance = currentAppearance,
                                    onAppearanceChange = { newAppearance: CardDetailsWidgetAppearance ->
                                        stylingViewModel.updateWidgetComponentAppearance(
                                            widgetContext,
                                            styleItemName,
                                            newAppearance
                                        )
                                    }
                                )
                            }
                                ?: Text("$styleItemName appearance not available for $widgetContext")
                        }

                        WidgetType.GIFT_CARD -> {
                            (componentAppearance as? GiftCardWidgetAppearance)?.let { currentAppearance ->
                                StyleGiftCardMiscSection(
                                    currentAppearance = currentAppearance,
                                    onAppearanceChange = { newAppearance: GiftCardWidgetAppearance ->
                                        stylingViewModel.updateWidgetComponentAppearance(
                                            widgetContext,
                                            styleItemName,
                                            newAppearance
                                        )
                                    }
                                )
                            }
                                ?: Text("$styleItemName appearance not available for $widgetContext")
                        }

                        WidgetType.ZIP -> {
                            (componentAppearance as? ZipWidgetAppearance)?.let { currentAppearance ->
                                StyleZipMiscSection(
                                    currentAppearance = currentAppearance,
                                    onAppearanceChange = { newAppearance ->
                                        stylingViewModel.updateWidgetComponentAppearance(
                                            widgetContext,
                                            styleItemName,
                                            newAppearance
                                        )
                                    }
                                )
                            }
                                ?: Text("$styleItemName appearance not available for $widgetContext")
                        }

                        else -> Text("$styleItemName appearance not available for $widgetContext")
                    }
                }

                StyleAppearanceComponent.SUB_DROP_DOWN_PROPERTIES -> {
                    (componentAppearance as? DropdownAppearance)?.let { currentAppearance ->
                        StyleDropdownMiscSection(
                            currentAppearance = currentAppearance,
                            onAppearanceChange = { newAppearance: DropdownAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PROPERTIES,
                StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PROPERTIES,
                StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PROPERTIES,
                StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PROPERTIES,
                StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PROPERTIES,
                StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PROPERTIES,
                StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PROPERTIES -> {
                    (componentAppearance as? TextFieldAppearance)?.let { currentTextFieldAppearance ->
                        StyleTextFieldSection(
                            currentAppearance = currentTextFieldAppearance,
                            onAppearanceChange = { newTextFieldAppearance: TextFieldAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newTextFieldAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> {
                    (componentAppearance as? ButtonAppearance)?.let { currentButtonAppearance ->
                        ButtonAppearanceStyleEditor(
                            currentAppearance = currentButtonAppearance,
                            onAppearanceChange = { newButtonAppearance: ButtonAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newButtonAppearance
                                )
                            }
                        )
                    }
                        ?: Text("$styleItemName appearance [${componentAppearance.javaClass}] not available for $widgetContext")
                }

                StyleAppearanceComponent.SUB_OVERLAY_LOADER_PROPERTIES -> {
                    (componentAppearance as? OverlayLoaderAppearance)?.let { currentAppearance ->
                        StyleOverlayLoaderMiscSection(
                            currentAppearance = currentAppearance,
                            onAppearanceChange = { newAppearance: OverlayLoaderAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.SUB_LINK_TEXT_PROPERTIES -> {
                    (componentAppearance as? LinkTextAppearance)?.let { currentAppearance ->
                        StyleLinkTextSection(
                            currentAppearance = currentAppearance,
                            onAppearanceChange = { newAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.SUB_OVERLAY_LOADER_CARD -> {
                    (componentAppearance as? CardAppearance)?.let { currentAppearance ->
                        StyleCardSection(
                            currentAppearance = currentAppearance,
                            onAppearanceChange = { newAppearance: CardAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newAppearance
                                )
                            }
                        )
                    } ?: Text("$styleItemName appearance not available for $widgetContext")
                }

                StyleAppearanceComponent.SUB_TEXT_BUTTON_PROPERTIES -> {
                    (componentAppearance as? ButtonAppearance.TextButtonAppearance)?.let { currentButtonAppearance ->
                        TextButtonAppearanceStyleEditor(
                            currentAppearance = currentButtonAppearance,
                            onAppearanceChange = { newButtonAppearance: ButtonAppearance.TextButtonAppearance ->
                                stylingViewModel.updateWidgetComponentAppearance(
                                    widgetContext,
                                    styleItemName,
                                    newButtonAppearance
                                )
                            }
                        )
                    }
                        ?: Text("$styleItemName appearance [${componentAppearance.javaClass}] not available for $widgetContext")
                }

                else -> {
                    Text("No specific styling defined for $styleItemName in context $widgetContext")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 20.dp)
                .testTag("style_properties_reset_section"),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                textAlign = TextAlign.Justify,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                text = stringResource(R.string.disclaimer_reset_button),
                modifier = Modifier.testTag("style_properties_reset_disclaimer")
            )

            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("style_properties_reset_button"),
                variant = AppButtonVariant.Outline,
                shape = AppButtonShape.Rectangle,
                enabled = hasChanges,
                text = stringResource(R.string.button_reset),
                onClick = {
                    initialAppearanceValue?.let {
                        stylingViewModel.updateWidgetComponentAppearance(
                            widgetContext,
                            styleItemName,
                            it // Pass the original, structurally distinct object
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun getAppearanceForWidgetComponent(
    widgetType: WidgetType,
    styleItemName: StyleAppearanceComponent,
    stylingViewModel: StylingViewModel
): State<Any?> { // Returns Any? because different components have different appearance types (TextAppearance, ButtonAppearance, etc.)
    return when (widgetType) {
        WidgetType.ADDRESS_DETAILS -> {
            val addressAppearance by stylingViewModel.addressWidgetAppearance.collectAsState()
            remember(addressAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.PROPERTIES -> addressAppearance
                        StyleAppearanceComponent.TITLE -> addressAppearance?.title
                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> addressAppearance?.actionButton
                        StyleAppearanceComponent.SUB_BUTTON_ICON -> addressAppearance?.actionButton?.iconAppearance
                        StyleAppearanceComponent.SUB_BUTTON_TEXT -> addressAppearance?.actionButton?.textAppearance
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> addressAppearance?.actionButton?.loaderAppearance
                        StyleAppearanceComponent.SUB_TEXT_BUTTON_PROPERTIES -> addressAppearance?.linkButton?.actionButton // Used for link button properties
                        StyleAppearanceComponent.SUB_LINK_BUTTON_ICON -> addressAppearance?.linkButton?.actionButton?.iconAppearance // Used for link button icon
                        StyleAppearanceComponent.SUB_LINK_BUTTON_TEXT -> addressAppearance?.linkButton?.actionButton?.textAppearance // Used for link button text
                        StyleAppearanceComponent.SUB_LINK_BUTTON_LOADER -> addressAppearance?.linkButton?.actionButton?.loaderAppearance // Used for link button loader
                        StyleAppearanceComponent.SUB_DROP_DOWN_PROPERTIES -> addressAppearance?.searchDropdown?.dropdown // Used for dropdown misc items
                        StyleAppearanceComponent.SUB_DROPDOWN_ITEM -> addressAppearance?.searchDropdown?.dropdown?.item // Used for dropdown item
                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PROPERTIES -> addressAppearance?.searchDropdown?.textField // Used for textfield properties
                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_LABEL -> addressAppearance?.searchDropdown?.textField?.label // Used for textfield label
                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_PLACEHOLDER -> addressAppearance?.searchDropdown?.textField?.placeholder // Used for textfield placeholder
                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_ERROR_LABEL -> addressAppearance?.searchDropdown?.textField?.errorLabel // Used for textfield error
                        StyleAppearanceComponent.SUB_SEARCH_TEXT_FIELD_VALID_ICON -> addressAppearance?.searchDropdown?.textField?.validIcon // Used for textfield icon
                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PROPERTIES -> addressAppearance?.textField
                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_LABEL -> addressAppearance?.textField?.label
                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_PLACEHOLDER -> addressAppearance?.textField?.placeholder
                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_ERROR_LABEL -> addressAppearance?.textField?.errorLabel
                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_HINT_LABEL -> addressAppearance?.textField?.hintLabel
                        StyleAppearanceComponent.SUB_ADDRESS_TEXT_FIELD_VALID_ICON -> addressAppearance?.textField?.validIcon
                        else -> null
                    }
                }
            }
        }

        WidgetType.CARD_DETAILS -> {
            val cardAppearance by stylingViewModel.cardDetailsWidgetAppearance.collectAsState()
            remember(cardAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.PROPERTIES -> cardAppearance
                        StyleAppearanceComponent.TOGGLE_TEXT -> cardAppearance?.toggleText
                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> cardAppearance?.actionButton
                        StyleAppearanceComponent.SUB_BUTTON_ICON -> cardAppearance?.actionButton?.iconAppearance
                        StyleAppearanceComponent.SUB_BUTTON_TEXT -> cardAppearance?.actionButton?.textAppearance
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> cardAppearance?.actionButton?.loaderAppearance
                        StyleAppearanceComponent.TOGGLE -> cardAppearance?.toggle
                        StyleAppearanceComponent.LINK_TEXT -> cardAppearance?.linkText
                        StyleAppearanceComponent.SUB_LINK_TEXT_PROPERTIES -> cardAppearance?.linkText
                        StyleAppearanceComponent.SUB_LINK_TEXT -> cardAppearance?.linkText?.textAppearance
                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PROPERTIES -> cardAppearance?.cardNameTextField
                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PROPERTIES -> cardAppearance?.cardNumberTextField
                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PROPERTIES -> cardAppearance?.cardExpiryTextField
                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PROPERTIES -> cardAppearance?.cardSecurityCodeTextField
                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_LABEL -> cardAppearance?.cardNameTextField?.label
                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_PLACEHOLDER -> cardAppearance?.cardNameTextField?.placeholder
                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_ERROR_LABEL -> cardAppearance?.cardNameTextField?.errorLabel
                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_HINT_LABEL -> cardAppearance?.cardNameTextField?.hintLabel
                        StyleAppearanceComponent.SUB_CARD_NAME_TEXT_FIELD_VALID_ICON -> cardAppearance?.cardNameTextField?.validIcon
                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_LABEL -> cardAppearance?.cardNumberTextField?.label
                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_PLACEHOLDER -> cardAppearance?.cardNumberTextField?.placeholder
                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_ERROR_LABEL -> cardAppearance?.cardNumberTextField?.errorLabel
                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_HINT_LABEL -> cardAppearance?.cardNumberTextField?.hintLabel
                        StyleAppearanceComponent.SUB_CARD_NUMBER_TEXT_FIELD_VALID_ICON -> cardAppearance?.cardNumberTextField?.validIcon
                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_LABEL -> cardAppearance?.cardExpiryTextField?.label
                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_PLACEHOLDER -> cardAppearance?.cardExpiryTextField?.placeholder
                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_ERROR_LABEL -> cardAppearance?.cardExpiryTextField?.errorLabel
                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_HINT_LABEL -> cardAppearance?.cardExpiryTextField?.hintLabel
                        StyleAppearanceComponent.SUB_CARD_EXPIRY_TEXT_FIELD_VALID_ICON -> cardAppearance?.cardExpiryTextField?.validIcon
                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_LABEL -> cardAppearance?.cardSecurityCodeTextField?.label
                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_PLACEHOLDER -> cardAppearance?.cardSecurityCodeTextField?.placeholder
                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_ERROR_LABEL -> cardAppearance?.cardSecurityCodeTextField?.errorLabel
                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_HINT_LABEL -> cardAppearance?.cardSecurityCodeTextField?.hintLabel
                        StyleAppearanceComponent.SUB_CARD_SECURITY_TEXT_FIELD_VALID_ICON -> cardAppearance?.cardSecurityCodeTextField?.validIcon
                        else -> null
                    }
                }
            }
        }

        WidgetType.GIFT_CARD -> {
            val giftCardAppearance by stylingViewModel.giftCardWidgetAppearance.collectAsState()
            remember(giftCardAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.PROPERTIES -> giftCardAppearance
                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> giftCardAppearance?.actionButton
                        StyleAppearanceComponent.SUB_BUTTON_ICON -> giftCardAppearance?.actionButton?.iconAppearance
                        StyleAppearanceComponent.SUB_BUTTON_TEXT -> giftCardAppearance?.actionButton?.textAppearance
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> giftCardAppearance?.actionButton?.loaderAppearance
                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PROPERTIES -> giftCardAppearance?.textField
                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_LABEL -> giftCardAppearance?.textField?.label
                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_PLACEHOLDER -> giftCardAppearance?.textField?.placeholder
                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_ERROR_LABEL -> giftCardAppearance?.textField?.errorLabel
                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_HINT_LABEL -> giftCardAppearance?.textField?.hintLabel
                        StyleAppearanceComponent.SUB_GIFT_CARD_TEXT_FIELD_VALID_ICON -> giftCardAppearance?.textField?.validIcon
                        else -> null
                    }
                }
            }
        }

        WidgetType.AFTER_PAY -> {
            val afterPayAppearance by stylingViewModel.afterpayWidgetAppearance.collectAsState()
            remember(afterPayAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.PROPERTIES -> afterPayAppearance
                        StyleAppearanceComponent.LOADER -> afterPayAppearance?.loader
                        else -> null
                    }
                }
            }
        }

        WidgetType.COLES_PAY -> {
            val colesPayAppearance by stylingViewModel.colesPayWidgetAppearance.collectAsState()
            remember(colesPayAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.LOADER -> colesPayAppearance?.loader
                        StyleAppearanceComponent.IMAGE_BUTTON -> colesPayAppearance?.imageButton
                        else -> null
                    }
                }
            }
        }

        WidgetType.GOOGLE_PAY -> {
            val googlePayAppearance by stylingViewModel.googlePayWidgetAppearance.collectAsState()
            remember(googlePayAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.PROPERTIES -> googlePayAppearance
                        StyleAppearanceComponent.LOADER -> googlePayAppearance?.loader
                        else -> null
                    }
                }
            }
        }

        WidgetType.MPGS_3DS -> {
            val mpgs3dsAppearance by stylingViewModel.mpgs3dsWidgetAppearance.collectAsState()
            remember(mpgs3dsAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.LOADER -> mpgs3dsAppearance?.loader
                        else -> null
                    }
                }
            }
        }

        WidgetType.CLICK_TO_PAY -> {
            val clickToPayAppearance by stylingViewModel.clickToPayWidgetAppearance.collectAsState()
            remember(clickToPayAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.LOADER -> clickToPayAppearance?.loader
                        else -> null
                    }
                }
            }
        }

        WidgetType.PAY_PAL -> {
            val paypalAppearance by stylingViewModel.paypalWidgetAppearance.collectAsState()
            return remember(paypalAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.PROPERTIES -> paypalAppearance
                        StyleAppearanceComponent.LOADER -> paypalAppearance?.loader
                        else -> null
                    }
                }
            }
        }

        WidgetType.PAY_PAL_VAULT -> {
            val paypalVaultAppearance by stylingViewModel.paypalVaultWidgetAppearance.collectAsState()
            remember(paypalVaultAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.SUB_ACTION_BUTTON_PROPERTIES -> paypalVaultAppearance?.actionButton
                        StyleAppearanceComponent.SUB_BUTTON_ICON -> paypalVaultAppearance?.actionButton?.iconAppearance
                        StyleAppearanceComponent.SUB_BUTTON_TEXT -> paypalVaultAppearance?.actionButton?.textAppearance
                        StyleAppearanceComponent.SUB_BUTTON_LOADER -> paypalVaultAppearance?.actionButton?.loaderAppearance
                        else -> null
                    }
                }
            }
        }

        WidgetType.STANDALONE_3DS -> {
            val standalone3SDAppearance by stylingViewModel.standalone3DSWidgetAppearance.collectAsState()
            remember(standalone3SDAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.OVERLAY_LOADER -> standalone3SDAppearance?.loader
                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_PROPERTIES -> standalone3SDAppearance?.loader
                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_CARD -> standalone3SDAppearance?.loader?.cardAppearance
                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_INDICATOR -> standalone3SDAppearance?.loader?.loaderAppearance
                        StyleAppearanceComponent.SUB_OVERLAY_LOADER_TEXT -> standalone3SDAppearance?.loader?.loaderTextAppearance
                        else -> null
                    }
                }
            }
        }

        WidgetType.ZIP -> {
            val zipAppearance by stylingViewModel.zipWidgetAppearance.collectAsState()
            remember(zipAppearance, styleItemName) {
                derivedStateOf {
                    when (styleItemName) {
                        StyleAppearanceComponent.PROPERTIES -> zipAppearance
                        StyleAppearanceComponent.LOADER -> zipAppearance?.loader
                        else -> null
                    }
                }
            }
        }
    }
}