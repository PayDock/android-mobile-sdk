package com.paydock.designsystems.components.link

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.sp
import com.paydock.designsystems.theme.Theme

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
internal fun LinkText(
    modifier: Modifier = Modifier,
    linkText: String,
    onClick: () -> Unit
) {
    Text(
        text = linkText,
        modifier = modifier
            .clickable {
                onClick()
            },
        style = Theme.typography.body1.copy(
            fontSize = 17.sp,
            lineHeight = 22.sp,
            textDecoration = TextDecoration.Underline
        ),
        color = Theme.colors.primary
    )
}

@PreviewLightDark
@Composable
internal fun PreviewLinkText() {
    LinkText(linkText = "Privacy Policy") { }
}