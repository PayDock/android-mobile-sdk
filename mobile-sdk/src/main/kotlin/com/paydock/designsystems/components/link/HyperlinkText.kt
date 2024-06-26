package com.paydock.designsystems.components.link

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme

/**
 * A composable that displays text as a clickable hyperlink.
 *
 * @param text The text to be displayed as a hyperlink.
 * @param url The URL to be opened when the hyperlink is clicked.
 */
@Composable
internal fun HyperlinkText(text: String, url: String) {
    val context = LocalContext.current
    val hyperlinkColor = Theme.colors.onSurface
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

    ClickableText(
        modifier = Modifier.fillMaxWidth(),
        text = annotatedString,
        style = Theme.typography.body1,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val uri = Uri.parse(annotation.item)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
        }
    )
}

@LightDarkPreview
@Composable
private fun PreviewHyperlinkText() {
    SdkTheme {
        HyperlinkText(text = "Privacy Policy", url = "https://paydock.com")
    }
}