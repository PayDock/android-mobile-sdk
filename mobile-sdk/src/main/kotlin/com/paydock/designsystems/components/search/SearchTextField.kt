package com.paydock.designsystems.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.R
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults

/**
 * A composable function that creates a search text field with a dropdown for suggestions.
 *
 * @param modifier The modifier to be applied to the search text field.
 * @param label The label to be displayed on the search text field.
 * @param autofillType The autofill type for the search text field (optional).
 * @param noResultsFoundLabel The label to be displayed when no results are found (defaults to "No results found").
 * @param selectedItem The currently selected item in the dropdown (optional).
 * @param viewModel The [SearchViewModel] instance responsible for managing search operations.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun <T : SearchViewModel<*>> SearchTextField(
    modifier: Modifier = Modifier,
    appearance: SearchDropdownAppearance = SearchDropdownAppearanceDefaults.appearance(),
    label: String,
    autofillType: AutofillType? = null,
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
            appearance = appearance.textField,
            value = searchText,
            label = label,
            showValidIcon = !selectedItem.isNullOrBlank(),
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
            )
        )

        // Refactor the logic in a val to make the code more readable
        val dropdownContent =
            getDropdownContent(isSearching, noResultsFoundLabel, searchText, filteredResults)

        AnimatedVisibility(visible = isDropdownExpanded) {
            Card(
                shape = appearance.dropdown.shape,
                elevation = CardDefaults.elevatedCardElevation()
            ) {
                // Calculate the height based on items.
                val dropdownHeight = appearance.dropdown.itemHeight * dropdownContent.size
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = dropdownHeight)
                        .onFocusChanged {
                            isDropdownFocused = it.hasFocus
                        },
                ) {
                    items(dropdownContent.sorted()) { item ->
                        DropdownItem(item, appearance.dropdown) { selection ->
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
 * Represents the visual appearance configuration for a search dropdown component.
 *
 * This class combines the appearance settings for the text field where the user types
 * and the dropdown list that displays search results. It allows for consistent
 * styling of both parts of the search dropdown.
 */
@Immutable
class SearchDropdownAppearance(
    val textField: TextFieldAppearance,
    val dropdown: DropdownAppearance
) {
    /**
     * Creates a copy of this SearchDropdownAppearance with optional overrides for its properties.
     *
     * @param textField The [TextFieldAppearance] to use for the copy. Defaults to the current textField.
     * @param dropdown The [DropdownAppearance] to use for the copy. Defaults to the current dropdown.
     * @return A new [SearchDropdownAppearance] instance that is a copy of this one.
     */
    fun copy(
        textField: TextFieldAppearance = this.textField,
        dropdown: DropdownAppearance = this.dropdown
    ): SearchDropdownAppearance = SearchDropdownAppearance(
        textField.copy(),
        dropdown.copy()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchDropdownAppearance

        if (textField != other.textField) return false
        if (dropdown != other.dropdown) return false

        return true
    }

    override fun hashCode(): Int {
        var result = textField.hashCode()
        result = 31 * result + dropdown.hashCode()
        return result
    }
}

/**
 * Represents the visual appearance configuration for a dropdown component.
 *
 * This class encapsulates properties that control how a dropdown and its items are rendered,
 * such as the overall shape of the dropdown, the padding and height of individual items,
 * and the text appearance of the items.
 *
 * @property shape The shape of the dropdown container.
 * @property itemPadding The padding to be applied around each dropdown item's content.
 * @property itemHeight The preferred height for each dropdown item.
 * @property item The text appearance configuration for the text displayed within each dropdown item.
 */
@Immutable
class DropdownAppearance(
    val shape: Shape,
    val itemPadding: PaddingValues,
    val itemHeight: Dp,
    val item: TextAppearance
) {
    /**
     * Creates a copy of the [DropdownAppearance] with optional overrides for its properties.
     *
     * This function allows you to create a new [DropdownAppearance] instance based on an existing one,
     * potentially changing some of its attributes. If a parameter is not provided, the value from
     * the original `DropdownAppearance` instance will be used.
     *
     * @param shape The shape of the dropdown container. Defaults to the current shape.
     * @param itemPadding The padding around each item in the dropdown. Defaults to the current item padding.
     * @param itemHeight The height of each item in the dropdown. Defaults to the current item height.
     *                   If the provided `itemHeight` is not valid (e.g., [Dp.Unspecified]), the current
     *                   `itemHeight` will be used instead.
     * @param item The text appearance for the items in the dropdown. Defaults to a copy of the current item appearance.
     * @return A new [DropdownAppearance] instance with the specified or default properties.
     */
    fun copy(
        shape: Shape = this.shape,
        itemPadding: PaddingValues = this.itemPadding,
        itemHeight: Dp = this.itemHeight,
        item: TextAppearance = this.item
    ): DropdownAppearance = DropdownAppearance(
        shape = shape,
        itemPadding = itemPadding,
        itemHeight = itemHeight.takeOrElse { this.itemHeight },
        item = item.copy()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DropdownAppearance

        if (shape != other.shape) return false
        if (itemPadding != other.itemPadding) return false
        if (itemHeight != other.itemHeight) return false
        if (item != other.item) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + itemPadding.hashCode()
        result = 31 * result + itemHeight.hashCode()
        result = 31 * result + item.hashCode()
        return result
    }
}

/**
 * Default appearance settings for the Search Dropdown component.
 *
 * This object provides a default [SearchDropdownAppearance] which combines default settings
 * for the inner components: a [TextFieldAppearance] for the search input field and a
 * [DropdownAppearance] for the dropdown list of results.
 */
object SearchDropdownAppearanceDefaults {

    /**
     * Defines the visual appearance of the search dropdown component.
     *
     * This composable function creates and returns a [SearchDropdownAppearance] object,
     * which encapsulates the appearance settings for both the text field input and the
     * dropdown list.
     *
     * The text field appearance is based on the default `TextFieldAppearanceDefaults.appearance()`
     * but with the `singleLine` property explicitly set to `true`, ensuring that the text field
     * displays its content on a single line.
     *
     * The dropdown list appearance uses the default `DropdownAppearanceDefaults.appearance()`.
     *
     * @return A [SearchDropdownAppearance] object defining the appearance of the search dropdown.
     */
    @Composable
    fun appearance(): SearchDropdownAppearance = SearchDropdownAppearance(
        textField = TextFieldAppearanceDefaults.appearance().copy(singleLine = true),
        dropdown = DropdownAppearanceDefaults.appearance(),
    )
}

/**
 * Default appearance settings for a dropdown menu.
 *
 * This object provides a convenient way to access a default set of appearance
 * properties for dropdown menus. It includes settings for the overall shape,
 * padding for individual items, the height of each item, and the text appearance
 * of the items.
 *
 * To use these defaults, call the `appearance()` composable function.
 */
object DropdownAppearanceDefaults {

    private val ItemHorizontalPadding = 15.dp
    private val ItemVerticalPadding = 15.dp
    private val ItemHeight = 50.dp

    /**
     * Defines the appearance settings for the dropdown menu.
     *
     * This composable function creates and returns a [DropdownAppearance] object, which
     * encapsulates various styling properties for rendering the dropdown menu.
     *
     * The default appearance includes:
     * - A rectangular shape for the dropdown container.
     * - Specific horizontal and vertical padding for individual menu items.
     * - A fixed height for each menu item.
     * - Text appearance for menu items based on [TextAppearanceDefaults.appearance],
     *   with the text style overridden to [MaterialTheme.typography.bodyMedium].
     *
     * Developers can customize the appearance by providing different values
     * or by creating their own [DropdownAppearance] object.
     *
     * @return A [DropdownAppearance] object containing the styling properties for the dropdown.
     */
    @Composable
    fun appearance(): DropdownAppearance = DropdownAppearance(
        shape = RectangleShape,
        itemPadding = PaddingValues(
            horizontal = ItemHorizontalPadding,
            vertical = ItemVerticalPadding
        ),
        itemHeight = ItemHeight,
        item = TextAppearanceDefaults.appearance().copy(
            style = MaterialTheme.typography.bodyMedium,
        )
    )
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
    appearance: DropdownAppearance = DropdownAppearanceDefaults.appearance(),
    onItemSelected: (Any) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onItemSelected(item)
            }
            .padding(appearance.itemPadding)
    ) {
        SdkText(
            modifier = Modifier.fillMaxWidth(),
            text = AnnotatedString(text = item),
            appearance = appearance.item
        )
    }
}