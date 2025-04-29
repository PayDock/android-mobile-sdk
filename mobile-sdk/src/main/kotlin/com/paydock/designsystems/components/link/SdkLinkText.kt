package com.paydock.designsystems.components.link

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.core.presentation.ui.previews.SdkFontScalePreviews
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearanceDefaults

/**
 * A composable function that renders a clickable text link.
 *
 * This function displays text with an underlined style, resembling a hyperlink.
 * When clicked, it triggers the provided [onClick] callback.
 *
 * @param modifier The [Modifier] to be applied to the underlying [Text] composable.
 * @param linkText The text content of the link.
 * @param onClick A callback function that is invoked when the link is clicked.
 */
@Composable
internal fun SdkLinkText(
    modifier: Modifier = Modifier,
    linkText: String,
    onClick: () -> Unit
) {
    Box {
        SdkText(
            modifier = modifier
                .sizeIn(minHeight = 24.dp)
                .wrapContentSize(Alignment.Center)
                .clickable {
                    onClick()
                },
            text = linkText,
            appearance = TextAppearanceDefaults.appearance().copy(
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value + 1).sp
                )
            )
        )
    }
}

@SdkFontScalePreviews
@Composable
internal fun PreviewLinkText() {
    SdkLinkText(linkText = "Privacy Policy") { }
}