package com.paydock.sample.designsystems.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.SampleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterAppTopBar(
    title: String,
    showTitle: Boolean,
    modifier: Modifier = Modifier,
    actionContent: (@Composable (() -> Unit))? = null,
    navigationContent: (@Composable (() -> Unit))? = null,
    onActionButtonClick: (() -> Unit)? = null,
    onBackButtonClick: (() -> Unit)? = null,
) {
    if (showTitle) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    modifier = modifier,
                    text = title
                )
            },
            modifier = modifier,
            navigationIcon = {
                if (navigationContent != null) {
                    navigationContent()
                } else {
                    onBackButtonClick?.let { clickListener ->
                        IconButton(onClick = { clickListener.invoke() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            actions = { actionContent?.invoke() },
            colors = TopAppBarDefaults.topAppBarColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        )
    } else {
        TopAppBar(
            title = {
                Image(
                    painter = painterResource(id = R.drawable.demo_icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            },
            actions = {
                if (actionContent != null) {
                    actionContent()
                } else {
                    IconButton(onClick = { onActionButtonClick?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            navigationIcon = {
                if (navigationContent != null) {
                    navigationContent()
                } else {
                    onBackButtonClick?.let { clickListener ->
                        IconButton(onClick = { clickListener.invoke() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewCenterAppTopBar() {
    SampleTheme {
        CenterAppTopBar(title = "Center Top Bar", showTitle = true)
    }
}

@Composable
@PreviewLightDark
internal fun PreviewCenterAppTopBarWithoutTitle() {
    SampleTheme {
        CenterAppTopBar(title = "Center Top Bar", showTitle = false)
    }
}

@Composable
@PreviewLightDark
internal fun PreviewCenterAppTopBarWithBackButton() {
    SampleTheme {
        CenterAppTopBar(
            title = "Center Top Bar",
            showTitle = true,
            onBackButtonClick = { }
        )
    }
}

@Composable
@PreviewLightDark
internal fun PreviewCenterAppTopBarWithoutTitleWithBackButton() {
    SampleTheme {
        CenterAppTopBar(
            title = "Center Top Bar",
            showTitle = false,
            onBackButtonClick = { }
        )
    }
}
