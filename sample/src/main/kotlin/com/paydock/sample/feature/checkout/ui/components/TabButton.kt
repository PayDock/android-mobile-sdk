package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.Border
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun TabButton(
    isSelected: Boolean,
    selectedBorderColor: Color,
    selectedBackgroundColor: Color,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .width(90.dp)
            .height(49.dp)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) selectedBorderColor else Border
                ),
                shape = RoundedCornerShape(size = 4.dp)
            )
            .background(
                color = if (isSelected) selectedBackgroundColor else Theme.colors.tertiaryContainer,
                shape = RoundedCornerShape(size = 4.dp)
            )
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}