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

package com.paydock.sample.feature.style.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.ThemeColors
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.feature.style.models.ColourTheme
import com.paydock.sample.feature.style.ui.components.picker.ColourPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColoursSection(
    colorTheme: ThemeColors.ThemeColor,
    onColorUpdated: (ThemeColors.ThemeColor) -> Unit
) {
    // Creates list of actual theming colours to show
    val colourThemeItems = remember {
        mutableStateListOf(
            ColourTheme("Primary", colorTheme.primary),
            ColourTheme("On Primary", colorTheme.onPrimary),
            ColourTheme("Text", colorTheme.text),
            ColourTheme("Placeholder", colorTheme.placeholder),
            ColourTheme("Success", colorTheme.success),
            ColourTheme("Error", colorTheme.error),
            ColourTheme("Background", colorTheme.background)
        )
    }
    // Handles selected item for updating colour
    var selectedItem by remember { mutableStateOf<ColourTheme?>(null) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        // This will expand the modal fully based on the size
        skipPartiallyExpanded = true
    )

    // Function to update a color theme item
    fun updateColorThemeItem(updatedItem: ColourTheme): ThemeColors.ThemeColor {
        // Update the specific color in the ThemeColors.ThemeColor object
        val updatedThemeColor = when (updatedItem.themeName) {
            "Primary" -> colorTheme.copy(primary = updatedItem.color)
            "On Primary" -> colorTheme.copy(onPrimary = updatedItem.color)
            "Text" -> colorTheme.copy(text = updatedItem.color)
            "Placeholder" -> colorTheme.copy(placeholder = updatedItem.color)
            "Success" -> colorTheme.copy(success = updatedItem.color)
            "Error" -> colorTheme.copy(error = updatedItem.color)
            "Background" -> colorTheme.copy(background = updatedItem.color)
            "Outline" -> colorTheme.copy(outline = updatedItem.color)
            else -> colorTheme
        }
        return updatedThemeColor
    }

    SectionContainer(title = stringResource(R.string.label_colours)) {
        colourThemeItems.forEach { theme ->
            ColourSelectionField(
                modifier = Modifier.fillMaxWidth(),
                colourTheme = ColourTheme(theme.themeName, theme.color),
                onItemClicked = { updatedItem ->
                    scope.launch {
                        selectedItem = updatedItem
                        showBottomSheet = true
                        //
                    }
                }
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            containerColor = Color.White,
            // This is to show above device navigation
            windowInsets = WindowInsets(bottom = 30.dp),
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            // Sheet content
            selectedItem?.let {
                ColourPicker(color = it.color, onColourUpdated = { color ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedItem = selectedItem?.copy(color = color)
                        // Updates the color theme component
                        val updatedColorTheme =
                            colourThemeItems.find { theme -> theme.themeName == selectedItem?.themeName }
                                ?.apply {
                                    this.color = color
                                }
                        // Updates the color theme as a whole
                        updatedColorTheme?.let { colorTheme ->
                            onColorUpdated(
                                updateColorThemeItem(
                                    colorTheme
                                )
                            )
                        }
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }, onCanceled = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                })
            }
        }
    }
}

sealed class Header {
    data class HeaderPlain(val titleText: String) : Header()
    data class HeaderImage(val titleText: String, val imageResourceId: Int?) : Header()
}

sealed class Content {
    data class Center(val valueText: String) : Content()
    data class Left(val valueText: String) : Content()
}

sealed class Footer {
    object Plain : Footer()
    sealed class ButtonSingle : Footer() {
        data class NegativeButton(
            val negativeBtnLabel: String,
            val onClickNegative: (() -> Unit)?
        ) : Footer()

        data class PositiveButton(
            val positiveBtnLabel: String,
            val onClickPositive: (() -> Unit)?
        ) : Footer()
    }

    data class ButtonMultiple(
        val negativeBtnLabel: String,
        val onClickNegative: (() -> Unit)?,
        val positiveBtnLabel: String,
        val onClickPositive: (() -> Unit)?
    ) : Footer()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetTest(
    header: Header,
    content: Content,
    footer: Footer
) {
    val coroutine = rememberCoroutineScope()
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            skipPartiallyExpanded = true,
            initialValue = SheetValue.Hidden
        )
    )
    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.padding(0.dp),
        sheetContent = {
            BottomSheetContent(
                BottomSheetState(header, content, footer),
                coroutine,
                sheetState.bottomSheetState
            )
        }
    ) {
        MainScreen(coroutine, sheetState.bottomSheetState)
    }
}

class BottomSheetState(
    header: Header,
    content: Content,
    footer: Footer
) {
    var imageResourceId: Int? = null
        private set
    var titleText = ""
        private set
    var valueText = ""
        private set
    var negativeLabel = ""
        private set
    var positiveLabel = ""
        private set
    var negativeButton = false
        private set
    var positiveButton = false
        private set
    var onClickNegative: (() -> Unit)? = null
        private set
    var onClickPositive: (() -> Unit)? = null
        private set
    var alignValue = Alignment.CenterHorizontally
        private set

    init {
        when (header) {
            is Header.HeaderPlain -> {
                titleText = header.titleText
            }

            is Header.HeaderImage -> {
                titleText = header.titleText
                imageResourceId = header.imageResourceId
            }
        }

        when (content) {
            is Content.Center -> {
                valueText = content.valueText
                alignValue = Alignment.CenterHorizontally
            }

            is Content.Left -> {
                valueText = content.valueText
                alignValue = Alignment.Start
            }
        }

        when (footer) {
            is Footer.ButtonSingle.NegativeButton -> {
                negativeButton = true
                negativeLabel = footer.negativeBtnLabel
                onClickNegative = footer.onClickNegative
            }

            is Footer.ButtonSingle.PositiveButton -> {
                positiveButton = true
                positiveLabel = footer.positiveBtnLabel
                onClickPositive = footer.onClickPositive
            }

            is Footer.ButtonMultiple -> {
                negativeButton = true
                positiveButton = true
                negativeLabel = footer.negativeBtnLabel
                positiveLabel = footer.positiveBtnLabel
                onClickNegative = footer.onClickNegative
                onClickPositive = footer.onClickPositive
            }

            Footer.Plain -> TODO()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
    state: BottomSheetState,
    coroutineScope: CoroutineScope,
    sheetState: SheetState
) {
    val configuration = LocalConfiguration.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                (configuration.screenHeightDp * 0.25).dp,
                (configuration.screenHeightDp * 0.75).dp
            )
            .wrapContentWidth(unbounded = false)
            .wrapContentHeight(unbounded = true)
            .padding(24.dp, 24.dp, 24.dp, 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = state.alignValue
        ) {
            state.imageResourceId?.let {
                header(state.imageResourceId!!)
            }
            Spacer(modifier = Modifier.height(24.dp))

            content(state.titleText, state.valueText)

            Spacer(modifier = Modifier.height(24.dp))

            footer(
                state.negativeLabel,
                state.negativeButton,
                state.positiveLabel,
                state.positiveButton,
                state.onClickNegative,
                state.onClickPositive,
                coroutineScope,
                sheetState
            )
        }
    }
}

@Composable
fun header(
    imageResourceId: Int
) {
    Box(
        modifier = Modifier
    ) {
        Image(painter = painterResource(id = imageResourceId), contentDescription = "Image")
    }
}

@Composable
fun content(
    titleText: String,
    valueText: String
) {
    Text(text = titleText)
    Spacer(modifier = Modifier.height(24.dp))
    Text(text = valueText)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun footer(
    negativeLabel: String,
    negativeButton: Boolean,
    positiveLabel: String,
    positiveButton: Boolean,
    onClickNegative: (() -> Unit)?,
    onClickPositive: (() -> Unit)?,
    coroutine: CoroutineScope,
    sheetState: SheetState
) {
    if (positiveButton) {
        Button(onClick = onClickPositive!!) {
            Text(text = positiveLabel)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    if (negativeButton) {
        OutlinedButton(
            onClick = {
                bottomSheetVisibility(coroutine, sheetState)
            }
        ) {
            Text(text = negativeLabel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(scope: CoroutineScope, state: SheetState) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            onClick = {
                scope.launch {
                    state.show()
                }
            }) {
            Text(text = "Open Modal Bottom Sheet Layout")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun bottomSheetVisibility(coroutineScope: CoroutineScope, sheetState: SheetState) {
    coroutineScope.launch {
        if (sheetState.currentValue == SheetValue.Hidden) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleBottomSheetScaffoldSample() {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Swipe up to expand sheet")
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sheet content")
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                    }
                ) {
                    Text("Click to collapse sheet")
                }
            }
        }) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            Text("Scaffold Content")
        }
    }
}