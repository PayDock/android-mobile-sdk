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
import com.paydock.feature.src.domain.model.integration.meta.enum.CardBrands

@Composable
fun ConfigCardBrandsField(
    label: String,
    selectedBrands: List<CardBrands>?,
    onBrandsChange: (List<CardBrands>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val allBrands = CardBrands.entries.toList()
    val selectedBrandsSet = selectedBrands?.toSet() ?: allBrands.toSet()
    val displayBrands = selectedBrands ?: allBrands
    val isAllowAll = selectedBrands == null || selectedBrandsSet.containsAll(allBrands.toSet())

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
                        onBrandsChange(null)
                    } else {
                        onBrandsChange(emptyList())
                    }
                }
            )
            Text(
                text = "Allow All Card Brands",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Always show the list of individual brands
        Column(modifier = Modifier.padding(start = 16.dp)) {
            CardBrands.entries.forEach { brand ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = displayBrands.contains(brand),
                        onCheckedChange = { checked ->
                            val currentBrands = selectedBrands ?: allBrands
                            val newBrands = if (checked) {
                                (currentBrands + brand).distinct()
                            } else {
                                currentBrands - brand
                            }
                            // If all brands are selected, set to null (allow all)
                            val finalBrands =
                                if (newBrands.toSet().containsAll(allBrands.toSet())) {
                                    null
                                } else {
                                    newBrands
                                }
                            onBrandsChange(finalBrands)
                        }
                    )
                    Text(
                        text = brand.name,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

