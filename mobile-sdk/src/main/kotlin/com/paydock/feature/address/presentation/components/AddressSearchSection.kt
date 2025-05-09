package com.paydock.feature.address.presentation.components

import android.location.Address
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.designsystems.components.search.SearchDropdownAppearance
import com.paydock.designsystems.components.search.SearchDropdownAppearanceDefaults
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults

/**
 * A composable that displays an address search section with a header and an input field
 * for finding and selecting an address.
 *
 * @param onAddressSelected A callback invoked when the user selects an address from the search results.
 * The selected address is provided as an [Address].
 *
 * This section includes:
 * - A header text prompting the user to find an address.
 * - An [AddressSearchInput] composable for entering and selecting an address.
 *
 * Example:
 * ```
 * AddressSearchSection { selectedAddress ->
 *     println("Selected Address: $selectedAddress")
 * }
 * ```
 *
 * Notes:
 * - The header text uses the `body1` typography style from the current theme.
 * - The input field is tagged with `"addressSearch"` for UI testing.
 */
@Composable
fun AddressSearchSection(
    titleAppearance: TextAppearance = TextAppearanceDefaults.appearance(),
    searchAppearance: SearchDropdownAppearance = SearchDropdownAppearanceDefaults.appearance(),
    onAddressSelected: (Address) -> Unit
) {
    // Header text
    SdkText(
        modifier = Modifier.fillMaxWidth(),
        appearance = titleAppearance,
        text = stringResource(R.string.label_find_an_address),
    )

    // Address search input
    AddressSearchInput(
        modifier = Modifier.testTag("addressSearch"),
        appearance = searchAppearance,
        onAddressSelected = onAddressSelected
    )
}