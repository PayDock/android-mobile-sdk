package com.paydock.sample.designsystems.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    title: String,
    trailingContent: (@Composable () -> Unit)? = null,
    skipPartiallyExpanded: Boolean = true,
    expandOnOpen: Boolean = true,
    allowOutsideDismiss: Boolean = false,
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = { newValue -> allowOutsideDismiss || newValue != SheetValue.Hidden }
    )

    if (expandOnOpen) {
        LaunchedEffect(Unit) {
            if (sheetState.currentValue != SheetValue.Expanded) {
                sheetState.expand()
            }
        }
    }

    val handleDismiss: () -> Unit = if (allowOutsideDismiss) onDismissRequest else ({})

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                trailingContent?.invoke()
            }
            content()
        }
    }
}


