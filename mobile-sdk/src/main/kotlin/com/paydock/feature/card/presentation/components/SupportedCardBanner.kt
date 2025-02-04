package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.card.domain.model.integration.enums.CardType

/**
 * Displays a banner showing the supported card schemes as icons.
 *
 * This composable function renders a horizontal row of card scheme icons,
 * representing the payment methods accepted by the integration.
 * The icons are aligned to the right/end of the banner and spaced with 7.dp between each image.
 *
 * @param supportedSchemes A list of [CardType] enums representing the supported card schemes.
 */
@Composable
internal fun SupportedCardBanner(supportedSchemes: Set<CardType>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        supportedSchemes.forEach { cardScheme ->
            Box(
                modifier = Modifier
                    .padding(end = 7.dp)
            ) {
                CardSchemeIcon(
                    cardScheme,
                    true
                )
            }
        }
    }
}