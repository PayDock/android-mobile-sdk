package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.enums.CardScheme

/**
 * A composable function to display an icon representing the card issuer.
 *
 * This function dynamically renders an icon based on the provided card issuer type
 * and adjusts its appearance based on the focus state.
 *
 * @param cardScheme The type of card scheme, represented as a `CardScheme`.
 * This determines the specific icon resource to be displayed.
 * @param focused A flag indicating whether the card input field is focused.
 * This affects the tint applied to the icon when the card scheme type is `OTHER`.
 */
@Composable
internal fun CardSchemeIcon(cardScheme: CardScheme?, focused: Boolean) {
    Icon(
        modifier = Modifier
            .testTag("cardIcon")
            .width(24.dp),
        painter = painterResource(id = mapSchemeToDrawable(cardScheme)),
        contentDescription = null,
        tint = if (cardScheme == null) {
            if (focused) Theme.colors.onSurface else Theme.colors.onSurfaceVariant
        } else Color.Unspecified
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
private fun mapSchemeToDrawable(scheme: CardScheme?): Int = when (scheme) {
    CardScheme.AMEX -> R.drawable.ic_amex
    CardScheme.AUSBC -> R.drawable.ic_ausbc
    CardScheme.DINERS -> R.drawable.ic_diners_club
    CardScheme.DISCOVER -> R.drawable.ic_discover
    CardScheme.JAPCB -> R.drawable.ic_jcb
    CardScheme.MASTERCARD -> R.drawable.ic_mastercard
    CardScheme.SOLO -> R.drawable.ic_solo
    CardScheme.VISA -> R.drawable.ic_visa
    else -> R.drawable.ic_credit_card
}
