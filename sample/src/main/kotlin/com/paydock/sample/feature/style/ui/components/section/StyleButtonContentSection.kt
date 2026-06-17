package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.StringDropdown
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun StyleButtonContentSection(
    contentText: String?,
    onContentTextChange: (String) -> Unit,
    selectedIcon: ButtonIcon?,
    onIconChange: (ButtonIcon?) -> Unit,
    defaultIcon: ButtonIcon? = null,
    iconDescription: String? = null,
    onIconDescriptionChange: (String) -> Unit = {},
    clickableDescription: String? = null,
    onClickableDescriptionChange: (String) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Button Text Input
        SectionContainer(title = stringResource(R.string.label_button_text)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = contentText ?: "",
                onValueChange = onContentTextChange,
                singleLine = true,
                maxLines = 1,
                placeholder = { Text(text = stringResource(R.string.placeholder_enter_button_text)) },
                trailingIcon = {
                    if (!contentText.isNullOrEmpty()) {
                        IconButton(onClick = {
                            onContentTextChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
        }

        HorizontalDivider()

        // Button Icon Selection
        SectionContainer(title = stringResource(R.string.label_button_icon_asset)) {
            // Remember icon options to maintain stable references for equality checks
            val iconOptions = remember(defaultIcon) {
                buildList {
                    add("None" to null)
                    if (defaultIcon != null) {
                        add("Default (SDK)" to defaultIcon)
                    }
                    add("Add" to ButtonIcon.Vector(Icons.Filled.Add))
                    add("Card" to ButtonIcon.Vector(Icons.Filled.CreditCard))
                    add("Check" to ButtonIcon.Vector(Icons.Filled.Check))
                    add("Close" to ButtonIcon.Vector(Icons.Filled.Close))
                    add("Favorite" to ButtonIcon.Vector(Icons.Filled.Favorite))
                    add("Forward" to ButtonIcon.Vector(Icons.AutoMirrored.Filled.ArrowForward))
                    add("Home" to ButtonIcon.Vector(Icons.Filled.Home))
                    add("Link" to ButtonIcon.Vector(Icons.Filled.Link))
                    add("Refresh" to ButtonIcon.Vector(Icons.Filled.Refresh))
                }
            }

            val selectedLabel = when (selectedIcon) {
                null -> {
                    iconOptions.firstOrNull { it.first.equals("None", ignoreCase = true) }?.first
                        ?: iconOptions.firstOrNull()?.first.orEmpty()
                }

                else -> {
                    val matchingOption = iconOptions.firstOrNull { it.second == selectedIcon }
                    matchingOption?.first ?: iconOptions.firstOrNull()?.first.orEmpty()
                }
            }

            StringDropdown(
                modifier = Modifier.fillMaxWidth(),
                options = iconOptions.map { it.first },
                selectedOption = selectedLabel,
                onOptionSelected = { selected ->
                    val newIcon = iconOptions.firstOrNull { it.first == selected }?.second
                    onIconChange(newIcon)
                },
                itemContent = { label, _ ->
                    val icon = iconOptions.firstOrNull { it.first == label }?.second
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            icon is ButtonIcon.Vector -> SdkIcon(
                                imageVector = icon.icon,
                                contentDescription = null
                            )

                            label == "Default (SDK)" -> SdkIcon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "SDK Default Icon"
                            )

                            else -> { /* no preview for null icons */
                            }
                        }
                        Text(text = label)
                    }
                },
                selectedContent = {
                    when {
                        selectedIcon is ButtonIcon.Vector -> SdkIcon(
                            imageVector = selectedIcon.icon,
                            contentDescription = null
                        )

                        selectedIcon == defaultIcon && defaultIcon != null -> SdkIcon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "SDK Default Icon"
                        )

                        else -> null // Don't show any icon for "None" or null selections
                    }
                }
            )
        }

        HorizontalDivider()

        SectionContainer(title = stringResource(R.string.label_more_options)) {
            TextField(
                label = stringResource(R.string.label_icon_description),
                value = iconDescription ?: "",
                onValueChange = onIconDescriptionChange
            )

            TextField(
                label = stringResource(R.string.label_clickable_description),
                value = clickableDescription ?: "",
                onValueChange = onClickableDescriptionChange
            )
        }
    }
}
