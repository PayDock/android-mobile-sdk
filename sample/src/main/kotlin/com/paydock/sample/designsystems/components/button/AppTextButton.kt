package com.paydock.sample.designsystems.components.button

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
internal fun AppTextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = style,
            fontWeight = fontWeight
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewAppTextButton() {
    SampleTheme {
        AppTextButton(text = "Add Address", onClick = {})
    }
}


