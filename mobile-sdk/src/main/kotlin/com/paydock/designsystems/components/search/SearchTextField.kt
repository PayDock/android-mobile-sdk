package com.paydock.designsystems.components.search

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.designsystems.components.DropdownListFieldStack
import com.paydock.designsystems.components.SdkDropDownMenu
import com.paydock.designsystems.components.SdkTextField

/**
 * A search text field composable for filtering and selecting items using a SearchViewModel.
 *
 * @param modifier Modifier for styling and layout.
 * @param label The label to be displayed in the input field.
 * @param autofillType An optional [AutofillType] indicating the type of data that can be
 * autofilled for this input field (e.g., address, postal code). If provided, it enables
 * autofill support.
 * @param leadingIcon Optional leading icon to be displayed at the beginning of the search field container
 * @param trailingIcon Optional trailing icon to be displayed at the end of the search field container
 * @param noResultsFoundLabel Label to display when no results are found.
 * @param selectedItem The initially selected item, if any.
 * @param viewModel The SearchViewModel to handle search and selection logic.
 * @param onItemSelected Callback function when an item is selected.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
@Composable
internal fun <T : SearchViewModel<*>> SearchTextField(
    modifier: Modifier = Modifier,
    label: String,
    autofillType: AutofillType? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    noResultsFoundLabel: String = stringResource(R.string.label_no_results_found),
    selectedItem: String? = null,
    viewModel: T,
    onItemSelected: (Any) -> Unit
) {
    // Collect the current search text, search results, and search status from the ViewModel
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResult.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // Collect and remember the filtered search results
    var filteredResults by remember { mutableStateOf<List<String>>(emptyList()) }

    // Update the filteredResults when searchResults changes
    LaunchedEffect(searchResults) {
        filteredResults = viewModel.stringResults()
    }

    // Remember the selected item value to avoid recomposition on every change
    val selectedItemValue = remember(selectedItem) {
        selectedItem?.let { viewModel.setSelectedValue(it) }
        selectedItem
    }

    val focusManager = LocalFocusManager.current
    var focusedState by remember { mutableStateOf(false) }
    val expanded by remember(
        focusedState,
        searchText
    ) { mutableStateOf(focusedState && searchText.isNotBlank()) }

    DropdownListFieldStack(
        textField = {
            SdkTextField(
                modifier = modifier.onFocusChanged {
                    focusedState = it.isFocused
                },
                value = searchText,
                label = label,
                trailingIcon = {
                    // If focused, only show progress indicator
                    if (focusedState) {
                        // If searching, then show searching indicator
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(25.dp))
                        }
                    }
                    // Else if we have set a custom trailing icon and do not have focus then show custom icon
                    else if (trailingIcon != null) {
                        trailingIcon()
                    }
                },
                onValueChange = { value ->
                    // Update the search text in the ViewModel
                    viewModel.onSearchTextChange(value)
                },
                leadingIcon = leadingIcon,
                autofillType = autofillType,
                // Use keyboard options and actions for a user-friendly input experience
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // Handling the "Search" action within the input field
                        if (searchResults.isNotEmpty()) {
                            onItemSelected(searchResults.firstNotNullOf { it })
                            focusManager.clearFocus()
                        }
                    }
                )
            )
        },
        dropdownMenu = { boxWidth, _ ->
            val isNotClickable = searchText.isNotBlank() && filteredResults.isEmpty()
            val dropdownContent = if (isNotClickable) {
                listOf(
                    if (isSearching) {
                        stringResource(R.string.label_searching)
                    } else {
                        noResultsFoundLabel
                    }
                )
            } else {
                filteredResults
            }
            // Display the dropdown menu
            SdkDropDownMenu(
                modifier = Modifier
                    .testTag("searchResultsDropDown")
                    .width(boxWidth),
                itemWidth = boxWidth,
                expanded = expanded,
                items = dropdownContent,
                dismissOnClickOutside = false,
                isClickEnabled = !isNotClickable, // Enable clicks if results are available
                onItemSelected = { item ->
                    // Update the selected item only when a different item is selected
                    if (item != selectedItemValue) {
                        viewModel.setSelectedValue(item)
                        onItemSelected(item)
                    }
                    focusManager.clearFocus()
                }, onDismissed = {
                    // Clear search text when dismissing the dropdown
                    viewModel.onSearchTextChange("")
                }
            )
        }
    )
}
