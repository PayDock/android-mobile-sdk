package com.paydock.sample.designsystems.components.button

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
internal fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    PrimaryButton(
        modifier = modifier.height(48.dp),
        onClick = onClick,
        enabled = enabled && !isLoading,
        content = { ButtonContent(text = text, isLoading = isLoading) }
    )
}

@Composable
private fun PrimaryButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
        shape = MaterialTheme.shapes.extraSmall,
    )
}

@Composable
private fun ButtonContent(text: String, isLoading: Boolean) {
    Crossfade(targetState = isLoading, label = "coss_fade") {
        if (it) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 1.dp
            )
        } else {
            Text(
                text = text
            )
        }
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonPrimary() {
    SampleTheme {
        AppButton(
            text = "Primary",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewButtonPrimaryWithLoading() {
    SampleTheme() {
        AppButton(
            text = "Primary",
            onClick = {},
            isLoading = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
