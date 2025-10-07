package com.paydock.feature.address.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.R
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
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.feature.address.presentation.components.AddressSearchSection
import com.paydock.feature.address.presentation.components.ManualAddressEntry
import com.paydock.feature.address.presentation.components.NameSectionEntry
import com.paydock.feature.address.presentation.viewmodels.AddressDetailsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable for displaying address details input UI.
 *
 * This widget provides a form for users to enter their address details. It includes fields for
 * first name, last name, address lines, city, state, postal code, and country.
 * It also features an address search functionality to pre-fill the form and an option to
 * enter the address manually.
 *
 * The appearance of the widget can be customized using the `appearance` parameter.
 *
 * @param modifier Modifier to apply to the composable.
 * @param appearance Customization options for the widget's appearance. See [AddressDetailsWidgetAppearance].
 * @param address An optional [BillingAddress] to pre-fill the input fields. If provided, the form will be populated with this address.
 * @param completion A callback function that is invoked when the user saves the address.
 * It receives the entered [BillingAddress] as a parameter.
 */
@Composable
fun AddressDetailsWidget(
    modifier: Modifier = Modifier,
    appearance: AddressDetailsWidgetAppearance = AddressDetailsAppearanceDefaults.appearance(),
    address: BillingAddress? = null,
    completion: (BillingAddress) -> Unit,
) {
    val viewModel: AddressDetailsViewModel = koinViewModel()
    val uiState by viewModel.stateFlow.collectAsState()

    // Control whether the manual address section is shown
    var isManualAddressVisible by rememberSaveable(key = "isManualAddressVisible_${address?.hashCode()}") {
        // Initialize based on whether an initial address is provided,
        // or if any part of the uiState.billingAddress (from ViewModel) suggests it should be visible.
        mutableStateOf(address != null || !uiState.billingAddress.isEmpty())
    }
    // Control whether the manual address input is valid (improve recompositions)
    val isDataValid by remember(uiState) { derivedStateOf { uiState.isDataValid } }

    // Remember the preset address value to avoid recomposition on every change
    LaunchedEffect(address) {
        address?.let {
            viewModel.populateFormWithBillingAddress(it)
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
            verticalSpacing = appearance.verticalSpacing,
            horizontalSpacing = appearance.horizontalSpacing,
            titleAppearance = appearance.title,
            textFieldAppearance = appearance.textField
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
            verticalSpacing = appearance.verticalSpacing,
            textFieldAppearance = appearance.textField,
            searchAppearance = appearance.searchDropdown
        )

        // Save Address button
        appearance.actionButton.RenderButton(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("saveAddress"),
            text = stringResource(R.string.button_save_address),
            enabled = isDataValid,
        ) {
            completion(viewModel.getBillingAddress())
        }
    }
}

/**
 * Represents the appearance configuration for the [AddressDetailsWidget].
 *
 * @property horizontalSpacing The horizontal spacing between the elements in the widget.
 * @property verticalSpacing The vertical spacing between the elements in the widget.
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
    val title: TextAppearance,
    val textField: TextFieldAppearance,
    val actionButton: ButtonAppearance,
    val linkButton: LinkButtonAppearance,
    val searchDropdown: SearchDropdownAppearance
) {

    /**
     * Creates a copy of this [AddressDetailsWidgetAppearance] with the specified changes.
     *
     * @param horizontalSpacing The horizontal spacing between elements.
     * @param verticalSpacing The vertical spacing between elements.
     * @param title The appearance for the title text.
     * @param textField The appearance for the text input fields.
     * @param actionButton The appearance for the action button (Save Address).
     * @param linkButton The appearance for the link button (Enter Address Manually).
     * @param searchDropdown The appearance for the address search dropdown.
     * @return A new [AddressDetailsWidgetAppearance] with the updated values.
     */
    fun copy(
        horizontalSpacing: Dp = this.horizontalSpacing,
        verticalSpacing: Dp = this.verticalSpacing,
        title: TextAppearance = this.title,
        textField: TextFieldAppearance = this.textField,
        actionButton: ButtonAppearance = this.actionButton,
        linkButton: LinkButtonAppearance = this.linkButton,
        searchDropdown: SearchDropdownAppearance = this.searchDropdown
    ): AddressDetailsWidgetAppearance = AddressDetailsWidgetAppearance(
        horizontalSpacing = horizontalSpacing.takeOrElse { this.horizontalSpacing },
        verticalSpacing = verticalSpacing.takeOrElse { this.verticalSpacing },
        title = title.copy(),
        textField = textField.copy(),
        actionButton = when (actionButton) {
            is ButtonAppearance.FilledButtonAppearance -> actionButton.copy()
            is ButtonAppearance.IconButtonAppearance -> actionButton.copy()
            is ButtonAppearance.OutlineButtonAppearance -> actionButton.copy()
            is ButtonAppearance.TextButtonAppearance -> actionButton.copy()
        },
        linkButton = linkButton.copy(),
        searchDropdown = searchDropdown.copy()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressDetailsWidgetAppearance

        if (horizontalSpacing != other.horizontalSpacing) return false
        if (verticalSpacing != other.verticalSpacing) return false
        if (title != other.title) return false
        if (textField != other.textField) return false
        if (actionButton != other.actionButton) return false
        if (linkButton != other.linkButton) return false
        if (searchDropdown != other.searchDropdown) return false

        return true
    }

    override fun hashCode(): Int {
        var result = verticalSpacing.hashCode()
        result = 31 * result + horizontalSpacing.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + textField.hashCode()
        result = 31 * result + actionButton.hashCode()
        result = 31 * result + linkButton.hashCode()
        result = 31 * result + searchDropdown.hashCode()
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
        title = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.titleMedium,
        ),
        textField = TextFieldAppearanceDefaults.appearance().copy(singleLine = true),
        actionButton = ButtonAppearanceDefaults.filledButtonAppearance(),
        linkButton = LinkButtonAppearanceDefaults.appearance(),
        searchDropdown = SearchDropdownAppearanceDefaults.appearance()
    )
}