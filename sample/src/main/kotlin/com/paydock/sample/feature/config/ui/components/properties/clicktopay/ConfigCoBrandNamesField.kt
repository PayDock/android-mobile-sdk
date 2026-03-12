package com.paydock.sample.feature.config.ui.components.properties.clicktopay

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
fun ConfigCoBrandNamesField(
    label: String,
    coBrandNames: List<String>?,
    onCoBrandNamesChange: (List<String>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val allBrandNames = CardBrands.entries.map { it.name.lowercase() }.toSet()
    val selectedNamesSet = coBrandNames?.toSet() ?: allBrandNames
    val displayNames = coBrandNames ?: allBrandNames.toList()
    val isAllowAll = coBrandNames == null || selectedNamesSet.containsAll(allBrandNames)

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
                        onCoBrandNamesChange(null)
                    } else {
                        onCoBrandNamesChange(emptyList())
                    }
                }
            )
            Text(
                text = "Allow All Co-Brand Names",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Always show the list of individual brand names
        Column(modifier = Modifier.padding(start = 16.dp)) {
            CardBrands.entries.forEach { brand ->
                val brandNameLower = brand.name.lowercase()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = displayNames.contains(brandNameLower),
                        onCheckedChange = { checked ->
                            val currentNames = coBrandNames ?: allBrandNames.toList()
                            val newNames = if (checked) {
                                (currentNames + brandNameLower).distinct()
                            } else {
                                currentNames - brandNameLower
                            }
                            // If all brand names are selected, set to null (allow all)
                            val finalNames = if (newNames.toSet().containsAll(allBrandNames)) {
                                null
                            } else {
                                newNames
                            }
                            onCoBrandNamesChange(finalNames)
                        }
                    )
                    Text(
                        text = brandNameLower,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

