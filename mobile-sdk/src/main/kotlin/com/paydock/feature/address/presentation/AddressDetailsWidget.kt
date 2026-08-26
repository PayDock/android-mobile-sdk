package com.paydock.feature.address.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.R
import com.paydock.core.domain.model.Event
import com.paydock.core.domain.model.EventAction
import com.paydock.core.presentation.util.WidgetEventDelegate
import com.paydock.designsystems.components.button.ButtonAppearance
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.designsystems.components.button.RenderButton
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.components.link.LinkButtonAppearance
import com.paydock.designsystems.components.link.LinkButtonAppearanceDefaults
import com.paydock.designsystems.components.link.SdkLinkButton
import com.paydock.designsystems.components.search.SearchDropdownAppearance
import com.paydock.designsystems.components.search.SearchDropdownAppearanceDefaults
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults
import com.paydock.feature.address.domain.mapper.integration.asEntity
import com.paydock.feature.address.domain.model.AddressDetailsEventNames
import com.paydock.feature.address.domain.model.integration.AddressDetailsWidgetConfig
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.components.AddressSearchSection
import com.paydock.feature.address.presentation.components.ManualAddressEntry
import com.paydock.feature.address.presentation.components.NameSectionEntry
import com.paydock.feature.address.presentation.state.AddressDetailsFormState.AddressField
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * A composable widget that provides a form for users to enter their address details.
 *
 * This widget includes fields for first name, last name, address lines, city, state, postal code, and country.
 * It also features an address search functionality to pre-fill the form and an option to
 * enter the address manually.
 *
 * @param modifier Modifier to apply to the composable.
 * @param appearance Customization options for the widget's appearance. See [AddressDetailsWidgetAppearance].
 * @param config Configuration for the widget's behavior and initial data, such as pre-filling the address. See [AddressDetailsWidgetConfig].
 * @param eventDelegate An optional [WidgetEventDelegate] for tracking widget events such as button clicks.
 * @param completion A callback function that is invoked when the user saves the address. It receives the entered [BillingAddress] as a parameter.
 */
@Composable
fun AddressDetailsWidget(
    modifier: Modifier = Modifier,
    appearance: AddressDetailsWidgetAppearance = AddressDetailsAppearanceDefaults.appearance(),
    config: AddressDetailsWidgetConfig = AddressDetailsWidgetConfig(),
    eventDelegate: WidgetEventDelegate? = null,
    completion: (BillingAddress) -> Unit,
) {
    // Use config.address as part of the key to force fresh ViewModel when address changes
    // This ensures that editing vs adding gets separate ViewModel instances
    val viewModelKey = remember(config.address) {
        "address_widget_${config.address.hashCode()}_${System.currentTimeMillis()}"
    }
    val viewModel: AddressDetailsViewModel = koinViewModel(key = viewModelKey)
    val uiState by viewModel.stateFlow.collectAsState()

    // Control whether the manual address section is shown
    var isManualAddressVisible by rememberSaveable {
        // Initialize based on whether an initial address is provided,
        // or if any part of the uiState.billingAddress (from ViewModel) suggests it should be visible.
        mutableStateOf(config.address != null || !uiState.billingAddress.isEmpty())
    }
    // Control whether the manual address input is valid (improve recompositions)
    val isDataValid by remember(uiState) { derivedStateOf { uiState.isDataValid } }
    val isEnabled by remember(uiState) {
        derivedStateOf { if (config.activePrimaryButton) true else isDataValid }
    }

    // Resolve per-field appearance, falling back to the shared textField when no override is provided.
    val firstNameAppearance = appearance.firstNameTextField ?: appearance.textField
    val lastNameAppearance = appearance.lastNameTextField ?: appearance.textField
    val addressLine1Appearance = appearance.addressLine1TextField ?: appearance.textField
    val addressLine2Appearance = appearance.addressLine2TextField ?: appearance.textField
    val cityAppearance = appearance.cityTextField ?: appearance.textField
    val stateAppearance = appearance.stateTextField ?: appearance.textField
    val postcodeAppearance = appearance.postcodeTextField ?: appearance.textField

    var focusFirstNameA11y by remember { mutableStateOf(false) }
    var focusLastNameA11y by remember { mutableStateOf(false) }
    var focusAddressLine1A11y by remember { mutableStateOf(false) }
    var focusCityA11y by remember { mutableStateOf(false) }
    var focusStateA11y by remember { mutableStateOf(false) }
    var focusPostcodeA11y by remember { mutableStateOf(false) }
    var errorAnnouncement by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Remember the preset address value to avoid recomposition on every change
    LaunchedEffect(Unit) {
        config.address?.let {
            viewModel.populateFormWithBillingAddress(it)
        }
    }

    // Reset a11y focus flags shortly after firing so they can be re-triggered on subsequent submits.
    LaunchedEffect(focusFirstNameA11y) {
        if (focusFirstNameA11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusFirstNameA11y = false
        }
    }
    LaunchedEffect(focusLastNameA11y) {
        if (focusLastNameA11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusLastNameA11y = false
        }
    }
    LaunchedEffect(focusAddressLine1A11y) {
        if (focusAddressLine1A11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusAddressLine1A11y = false
        }
    }
    LaunchedEffect(focusCityA11y) {
        if (focusCityA11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusCityA11y = false
        }
    }
    LaunchedEffect(focusStateA11y) {
        if (focusStateA11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusStateA11y = false
        }
    }
    LaunchedEffect(focusPostcodeA11y) {
        if (focusPostcodeA11y) {
            delay(A11Y_FOCUS_RESET_DELAY)
            focusPostcodeA11y = false
        }
    }
    LaunchedEffect(errorAnnouncement) {
        if (errorAnnouncement != null) {
            delay(ERROR_ANNOUNCEMENT_RESET_DELAY)
            errorAnnouncement = null
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(appearance.verticalSpacing, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        NameSectionEntry(
            firstName = uiState.billingAddress.firstName,
            lastName = uiState.billingAddress.lastName,
            onFirstNameChange = viewModel::updateFirstName,
            onLastNameChange = viewModel::updateLastName,
            verticalSpacing = appearance.textFieldVerticalSpacing,
            horizontalSpacing = appearance.textFieldHorizontalSpacing,
            titleAppearance = appearance.title,
            firstNameAppearance = firstNameAppearance,
            lastNameAppearance = lastNameAppearance,
            firstNameForceShowErrors = uiState.billingAddress.firstNameErrorOverwrite,
            lastNameForceShowErrors = uiState.billingAddress.lastNameErrorOverwrite,
            a11yFirstNameFocus = focusFirstNameA11y,
            a11yLastNameFocus = focusLastNameA11y,
        )

        AddressSearchSection(
            titleAppearance = appearance.title,
            searchAppearance = appearance.searchDropdown
        ) { searchResultAddress ->
            isManualAddressVisible = true
            viewModel.populateFormWithBillingAddress(searchResultAddress.asEntity())
        }

        // Show the "Enter Address Manually" text
        if (!isManualAddressVisible) {
            SdkLinkButton(
                modifier = Modifier.testTag("showManualAddressButton"),
                linkText = stringResource(R.string.button_enter_address_manually),
                appearance = appearance.linkButton
            ) {
                // Emit manual entry button event
                eventDelegate?.widgetEvent(
                    Event.ButtonEvent(
                        name = AddressDetailsEventNames.MANUAL_ENTRY_BUTTON,
                        action = EventAction.CLICK
                    )
                )
                isManualAddressVisible = !isManualAddressVisible
            }
        }

        ManualAddressEntry(
            isManualAddressVisible = isManualAddressVisible,
            addressInputState = uiState.billingAddress,
            onAddressLine1Change = viewModel::updateAddressLine1,
            onAddressLine2Change = viewModel::updateAddressLine2,
            onCityChange = viewModel::updateCity,
            onStateChange = viewModel::updateState,
            onPostalCodeChange = viewModel::updatePostalCode,
            onCountryChange = viewModel::updateCountry,
            verticalSpacing = appearance.textFieldVerticalSpacing,
            textFieldAppearance = appearance.textField,
            searchAppearance = appearance.searchDropdown,
            addressLine1Appearance = addressLine1Appearance,
            addressLine2Appearance = addressLine2Appearance,
            cityAppearance = cityAppearance,
            stateAppearance = stateAppearance,
            postcodeAppearance = postcodeAppearance,
            addressLine1ForceShowErrors = uiState.billingAddress.addressLine1ErrorOverwrite,
            cityForceShowErrors = uiState.billingAddress.cityErrorOverwrite,
            stateForceShowErrors = uiState.billingAddress.stateErrorOverwrite,
            postcodeForceShowErrors = uiState.billingAddress.postcodeErrorOverwrite,
            a11yAddressLine1Focus = focusAddressLine1A11y,
            a11yCityFocus = focusCityA11y,
            a11yStateFocus = focusStateA11y,
            a11yPostcodeFocus = focusPostcodeA11y,
        )

        Column {
            val errorCount = uiState.billingAddress.errorCount
            val errorCountMessage = when {
                errorCount > 1 -> stringResource(R.string.error_form_count_plural, errorCount)
                errorCount == 1 -> stringResource(R.string.error_form_count_singular, errorCount)
                else -> ""
            }
            if (errorAnnouncement != null) {
                // Invisible text used for screen reader announcement via live region
                Text(
                    text = errorAnnouncement!!,
                    modifier = Modifier
                        .size(1.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            // Save Address button
            appearance.actionButton.RenderButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveAddress"),
                text = appearance.actionButton.text,
                buttonIcon = appearance.actionButton.icon,
                enabled = isEnabled,
            ) {
                focusManager.clearFocus()

                // Emit save button event
                eventDelegate?.widgetEvent(
                    Event.ButtonEvent(
                        name = AddressDetailsEventNames.SAVE_BUTTON,
                        action = EventAction.CLICK,
                        text = appearance.actionButton.text
                    )
                )

                if (config.activePrimaryButton && !isDataValid) {
                    // Button enabled by default but data invalid: trigger validation to surface errors.
                    viewModel.validateAllFields()
                    if (!isManualAddressVisible) {
                        // Reveal the manual-entry fields so the errors being surfaced are visible.
                        isManualAddressVisible = true
                    }

                    errorAnnouncement = errorCountMessage

                    coroutineScope.launch {
                        if (focusFirstNameA11y) focusFirstNameA11y = false
                        if (focusLastNameA11y) focusLastNameA11y = false
                        if (focusAddressLine1A11y) focusAddressLine1A11y = false
                        if (focusCityA11y) focusCityA11y = false
                        if (focusStateA11y) focusStateA11y = false
                        if (focusPostcodeA11y) focusPostcodeA11y = false

                        delay(FIRST_ERROR_FOCUS_DELAY)

                        // Shift accessibility focus to the first invalid field. The country field is
                        // validated but not focusable here (it's driven by a separate search component).
                        when (uiState.billingAddress.invalidFields.firstOrNull()) {
                            AddressField.FIRST_NAME -> focusFirstNameA11y = true
                            AddressField.LAST_NAME -> focusLastNameA11y = true
                            AddressField.ADDRESS_LINE_1 -> focusAddressLine1A11y = true
                            AddressField.CITY -> focusCityA11y = true
                            AddressField.STATE -> focusStateA11y = true
                            AddressField.POSTCODE -> focusPostcodeA11y = true
                            AddressField.COUNTRY, null -> Unit
                        }
                    }
                } else {
                    errorAnnouncement = null
                    completion(viewModel.getBillingAddress())
                }
            }
        }
    }
}

// Give the accessibility framework time to read out before resetting the focus flag; firing again
// before readout completes would re-announce the state change.
private const val A11Y_FOCUS_RESET_DELAY = 20000L
private const val ERROR_ANNOUNCEMENT_RESET_DELAY = 2000L
private const val FIRST_ERROR_FOCUS_DELAY = 1000L

/**
 * Represents the appearance configuration for the [AddressDetailsWidget].
 *
 * @property horizontalSpacing The horizontal spacing between the elements in the widget.
 * @property verticalSpacing The vertical spacing between the elements in the widget.
 * @property textFieldVerticalSpacing The vertical spacing between text input fields.
 * @property textFieldHorizontalSpacing The horizontal spacing between text input fields.
 * @property title The appearance configuration for the title text.
 * @property textField The appearance configuration for the text fields.
 * @property actionButton The appearance configuration for the action button.
 * @property linkButton The appearance configuration for the link button.
 * @property searchDropdown The appearance configuration for the search dropdown.
 */
@Immutable
class AddressDetailsWidgetAppearance(
    val horizontalSpacing: Dp,
    val verticalSpacing: Dp,
    val textFieldVerticalSpacing: Dp,
    val textFieldHorizontalSpacing: Dp,
    val title: TextAppearance,
    val textField: TextFieldAppearance,
    val actionButton: ButtonAppearance,
    val linkButton: LinkButtonAppearance,
    val searchDropdown: SearchDropdownAppearance,
    // Optional per-field overrides. When null, the field falls back to [textField]. The country field
    // is styled via [searchDropdown] and so is not overridable here.
    val firstNameTextField: TextFieldAppearance? = null,
    val lastNameTextField: TextFieldAppearance? = null,
    val addressLine1TextField: TextFieldAppearance? = null,
    val addressLine2TextField: TextFieldAppearance? = null,
    val cityTextField: TextFieldAppearance? = null,
    val stateTextField: TextFieldAppearance? = null,
    val postcodeTextField: TextFieldAppearance? = null
) {

    /**
     * Creates a copy of this [AddressDetailsWidgetAppearance] with the specified changes.
     *
     * @param horizontalSpacing The horizontal spacing between elements.
     * @param verticalSpacing The vertical spacing between elements.
     * @param textFieldVerticalSpacing The vertical spacing between text fields.
     * @param textFieldHorizontalSpacing The horizontal spacing between text fields.
     * @param title The appearance for the title text.
     * @param textField The appearance for the text input fields.
     * @param actionButton The appearance for the action button (Save Address).
     * @param linkButton The appearance for the link button (Enter Address Manually).
     * @param searchDropdown The appearance for the address search dropdown.
     * @return A new [AddressDetailsWidgetAppearance] with the updated values.
     */
    @Suppress("LongParameterList")
    fun copy(
        horizontalSpacing: Dp = this.horizontalSpacing,
        verticalSpacing: Dp = this.verticalSpacing,
        textFieldVerticalSpacing: Dp = this.textFieldVerticalSpacing,
        textFieldHorizontalSpacing: Dp = this.textFieldHorizontalSpacing,
        title: TextAppearance = this.title,
        textField: TextFieldAppearance = this.textField,
        actionButton: ButtonAppearance = this.actionButton,
        linkButton: LinkButtonAppearance = this.linkButton,
        searchDropdown: SearchDropdownAppearance = this.searchDropdown,
        firstNameTextField: TextFieldAppearance? = this.firstNameTextField,
        lastNameTextField: TextFieldAppearance? = this.lastNameTextField,
        addressLine1TextField: TextFieldAppearance? = this.addressLine1TextField,
        addressLine2TextField: TextFieldAppearance? = this.addressLine2TextField,
        cityTextField: TextFieldAppearance? = this.cityTextField,
        stateTextField: TextFieldAppearance? = this.stateTextField,
        postcodeTextField: TextFieldAppearance? = this.postcodeTextField
    ): AddressDetailsWidgetAppearance = AddressDetailsWidgetAppearance(
        horizontalSpacing = horizontalSpacing.takeOrElse { this.horizontalSpacing },
        verticalSpacing = verticalSpacing.takeOrElse { this.verticalSpacing },
        textFieldVerticalSpacing = textFieldVerticalSpacing.takeOrElse { this.textFieldVerticalSpacing },
        textFieldHorizontalSpacing = textFieldHorizontalSpacing.takeOrElse { this.textFieldHorizontalSpacing },
        title = title.copy(),
        textField = textField.copy(),
        actionButton = when (actionButton) {
            is ButtonAppearance.FilledButtonAppearance -> actionButton.copy()
            is ButtonAppearance.IconButtonAppearance -> actionButton.copy()
            is ButtonAppearance.OutlineButtonAppearance -> actionButton.copy()
            is ButtonAppearance.TextButtonAppearance -> actionButton.copy()
        },
        linkButton = linkButton.copy(),
        searchDropdown = searchDropdown.copy(),
        firstNameTextField = firstNameTextField?.copy(),
        lastNameTextField = lastNameTextField?.copy(),
        addressLine1TextField = addressLine1TextField?.copy(),
        addressLine2TextField = addressLine2TextField?.copy(),
        cityTextField = cityTextField?.copy(),
        stateTextField = stateTextField?.copy(),
        postcodeTextField = postcodeTextField?.copy()
    )

    @Suppress("CyclomaticComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressDetailsWidgetAppearance

        if (horizontalSpacing != other.horizontalSpacing) return false
        if (verticalSpacing != other.verticalSpacing) return false
        if (textFieldVerticalSpacing != other.textFieldVerticalSpacing) return false
        if (textFieldHorizontalSpacing != other.textFieldHorizontalSpacing) return false
        if (title != other.title) return false
        if (textField != other.textField) return false
        if (actionButton != other.actionButton) return false
        if (linkButton != other.linkButton) return false
        if (searchDropdown != other.searchDropdown) return false
        if (firstNameTextField != other.firstNameTextField) return false
        if (lastNameTextField != other.lastNameTextField) return false
        if (addressLine1TextField != other.addressLine1TextField) return false
        if (addressLine2TextField != other.addressLine2TextField) return false
        if (cityTextField != other.cityTextField) return false
        if (stateTextField != other.stateTextField) return false
        if (postcodeTextField != other.postcodeTextField) return false

        return true
    }

    override fun hashCode(): Int {
        var result = verticalSpacing.hashCode()
        result = 31 * result + horizontalSpacing.hashCode()
        result = 31 * result + textFieldVerticalSpacing.hashCode()
        result = 31 * result + textFieldHorizontalSpacing.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + textField.hashCode()
        result = 31 * result + actionButton.hashCode()
        result = 31 * result + linkButton.hashCode()
        result = 31 * result + searchDropdown.hashCode()
        result = 31 * result + (firstNameTextField?.hashCode() ?: 0)
        result = 31 * result + (lastNameTextField?.hashCode() ?: 0)
        result = 31 * result + (addressLine1TextField?.hashCode() ?: 0)
        result = 31 * result + (addressLine2TextField?.hashCode() ?: 0)
        result = 31 * result + (cityTextField?.hashCode() ?: 0)
        result = 31 * result + (stateTextField?.hashCode() ?: 0)
        result = 31 * result + (postcodeTextField?.hashCode() ?: 0)
        return result
    }
}

/**
 * Defaults for the appearance of the [AddressDetailsWidget].
 *
 * This object provides a default [AddressDetailsWidgetAppearance] that includes
 * standard spacing, typography for titles, text field styles, and button appearances.
 * It is designed to offer a consistent look and feel for the address details input UI
 * within the application.
 */
object AddressDetailsAppearanceDefaults {

    /**
     * Returns the default appearance for the [AddressDetailsWidget].
     *
     * This appearance includes default spacing, title text style, text field appearance,
     * action button appearance (filled button), link button appearance, and search dropdown appearance.
     *
     * @return The default [AddressDetailsWidgetAppearance].
     */
    @Composable
    fun appearance(): AddressDetailsWidgetAppearance = AddressDetailsWidgetAppearance(
        horizontalSpacing = WidgetDefaults.Spacing,
        verticalSpacing = WidgetDefaults.Spacing,
        textFieldVerticalSpacing = WidgetDefaults.Spacing,
        textFieldHorizontalSpacing = WidgetDefaults.Spacing,
        title = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.titleMedium,
        ),
        textField = TextFieldAppearanceDefaults.appearance().copy(singleLine = true),
        actionButton = ButtonAppearanceDefaults.filledButtonAppearance().copy(
            text = stringResource(R.string.button_save_address)
        ),
        linkButton = LinkButtonAppearanceDefaults.appearance(),
        searchDropdown = SearchDropdownAppearanceDefaults.appearance()
    )
}