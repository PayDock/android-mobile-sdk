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

package com.paydock.designsystems.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.paydock.R
import com.paydock.designsystems.theme.Theme

/**
 * Composable function to display a modal bottom sheet with customizable content.
 *
 * This may require declared in the Activity, otherwise the [ModalBottomSheet] may be obscured by WindowInsets
 * Example: (Navigation Bar, Status Bar etc)
 * @see { WindowCompat.setDecorFitsSystemWindows(window, false) }
 *
 * @param allowFullScreen Flag indicating whether the bottom sheet can take up the full screen.
 * @param bottomSheetState The state of the bottom sheet to control its visibility and position.
 * @param onDismissRequest Callback to be invoked when the bottom sheet is dismissed.
 * @param content The content of the bottom sheet provided as a composable function.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdkBottomSheet(
    containerColor: Color = Theme.colors.background,
    allowFullScreen: Boolean = false,
    bottomSheetState: SheetState,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    // Set window insets to customize the space taken by the bottom sheet
    val windowInsets = if (allowFullScreen) {
        WindowInsets(0)
    } else {
        BottomSheetDefaults.windowInsets
    }

    // Define modifier based on whether full screen is allowed or not
    val modifier = if (allowFullScreen) {
        Modifier.fillMaxSize()
    } else
        Modifier.fillMaxWidth()

    // Display a modal bottom sheet with customizable content
    ModalBottomSheet(
        modifier = modifier,
        containerColor = containerColor,
        windowInsets = windowInsets,
        dragHandle = null,
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState
    ) {
        // Column to structure content within the bottom sheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button at the top-right corner
            IconButton(modifier = Modifier.align(Alignment.End), onClick = onDismissRequest) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_circle),
                    contentDescription = null
                )
            }
            // Content provided by the caller
            content()
        }
    }
}
