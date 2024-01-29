/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.address.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.search.SearchTextField
import com.paydock.feature.address.presentation.viewmodels.CountryAutoCompleteViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable for displaying a country autocomplete input.
 *
 * @param modifier Modifier to apply to the composable.
 * @param viewModel ViewModel for country autocomplete functionality.
 * @param selectedItem The pre-selected item to be displayed initially.
 * @param onCountrySelected Callback when a country is selected.
 */
@Composable
internal fun CountryInputAutoComplete(
    modifier: Modifier = Modifier,
    viewModel: CountryAutoCompleteViewModel = koinViewModel(),
    selectedItem: String = "",
    onCountrySelected: (String) -> Unit
) {
    SearchTextField(
        modifier = modifier.testTag("countrySearch"),
        label = stringResource(R.string.label_country),
        trailingIcon = {
            if (selectedItem.isNotBlank()) {
                InputValidIcon()
            } else {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.ArrowDropDown),
                    contentDescription = stringResource(id = R.string.content_desc_dropdown_arrow),
                )
            }
        },
        selectedItem = selectedItem,
        viewModel = viewModel,
        onItemSelected = {
            onCountrySelected(it as String)
        }
    )
}