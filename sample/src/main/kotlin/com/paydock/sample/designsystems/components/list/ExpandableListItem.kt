package com.paydock.sample.designsystems.components.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.TitleSection
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ExpandableListItem(
    title: String,
    description: String? = null,
    iconResource: Int? = null,
    isExpanded: Boolean = false,
    isSelected: Boolean = false,
    onExpandedChange: (Boolean) -> Unit,
    onSelectionChange: (() -> Unit)? = null,
    expandedContent: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onSelectionChange?.invoke() ?: onExpandedChange(!isExpanded)
                }
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator (if selection is enabled)
            if (onSelectionChange != null) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
            }

            iconResource?.let { iconRes ->
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Spacer(Modifier.width(16.dp))
            }
            TitleSection(modifier = Modifier.weight(1f), title = title, subTitle = description)
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    expandedContent()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun PreviewExpandableListItem() {
    SampleTheme {
        var expanded by remember { mutableStateOf(false) }
        ExpandableListItem(
            title = "Payment Processor",
            description = "Select payment processor",
            iconResource = R.drawable.ic_config,
            isExpanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            // Expanded content preview
        }
    }
}

