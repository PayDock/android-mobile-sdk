package com.paydock.sample.feature.config.ui.components.properties.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.card.domain.model.integration.enums.CardType

@Composable
fun ConfigCardSchemesField(
    label: String,
    selectedSchemes: Set<CardType>?,
    onSchemesChange: (Set<CardType>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCardTypes = CardType.entries.toSet()
    val displaySchemes = selectedSchemes ?: allCardTypes
    val isAllowAll = selectedSchemes == null || selectedSchemes.containsAll(allCardTypes)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Option to select all or none
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAllowAll,
                onCheckedChange = { checked ->
                    if (checked) {
                        onSchemesChange(null)
                    } else {
                        onSchemesChange(emptySet())
                    }
                }
            )
            Text(
                text = "Allow All Card Schemes",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Always show the list of individual schemes
        Column(modifier = Modifier.padding(start = 16.dp)) {
            CardType.entries.forEach { cardType ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = displaySchemes.contains(cardType),
                        onCheckedChange = { checked ->
                            val currentSchemes = selectedSchemes ?: allCardTypes
                            val newSchemes = if (checked) {
                                currentSchemes + cardType
                            } else {
                                currentSchemes - cardType
                            }
                            // If all schemes are selected, set to null (allow all)
                            val finalSchemes = if (newSchemes.containsAll(allCardTypes)) {
                                null
                            } else {
                                newSchemes
                            }
                            onSchemesChange(finalSchemes)
                        }
                    )
                    Text(
                        text = getCardTypeDisplayName(cardType),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun getCardTypeDisplayName(cardType: CardType): String {
    return when (cardType) {
        CardType.AMEX -> "American Express"
        CardType.DINERS -> "Diners Club"
        CardType.DISCOVER -> "Discover"
        CardType.JAPCB -> "JCB"
        CardType.MASTERCARD -> "MasterCard"
        CardType.VISA -> "Visa"
        CardType.UNIONPAY -> "UnionPay International"
    }
}

