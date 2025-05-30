package com.paydock.designsystems.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.extensions.alpha20
import com.paydock.core.presentation.extensions.alpha40
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.theme.Theme

/**
 * A composable function that creates a search text field with a dropdown for suggestions.
 *
 * @param modifier The modifier to be applied to the search text field.
 * @param label The label to be displayed on the search text field.
 * @param autofillType The autofill type for the search text field (optional).
 * @param leadingIcon The composable function to render the leading icon (optional).
 * @param trailingIcon The composable function to render the trailing icon (optional).
 * @param noResultsFoundLabel The label to be displayed when no results are found (defaults to "No results found").
 * @param selectedItem The currently selected item in the dropdown (optional).
 * @param viewModel The [SearchViewModel] instance responsible for managing search operations.
 */
@OptIn(ExperimentalComposeUiApi::class)
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

    val focusManager: FocusManager = LocalFocusManager.current
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var isDropdownFocused by remember { mutableStateOf(false) }

    // Use a derived state
    val isDropdownExpanded by remember(isTextFieldFocused, isDropdownFocused, searchText) {
        mutableStateOf((isTextFieldFocused || isDropdownFocused) && searchText.isNotBlank())
    }

    Column(modifier = modifier) {
        SdkTextField(
            modifier = Modifier.onFocusChanged { focusState ->
                isTextFieldFocused = focusState.isFocused
            },
            value = searchText,
            label = label,
            trailingIcon = {
                // Show progress indicator if searching and focused
                if (isTextFieldFocused && isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(25.dp))
                } else if (!isTextFieldFocused && trailingIcon != null) {
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
                    if (searchResults.isNotEmpty()) {
                        searchResults.first()?.let { onItemSelected(it) } // Handle non-null better
                        focusManager.clearFocus()
                    }
                }
            ),
            singleLine = true
        )

        // Refactor the logic in a val to make the code more readable
        val dropdownContent =
            getDropdownContent(isSearching, noResultsFoundLabel, searchText, filteredResults)

        AnimatedVisibility(visible = isDropdownExpanded) {
            Card(
                modifier = Modifier.background(Theme.colors.primary.alpha40),
                shape = RoundedCornerShape(0.dp)
            ) {
                // Calculate the height based on items.
                val dropdownHeight = 50.dp * dropdownContent.size
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = dropdownHeight)
                        .onFocusChanged {
                            isDropdownFocused = it.hasFocus
                        },
                ) {
                    items(dropdownContent.sorted()) { item ->
                        DropdownItem(item) { selection ->
                            if (selection != selectedItemValue) {
                                viewModel.setSelectedValue(item)
                                onItemSelected(selection)
                            }
                            focusManager.clearFocus()
                        }
                    }
                }
            }
        }
    }
}

/**
 *  Generates the content for a dropdown list based on the search state and filtered results.
 *
 *  If the user has entered a search text but no results are found, the dropdown will display either
 *  a "Searching..." label (if `isSearching` is true) or a custom "No results found" label.  Otherwise,
 *  the dropdown will display the list of `filteredResults`.
 *
 *  @param isSearching  Boolean indicating whether a search operation is currently in progress.
 *  Affects the label shown when no results are found.
 *  @param noResultsFoundLabel  String to display when no search results are found and `isSearching` is false.
 *  @param searchText  The current search text entered by the user.
 *  @param filteredResults  The list of strings that match the current search text.
 *  @return A list of strings representing the content to be displayed in the dropdown list.
 *  This will be either a list containing a single string (indicating search status or no results)
 *  or the `filteredResults` list.
 */
@Composable
private fun getDropdownContent(
    isSearching: Boolean,
    noResultsFoundLabel: String,
    searchText: String,
    filteredResults: List<String>
): List<String> {
    return if (searchText.isNotBlank() && filteredResults.isEmpty()) {
        listOf(if (isSearching) stringResource(R.string.label_searching) else noResultsFoundLabel)
    } else {
        filteredResults
    }
}

/**
 * Composable function that renders a single item in a dropdown list.
 *
 * @param item The string value of the dropdown item to display.
 * @param onItemSelected A callback function that is invoked when the item is selected.
 * It receives the selected item's value (as `Any`) as a parameter,  presumably for
 * handling side effects like updating a parent view's state or triggering other actions
 * based on the selection.  It's crucial that this callback aligns with the type expected
 * by the ViewModel's `setSelectedValue` function.
 */
@Composable
private fun DropdownItem(
    item: String,
    onItemSelected: (Any) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme.colors.primary.alpha20)
            .clickable {
                onItemSelected(item)
            }
            .padding(vertical = 15.dp, horizontal = 15.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = AnnotatedString(text = item),
            style = Theme.typography.body1
        )
    }
}