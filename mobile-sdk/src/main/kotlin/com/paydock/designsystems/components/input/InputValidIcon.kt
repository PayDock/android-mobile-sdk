package com.paydock.designsystems.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.icon.IconAppearanceDefaults
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.designsystems.theme.Success

/**
 * Composable function to display an input validation icon.
 * The icon displayed indicates a successful validation state.
 */
@SdkLightDarkPreviews
@Composable
internal fun InputValidIcon() {
    SdkIcon(
        modifier = Modifier.testTag("successIcon"),
        painter = painterResource(id = R.drawable.ic_success),
        contentDescription = stringResource(id = R.string.content_desc_success_icon),
        appearance = IconAppearanceDefaults.appearance().copy(tint = Success)
    )
}