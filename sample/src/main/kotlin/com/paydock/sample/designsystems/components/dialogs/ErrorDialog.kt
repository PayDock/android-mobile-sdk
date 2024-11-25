package com.paydock.sample.designsystems.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogText: String,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.label_something_went_wrong))
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

@Preview(showBackground = true)
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
