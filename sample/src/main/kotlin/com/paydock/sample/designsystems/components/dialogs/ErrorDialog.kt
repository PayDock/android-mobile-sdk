package com.paydock.sample.designsystems.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String = stringResource(R.string.label_transaction_failed),
    dialogText: String,
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.button_ok))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ErrorDialogPreview() {
    SampleTheme {
        ErrorDialog(
            onDismissRequest = {},
            onConfirmation = {},
            dialogText = "This is an error dialog"
        )
    }
}
