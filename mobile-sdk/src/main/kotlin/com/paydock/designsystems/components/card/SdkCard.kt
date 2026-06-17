package com.paydock.designsystems.components.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews

@Composable
fun SdkCard(
    appearance: CardAppearance = CardAppearanceDefaults.appearance(),
    content: @Composable () -> Unit
) {
    Card(
        modifier = appearance.modifier,
        shape = RoundedCornerShape(appearance.cornerRadius),
        colors = appearance.cardColors
    ) {
        Box(modifier = Modifier.padding(appearance.contentPadding)) {
            content()
        }
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkCard() {
    SdkCard(
        appearance = CardAppearanceDefaults.appearance()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("This is a card")
        }
    }
}

/**
 * Represents the appearance of a card.
 *
 * This class is immutable, ensuring that once an instance is created, its properties cannot be changed.
 * This is particularly useful in Compose, where immutability helps optimize recomposition.
 *
 * @property modifier The [Modifier] to apply to the card.
 * @property cornerRadius The corner radius of the card.
 * @property cardColors The colors to use for the card.
 * @property contentPadding The padding to apply around the content inside the card.
 */
@Immutable
class CardAppearance(
    val modifier: Modifier,
    val cornerRadius: Dp,
    val cardColors: CardColors,
    val contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    /**
     * Creates a new [CardAppearance] instance with the same properties as this one, but with optionally overridden values.
     *
     * @param modifier The new [Modifier] to apply.
     * @param cornerRadius The new corner radius of the card.
     * @param cardColors The new colors to use for the card.
     * @param contentPadding The new content padding.
     */
    fun copy(
        modifier: Modifier = this.modifier,
        cornerRadius: Dp = this.cornerRadius,
        cardColors: CardColors = this.cardColors,
        contentPadding: PaddingValues = this.contentPadding
    ): CardAppearance = CardAppearance(
        modifier = modifier,
        cornerRadius = cornerRadius.takeOrElse { this.cornerRadius },
        cardColors = cardColors,
        contentPadding = contentPadding
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CardAppearance

        if (modifier != other.modifier) return false
        if (cornerRadius != other.cornerRadius) return false
        if (cardColors != other.cardColors) return false
        if (contentPadding != other.contentPadding) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modifier.hashCode()
        result = 31 * result + cornerRadius.hashCode()
        result = 31 * result + cardColors.hashCode()
        result = 31 * result + contentPadding.hashCode()
        return result
    }
}

/**
 * [CardAppearanceDefaults] provides default values for [CardAppearance].
 *
 * This object contains functions that return pre-configured [CardAppearance] instances with
 * sensible default values. These defaults can be used directly or as a basis for further
 * customization.
 */
object CardAppearanceDefaults {

    /**
     * Defines the default visual appearance for a card.
     *
     * This function provides a pre-configured [CardAppearance] object with sensible defaults
     * for common use cases. The default values include:
     *
     * - `modifier`: A default size of 150dp x 150dp.
     * - `cornerRadius`: 28.dp.
     * - `cardColors`: The colors to use for the card.
     * - `contentPadding`: 16.dp.
     */
    @Composable
    fun appearance(): CardAppearance = CardAppearance(
        modifier = Modifier.size(150.0.dp),
        cornerRadius = 28.0.dp,
        cardColors = CardDefaults.cardColors(),
        contentPadding = PaddingValues(16.dp)
    )
}