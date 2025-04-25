package com.paydock.designsystems.components.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews

/**
 * Displays a loading indicator (circular progress indicator) that fills the width of its parent.
 *
 * This composable is typically used to indicate that data is being loaded or processed.  It places
 * the circular indicator in the center of a transparent background that occupies the full width
 * of the available space.
 *
 * @param modifier Optional [Modifier] to apply to the loading indicator's container.  This can be
 *        used to adjust properties such as padding, margins, and alignment relative to the parent
 *        layout.  The [Modifier] is applied to the outer [Box] that contains the loading indicator,
 *        not directly to the [CircularProgressIndicator] itself.  If you want to modify the
 *        [CircularProgressIndicator] directly, you should pass the [Modifier] to it within the
 *        parent layout.
 */
@SdkLightDarkPreviews
@Composable
internal fun SdkLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = modifier)
    }
}