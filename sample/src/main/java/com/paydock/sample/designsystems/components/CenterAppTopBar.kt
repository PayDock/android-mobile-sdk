/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
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

package com.paydock.sample.designsystems.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterAppTopBar(
    title: String,
    showTitle: Boolean,
    modifier: Modifier = Modifier,
    onBackButtonClick: (() -> Unit)? = null,
) {
    if (showTitle) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    modifier = modifier,
                    text = title,
                    style = Theme.typography.navTitle,
                    color = Theme.colors.onPrimaryContainer,
                    fontWeight = FontWeight(590)
                )
            },
            modifier = modifier,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Theme.colors.primaryContainer,
                titleContentColor = Theme.colors.onPrimaryContainer
            ),
            navigationIcon = {
                onBackButtonClick?.let { clickListener ->
                    IconButton(onClick = { clickListener.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = Theme.colors.onPrimaryContainer
                        )
                    }
                }
            }
        )
    } else {
        TopAppBar(
            title = {
                Image(
                    painter = painterResource(id = R.drawable.demo_icon),
                    contentDescription = null
                )
            },
            modifier = modifier,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Theme.colors.primaryContainer,
                titleContentColor = Theme.colors.onPrimaryContainer
            ),
            actions = {
                IconButton(onClick = { /*No-Op*/ }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Theme.colors.onPrimaryContainer
                    )
                }
            },
            navigationIcon = {
                onBackButtonClick?.let { clickListener ->
                    IconButton(onClick = { clickListener.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = Theme.colors.onPrimaryContainer
                        )
                    }
                }
            }
        )
    }
}

@Composable
@Preview
private fun PreviewCenterAppTopBar() {
    SampleTheme {
        CenterAppTopBar(title = "Center Top Bar", showTitle = true)
    }
}

@Composable
@Preview
private fun PreviewCenterAppTopBarWithoutTitle() {
    SampleTheme {
        CenterAppTopBar(title = "Center Top Bar", showTitle = false)
    }
}

@Composable
@Preview
private fun PreviewCenterAppTopBarWithBackButton() {
    SampleTheme {
        CenterAppTopBar(
            title = "Center Top Bar",
            showTitle = true,
            onBackButtonClick = { }
        )
    }
}

@Composable
@Preview
private fun PreviewCenterAppTopBarWithoutTitleWithBackButton() {
    SampleTheme {
        CenterAppTopBar(
            title = "Center Top Bar",
            showTitle = false,
            onBackButtonClick = { }
        )
    }
}
