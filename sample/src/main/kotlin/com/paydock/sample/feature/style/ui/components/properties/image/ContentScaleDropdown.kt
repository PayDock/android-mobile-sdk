package com.paydock.sample.feature.style.ui.components.properties.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.paydock.sample.R
import com.paydock.sample.feature.style.ui.components.core.dropdown.DropdownSelector

@Composable
fun ContentScaleDropdown(
    modifier: Modifier = Modifier,
    currentContentScale: ContentScale,
    onContentScaleChange: (ContentScale) -> Unit
) {
    val options = remember {
        listOf(
            "Fit" to ContentScale.Fit,
            "FillWidth" to ContentScale.FillWidth,
            "FillHeight" to ContentScale.FillHeight,
            "Crop" to ContentScale.Crop,
            "Inside" to ContentScale.Inside,
            "FillBounds" to ContentScale.FillBounds,
            "None" to ContentScale.None,
        )
    }

    val selectedOptionString = remember(currentContentScale) {
        options.find { it.second == currentContentScale }?.first ?: ContentScale.Fit.toString()
    }

    DropdownSelector(
        modifier = modifier,
        title = stringResource(R.string.label_content_scale),
        options = options.map { it.first },
        selectedOption = selectedOptionString,
        onOptionSelected = { newValueString ->
            val newContentScale =
                options.find { it.first == newValueString }?.second ?: ContentScale.Fit
            onContentScaleChange(newContentScale)
        }
    )
}