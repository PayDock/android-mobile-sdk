package com.paydock.designsystems.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paydock.MobileSDK
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview

/**
 * Composable function to display an input validation icon.
 * The icon displayed indicates a successful validation state.
 */
@LightDarkPreview
@Composable
internal fun InputValidIcon() {
    Icon(
        modifier = Modifier.testTag("successIcon"), // Modifier to provide a test tag for UI testing
        painter = painterResource(id = R.drawable.ic_success), // Painter resource for the success icon
        contentDescription = stringResource(id = R.string.content_desc_success_icon), // Content description for accessibility
        tint = MobileSDK.getInstance().sdkTheme.colorTheme.success // Tint color for the success icon
    )
}