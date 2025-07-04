package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.icon.IconAppearanceDefaults
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.integration.enums.CardType.Companion.displayLabel

/**
 * A composable function to display an icon representing the card issuer.
 *
 * This function dynamically renders an icon based on the provided card issuer type
 * and adjusts its appearance based on the focus state.
 *
 * @param cardType The type of card scheme, represented as a `CardScheme`.
 * This determines the specific icon resource to be displayed.
 * @param focused A flag indicating whether the card input field is focused.
 * This affects the tint applied to the icon when the card scheme type is `OTHER`.
 */
@Composable
internal fun CardSchemeIcon(cardType: CardType?, focused: Boolean = false) {
    SdkIcon(
        modifier = Modifier
            .testTag("cardIcon")
            .width(24.dp),
        painter = painterResource(id = mapSchemeToDrawable(cardType)),
        contentDescription = cardType?.displayLabel(),
        appearance = IconAppearanceDefaults.appearance().copy(
            tint = if (cardType == null) {
                if (focused) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                Color.Unspecified
            }
        ),
    )
}

/**
 * Maps a given card scheme to its corresponding drawable resource ID.
 *
 * This function takes a card scheme as input and returns the drawable resource ID
 * associated with the scheme. If the scheme is `null` or does not match any known
 * scheme, a default credit card drawable resource ID is returned.
 *
 * @param scheme The card scheme to be mapped. Can be `null`.
 * @return The drawable resource ID corresponding to the provided card scheme, or
 * the default credit card drawable if the scheme is `null` or unrecognized.
 */
private fun mapSchemeToDrawable(scheme: CardType?): Int = when (scheme) {
    CardType.AMEX -> R.drawable.ic_amex
    CardType.AUSBC -> R.drawable.ic_ausbc
    CardType.DINERS -> R.drawable.ic_diners_club
    CardType.DISCOVER -> R.drawable.ic_discover
    CardType.JAPCB -> R.drawable.ic_jcb
    CardType.MASTERCARD -> R.drawable.ic_mastercard
    CardType.SOLO -> R.drawable.ic_solo
    CardType.VISA -> R.drawable.ic_visa
    else -> R.drawable.ic_credit_card
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSchemeIconUnknownFocused() {
    CardSchemeIcon(null, true)
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSchemeIconUnknownUnfocused() {
    CardSchemeIcon(null, false)
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSchemeIconVisa() {
    CardSchemeIcon(CardType.VISA, false)
}