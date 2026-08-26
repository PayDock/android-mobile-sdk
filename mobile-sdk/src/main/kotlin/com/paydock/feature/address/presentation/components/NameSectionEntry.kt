package com.paydock.feature.address.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.core.WidgetDefaults

/**
 * A composable function that displays a section for entering first and last names.
 * It adapts its layout based on the current font scale. For larger font scales,
 * the input fields are displayed in a column; otherwise, they are displayed in a row.
 *
 * @param firstName The current value of the first name.
 * @param lastName The current value of the last name.
 * @param onFirstNameChange A callback function invoked when the first name input changes.
 * @param onLastNameChange A callback function invoked when the last name input changes.
 * @param verticalSpacing The vertical spacing between elements in the column layout. Defaults to [WidgetDefaults.Spacing].
 * @param horizontalSpacing The horizontal spacing between elements in the row layout. Defaults to [WidgetDefaults.Spacing].
 * @param titleAppearance The appearance style for the section title text. Defaults to [TextAppearanceDefaults.appearance].
 * @param textFieldAppearance The appearance style for the input text fields. Defaults to [TextFieldAppearanceDefaults.appearance].
 */
@Suppress("LongParameterList")
@Composable
internal fun NameSectionEntry(
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    horizontalSpacing: Dp = WidgetDefaults.Spacing,
    titleAppearance: TextAppearance = TextAppearanceDefaults.appearance(),
    firstNameAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    lastNameAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    firstNameForceShowErrors: Boolean = false,
    lastNameForceShowErrors: Boolean = false,
    a11yFirstNameFocus: Boolean = false,
    a11yLastNameFocus: Boolean = false,
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    val focusLastName = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.Start
    ) {
        SdkText(
            modifier = Modifier.fillMaxWidth(),
            appearance = titleAppearance,
            text = stringResource(R.string.label_name),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // For Name fields: FirstName (50% weight) + LastName (50% weight) + spacing
            // Each field needs ~150dp minimum for typical names + padding
            val shouldUseColumnLayout = WidgetDefaults.shouldUseColumnLayout(
                availableWidth = maxWidth,
                fontScale = fontScale,
                minFieldWidth = 150.dp, // Name field minimum
                fieldWeight = 0.5f // Each field weight (equal split)
            )

            if (shouldUseColumnLayout) {
                NameInputDetailsColumn(
                    verticalSpacing = verticalSpacing,
                    firstNameAppearance = firstNameAppearance,
                    lastNameAppearance = lastNameAppearance,
                    firstName = firstName,
                    lastName = lastName,
                    focusLastName = focusLastName,
                    firstNameForceShowErrors = firstNameForceShowErrors,
                    lastNameForceShowErrors = lastNameForceShowErrors,
                    a11yFirstNameFocus = a11yFirstNameFocus,
                    a11yLastNameFocus = a11yLastNameFocus,
                    onFirstNameChange = onFirstNameChange,
                    onLastNameChange = onLastNameChange
                )
            } else {
                NameInputDetailsRow(
                    horizontalSpacing = horizontalSpacing,
                    firstNameAppearance = firstNameAppearance,
                    lastNameAppearance = lastNameAppearance,
                    firstName = firstName,
                    lastName = lastName,
                    focusLastName = focusLastName,
                    firstNameForceShowErrors = firstNameForceShowErrors,
                    lastNameForceShowErrors = lastNameForceShowErrors,
                    a11yFirstNameFocus = a11yFirstNameFocus,
                    a11yLastNameFocus = a11yLastNameFocus,
                    onFirstNameChange = onFirstNameChange,
                    onLastNameChange = onLastNameChange
                )
            }
        }
    }
}

/**
 * A private composable function that displays the first and last name input fields in a column layout.
 * This layout is typically used for larger font scales where a row layout might be too cramped.
 *
 * @param verticalSpacing The vertical spacing between the input fields. Defaults to [WidgetDefaults.Spacing].
 * @param appearance The appearance style for the input text fields. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param firstName The current value of the first name.
 * @param lastName The current value of the last name.
 * @param focusLastName A [FocusRequester] used to programmatically move focus to the last name field.
 * @param onFirstNameChange A callback function invoked when the first name input changes.
 * @param onLastNameChange A callback function invoked when the last name input changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongParameterList")
@Composable
private fun NameInputDetailsColumn(
    verticalSpacing: Dp = WidgetDefaults.Spacing,
    firstNameAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    lastNameAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    firstName: String,
    lastName: String,
    focusLastName: FocusRequester,
    firstNameForceShowErrors: Boolean = false,
    lastNameForceShowErrors: Boolean = false,
    a11yFirstNameFocus: Boolean = false,
    a11yLastNameFocus: Boolean = false,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.Start
    ) {
        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firstName1Input"),
            appearance = firstNameAppearance,
            value = firstName,
            label = stringResource(R.string.label_first_name),
            nextFocus = focusLastName,
            autofillType = ContentType.PersonFirstName,
            forceShowErrors = firstNameForceShowErrors,
            a11yFocus = a11yFirstNameFocus,
            onValueUpdated = onFirstNameChange
        )

        AddressInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusLastName)
                .testTag("lastNameInput"),
            appearance = lastNameAppearance,
            value = lastName,
            label = stringResource(R.string.label_last_name),
            autofillType = ContentType.PersonLastName,
            forceShowErrors = lastNameForceShowErrors,
            a11yFocus = a11yLastNameFocus,
            onValueUpdated = onLastNameChange
        )
    }
}

/**
 * A private composable function that displays the first and last name input fields in a row layout.
 * This layout is typically used for standard font scales.
 *
 * @param horizontalSpacing The horizontal spacing between the input fields. Defaults to [WidgetDefaults.Spacing].
 * @param appearance The appearance style for the input text fields. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param firstName The current value of the first name.
 * @param lastName The current value of the last name.
 * @param focusLastName A [FocusRequester] used to programmatically move focus to the last name field.
 * @param onFirstNameChange A callback function invoked when the first name input changes.
 * @param onLastNameChange A callback function invoked when the last name input changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongParameterList")
@Composable
private fun NameInputDetailsRow(
    horizontalSpacing: Dp = WidgetDefaults.Spacing,
    firstNameAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    lastNameAppearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    firstName: String,
    lastName: String,
    focusLastName: FocusRequester,
    firstNameForceShowErrors: Boolean = false,
    lastNameForceShowErrors: Boolean = false,
    a11yFirstNameFocus: Boolean = false,
    a11yLastNameFocus: Boolean = false,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            horizontalSpacing,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.Top
    ) {
        AddressInputField(
            modifier = Modifier
                .weight(0.5f)
                .testTag("firstName1Input"),
            appearance = firstNameAppearance,
            value = firstName,
            label = stringResource(R.string.label_first_name),
            nextFocus = focusLastName,
            autofillType = ContentType.PersonFirstName,
            forceShowErrors = firstNameForceShowErrors,
            a11yFocus = a11yFirstNameFocus,
            onValueUpdated = onFirstNameChange
        )

        AddressInputField(
            modifier = Modifier
                .weight(0.5f)
                .focusRequester(focusLastName)
                .testTag("lastNameInput"),
            appearance = lastNameAppearance,
            value = lastName,
            label = stringResource(R.string.label_last_name),
            autofillType = ContentType.PersonLastName,
            forceShowErrors = lastNameForceShowErrors,
            a11yFocus = a11yLastNameFocus,
            onValueUpdated = onLastNameChange
        )
    }
}