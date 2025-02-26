package com.paydock.designsystems.components.link

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme

/**
 * A composable that displays text as a clickable hyperlink.
 *
 * @param enabled Controls the enabled state of this hyperlink.
 * @param text The text to be displayed as a hyperlink.
 * @param url The URL to be opened when the hyperlink is clicked.
 */
@Composable
internal fun HyperlinkText(
    enabled: Boolean = true,
    text: String,
    url: String
) {
    val context = LocalContext.current
    val hyperlinkColor = Theme.colors.primary
    val hyperlinkStyle = SpanStyle(
        color = hyperlinkColor,
        textDecoration = TextDecoration.Underline
    )

    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(style = hyperlinkStyle) {
            append(text)
        }
        pop()
    }

    Text(
        text = annotatedString,
        modifier = Modifier
            .clickable {
                if (enabled) {
                    val uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            },
        style = Theme.typography.body1
    )
}

@PreviewLightDark
@Composable
internal fun PreviewHyperlinkText() {
    SdkTheme {
        HyperlinkText(text = "Privacy Policy", url = MobileSDKConstants.DEFAULT_WEB_URL)
    }
}