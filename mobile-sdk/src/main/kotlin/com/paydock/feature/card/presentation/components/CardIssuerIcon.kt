package com.paydock.feature.card.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.enums.CardIssuerType

/**
 * A composable function to display an icon representing the card issuer.
 *
 * This function dynamically renders an icon based on the provided card issuer type
 * and adjusts its appearance based on the focus state.
 *
 * @param cardIssuer The type of card issuer, represented as a `CardIssuerType`.
 * This determines the specific icon resource to be displayed.
 * @param focused A flag indicating whether the card input field is focused.
 * This affects the tint applied to the icon when the card issuer type is `OTHER`.
 */
@Composable
internal fun CardIssuerIcon(cardIssuer: CardIssuerType, focused: Boolean) {
    Icon(
        modifier = Modifier.testTag("cardIcon"),
        painter = painterResource(id = cardIssuer.imageResId),
        contentDescription = null,
        tint = if (cardIssuer == CardIssuerType.OTHER) {
            if (focused) Theme.colors.onSurface else Theme.colors.onSurfaceVariant
        } else Color.Unspecified
    )
}
