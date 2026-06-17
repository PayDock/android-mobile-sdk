package com.paydock.designsystems.components.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.card.CardAppearance
import com.paydock.designsystems.components.card.CardAppearanceDefaults
import com.paydock.designsystems.components.card.SdkCard
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults

/**
 * A composable function that displays an overlay loader covering the maximum available size.
 *
 * It features a centered card containing a loading indicator and configurable loading text.
 * The background of the overlay and the styling of the card and loader are customizable via
 * the [OverlayLoaderAppearance] parameter.
 *
 * @param modifier The modifier to be applied to the overlay loader.
 * @param appearance The appearance configuration for the overlay loader.
 *                   Defaults to [OverlayLoaderAppearanceDefaults.appearance()].
 *
 * @see SdkLoader
 * @see SdkCard
 * @see OverlayLoaderAppearance
 */
@Composable
internal fun SdkOverlayLoader(
    modifier: Modifier = Modifier,
    appearance: OverlayLoaderAppearance
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appearance.backgroundColor)
            .semantics {
                paneTitle = appearance.loaderText.ifEmpty { "Loading" }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val content = @Composable {
            Column(
                // For a dynamic card (cardSize == null) the content wraps so the card hugs it.
                // For a fixed-size card the content fills the card so the Column's centre
                // alignment/arrangement keeps the loader (and optional text) centred within it —
                // otherwise a wrapped Column sits top-start inside the larger fixed card.
                modifier = when {
                    !appearance.showCard -> Modifier
                    appearance.cardSize == null -> Modifier.wrapContentSize()
                    else -> Modifier.fillMaxSize()
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Clear the loader's own "Loading" live-region announcement — the overlay's
                // `paneTitle` (loaderText, falling back to "Loading") is the single accessibility
                // announcement. Otherwise, TalkBack reads both (e.g. "Loading, Payment Processing").
                SdkLoader(
                    modifier = Modifier.clearAndSetSemantics { },
                    size = appearance.loaderSize,
                    appearance = appearance.loaderAppearance
                )

                if (appearance.loaderText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(appearance.loaderSpacing))

                    SdkText(
                        text = appearance.loaderText,
                        appearance = appearance.loaderTextAppearance
                    )
                }
            }
        }

        if (appearance.showCard) {
            // Drive the card's size from cardSize: a fixed size when set, otherwise wrap content.
            // cardAppearance provides the visual styling (colours, corner radius, padding).
            val cardSizeModifier = appearance.cardSize
                ?.let { Modifier.size(it) }
                ?: Modifier.wrapContentSize()
            SdkCard(
                appearance = appearance.cardAppearance.copy(modifier = cardSizeModifier),
                content = content
            )
        } else {
            content()
        }
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSdkOverlayLoader() {
    SdkOverlayLoader(
        appearance = OverlayLoaderAppearanceDefaults.appearance()
    )
}

/**
 * Defines the visual appearance of an overlay loader.
 *
 * This class is immutable and encapsulates all styling properties for the [SdkOverlayLoader],
 * including the background overlay, the central card, and the loader itself.
 *
 * @property backgroundColor The color of the full-screen background overlay.
 * @property showCard Whether to display the loader content inside a centered card.
 * @property cardSize The fixed size of the card, or `null` (default) for dynamic sizing where the
 * card hugs its content (loader + text + padding). When non-null the card uses this size and the
 * loader content is centred within it.
 * @property cardAppearance The appearance configuration for the centered card (colours, corner
 * radius, padding). Its size is governed by [cardSize], not its own modifier.
 * @property loaderAppearance The appearance configuration for the loading indicator.
 * @property loaderSize The size (diameter) of the loading indicator.
 * @property loaderSpacing The vertical spacing between the loader and the text. Only used if
 * [loaderText] is not empty.
 * @property loaderText The text displayed below the loading indicator. Set to empty to hide.
 * @property loaderTextAppearance The appearance configuration for the loading text.
 */
@Immutable
class OverlayLoaderAppearance(
    val backgroundColor: Color,
    val showCard: Boolean = true,
    val cardSize: DpSize? = null,
    val cardAppearance: CardAppearance,
    val loaderAppearance: LoaderAppearance,
    val loaderSize: Dp,
    val loaderSpacing: Dp,
    val loaderText: String,
    val loaderTextAppearance: TextAppearance,
) {

    /**
     * Creates a copy of this appearance with optional property overrides.
     *
     * @param backgroundColor The new background color.
     * @param showCard The new showCard setting.
     * @param cardSize The new card size (`null` for dynamic sizing).
     * @param cardAppearance The new card appearance.
     * @param loaderAppearance The new loader appearance.
     * @param loaderSize The new loader size.
     * @param loaderSpacing The new vertical spacing.
     * @param loaderText The new loading text.
     * @param loaderTextAppearance The new text appearance.
     * @return A new [OverlayLoaderAppearance] instance with the updated values.
     */
    fun copy(
        backgroundColor: Color = this.backgroundColor,
        showCard: Boolean = this.showCard,
        cardSize: DpSize? = this.cardSize,
        cardAppearance: CardAppearance = this.cardAppearance,
        loaderAppearance: LoaderAppearance = this.loaderAppearance,
        loaderSize: Dp = this.loaderSize,
        loaderSpacing: Dp = this.loaderSpacing,
        loaderText: String = this.loaderText,
        loaderTextAppearance: TextAppearance = this.loaderTextAppearance,
    ) = OverlayLoaderAppearance(
        backgroundColor = backgroundColor.takeOrElse { this.backgroundColor },
        showCard = showCard,
        cardSize = cardSize,
        cardAppearance = cardAppearance,
        loaderAppearance = loaderAppearance,
        loaderSize = loaderSize,
        loaderSpacing = loaderSpacing,
        loaderText = loaderText,
        loaderTextAppearance = loaderTextAppearance,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OverlayLoaderAppearance

        if (backgroundColor != other.backgroundColor) return false
        if (showCard != other.showCard) return false
        if (cardSize != other.cardSize) return false
        if (cardAppearance != other.cardAppearance) return false
        if (loaderAppearance != other.loaderAppearance) return false
        if (loaderSize != other.loaderSize) return false
        if (loaderSpacing != other.loaderSpacing) return false
        if (loaderText != other.loaderText) return false
        if (loaderTextAppearance != other.loaderTextAppearance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundColor.hashCode()
        result = 31 * result + showCard.hashCode()
        result = 31 * result + (cardSize?.hashCode() ?: 0)
        result = 31 * result + cardAppearance.hashCode()
        result = 31 * result + loaderAppearance.hashCode()
        result = 31 * result + loaderSize.hashCode()
        result = 31 * result + loaderSpacing.hashCode()
        result = 31 * result + loaderText.hashCode()
        result = 31 * result + loaderTextAppearance.hashCode()
        return result
    }
}

/**
 * Provides default appearance configurations for [OverlayLoaderAppearance].
 */
object OverlayLoaderAppearanceDefaults {

    /**
     * Returns the default appearance for the overlay loader.
     *
     * The default values are:
     * - `backgroundColor`: [Color.Black] with 0.45 alpha
     * - `showCard`: `true`
     * - `cardAppearance`: [CardAppearanceDefaults.appearance()] with 32.dp content padding
     * - `loaderAppearance`: [LoaderAppearanceDefaults.appearance()]
     * - `loaderSize`: 32.dp
     * - `loaderSpacing`: 16.dp
     * - `loaderText`: "Loading..."
     * - `loaderTextAppearance`: [TextAppearanceDefaults.appearance()]
     *
     * @return A default [OverlayLoaderAppearance] instance.
     */
    @Composable
    fun appearance(): OverlayLoaderAppearance = OverlayLoaderAppearance(
        backgroundColor = Color.Black.copy(alpha = 0.45f),
        showCard = true,
        // null = dynamic sizing: the card hugs its content (spinner + text + padding).
        // Set a DpSize for a fixed card.
        cardSize = null,
        cardAppearance = CardAppearanceDefaults.appearance().copy(
            contentPadding = PaddingValues(32.0.dp)
        ),
        loaderAppearance = LoaderAppearanceDefaults.appearance(),
        loaderSize = 32.0.dp,
        loaderSpacing = 16.0.dp,
        loaderText = "Loading...",
        loaderTextAppearance = TextAppearanceDefaults.appearance()
    )
}
