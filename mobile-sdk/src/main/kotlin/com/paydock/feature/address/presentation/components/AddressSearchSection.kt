package com.paydock.feature.address.presentation.components

import android.location.Address
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.designsystems.theme.Theme

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
fun AddressSearchSection(onAddressSelected: (Address) -> Unit) {
    // Header text
    Text(
        modifier = Modifier.fillMaxWidth(),
        style = Theme.typography.body1,
        text = stringResource(R.string.label_find_an_address),
        color = Theme.colors.onSurface
    )

    // Address search input
    AddressSearchInput(
        modifier = Modifier.testTag("addressSearch"),
        onAddressSelected = onAddressSelected
    )
}
