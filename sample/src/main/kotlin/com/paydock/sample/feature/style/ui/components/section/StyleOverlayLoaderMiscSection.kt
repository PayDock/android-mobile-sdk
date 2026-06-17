package com.paydock.sample.feature.style.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.paydock.designsystems.components.loader.OverlayLoaderAppearance
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.NumberCounter
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.style.ui.components.core.color.ColorPickerField

private const val DEFAULT_CARD_DIMENSION = 150

@Composable
fun StyleOverlayLoaderMiscSection(
    currentAppearance: OverlayLoaderAppearance,
    onAppearanceChange: (OverlayLoaderAppearance) -> Unit
) {
    // cardSize is nullable: null = dynamic (card hugs content), non-null = fixed size. The mode
    // and the fixed dimensions are read straight back from the appearance.
    val cardSize = currentAppearance.cardSize
    val dynamicCardSize = cardSize == null
    val cardWidth = cardSize?.width?.value?.toInt() ?: DEFAULT_CARD_DIMENSION
    val cardHeight = cardSize?.height?.value?.toInt() ?: DEFAULT_CARD_DIMENSION

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorPickerField(
            label = stringResource(R.string.label_background_colour),
            currentColor = currentAppearance.backgroundColor,
            onColorChange = { newColor ->
                onAppearanceChange(currentAppearance.copy(backgroundColor = newColor))
            },
            modifier = Modifier.fillMaxWidth()
        )

        BooleanField(
            label = "Show Card",
            value = currentAppearance.showCard,
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(showCard = newValue))
            }
        )

        BooleanField(
            label = "Dynamic Card Size",
            value = dynamicCardSize,
            onValueChange = { newValue ->
                // null = dynamic; otherwise seed a fixed size from the current/default dimensions.
                val newSize = if (newValue) null else DpSize(cardWidth.dp, cardHeight.dp)
                onAppearanceChange(currentAppearance.copy(cardSize = newSize))
            }
        )

        if (!dynamicCardSize) {
            NumberCounter(
                title = stringResource(R.string.label_width),
                value = cardWidth,
                onValueChange = { newValue ->
                    onAppearanceChange(
                        currentAppearance.copy(cardSize = DpSize(newValue.dp, cardHeight.dp))
                    )
                }
            )

            NumberCounter(
                title = stringResource(R.string.label_height),
                value = cardHeight,
                onValueChange = { newValue ->
                    onAppearanceChange(
                        currentAppearance.copy(cardSize = DpSize(cardWidth.dp, newValue.dp))
                    )
                }
            )
        }

        NumberCounter(
            title = stringResource(R.string.label_size),
            value = currentAppearance.loaderSize.value.toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(loaderSize = newValue.dp))
            }
        )

        NumberCounter(
            title = stringResource(R.string.label_loader_spacing),
            value = currentAppearance.loaderSpacing.value.toInt(),
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(loaderSpacing = newValue.dp))
            }
        )

        TextField(
            label = stringResource(R.string.label_loader_text),
            value = currentAppearance.loaderText,
            onValueChange = { newValue ->
                onAppearanceChange(currentAppearance.copy(loaderText = newValue))
            }
        )
    }
}
