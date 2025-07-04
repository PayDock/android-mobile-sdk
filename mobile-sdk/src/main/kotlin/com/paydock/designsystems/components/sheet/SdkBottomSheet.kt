package com.paydock.designsystems.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.designsystems.components.icon.IconAppearanceDefaults
import com.paydock.designsystems.components.icon.SdkIcon

/**
 * Composable function to display a modal bottom sheet with customizable content.
 *
 * This may require declared in the Activity, otherwise the [ModalBottomSheet] may be obscured by WindowInsets
 * Example: (Navigation Bar, Status Bar etc)
 * @see { WindowCompat.setDecorFitsSystemWindows(window, false) }
 *
 * @param allowFullScreen Flag indicating whether the bottom sheet can take up the full screen.
 * @param bottomSheetState The state of the bottom sheet to control its visibility and position.
 * @param onDismissRequest Callback to be invoked when the bottom sheet is dismissed.
 * @param content The content of the bottom sheet provided as a composable function.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdkBottomSheet(
    containerColor: Color = MaterialTheme.colorScheme.background,
    allowFullScreen: Boolean = false,
    bottomSheetState: SheetState,
    enableClose: Boolean = true,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    // Set window insets to customize the space taken by the bottom sheet
    val windowInsets = if (allowFullScreen) {
        WindowInsets(0)
    } else {
        BottomSheetDefaults.windowInsets
    }

    // Define modifier based on whether full screen is allowed or not
    val modifier = if (allowFullScreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxWidth()
    }

    // Display a modal bottom sheet with customizable content
    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        containerColor = containerColor,
        contentWindowInsets = { windowInsets },
        dragHandle = null,
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState
    ) {
        // Column to structure content within the bottom sheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button at the top-right corner
            IconButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onDismissRequest,
                enabled = enableClose
            ) {
                SdkIcon(
                    painter = painterResource(id = R.drawable.ic_close_circle),
                    contentDescription = stringResource(id = R.string.content_desc_close_icon),
                    appearance = IconAppearanceDefaults.appearance().copy(
                        tint = if (enableClose) {
                            LocalContentColor.current
                        } else {
                            LocalContentColor.current.copy(
                                alpha = 0.2f
                            )
                        }
                    )
                )
            }
            // Content provided by the caller
            content()
        }
    }
}
